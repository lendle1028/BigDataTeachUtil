package net.sf.jagg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.jagg.msd.Discriminators;
import net.sf.jagg.msd.MsdWorkspace;
import net.sf.jagg.msd.PropertiesDiscriminator;
import net.sf.jagg.exception.JaggException;
import net.sf.jagg.model.AggregateValue;
import net.sf.jagg.model.PositionedAggregatorList;
import net.sf.jagg.util.AggregateValuePropertiesComparator;
import net.sf.jagg.util.ComparableComparator;
import net.sf.jagg.util.PropertiesComparator;

/**
 * The <code>Aggregation</code> class performs the actual aggregation
 * operations.  It contains a <code>Builder</code> class that, following the
 * Builder Pattern, builds a <code>Aggregation</code> object that can be used
 * for the actual aggregation calculations.
 *
 * @author Randy Gettman
 * @since 0.7.0
 */
public class Aggregation
{
   private static final boolean DEBUG = false;

   private static ThreadPoolExecutor theThreadPool = null;

   private List<AggregateFunction> myAggregators;
   private List<String> myProperties;
   private int myParallelism;
   private boolean amIUsingMsd;
   private MsdWorkspace myWorkspace;
   private List<List<Integer>> myGroupingSets;
   private boolean amIUsingSuperAggregation;

   /**
    * Private constructor to ensure that the "Builder" pattern is used.
    * @param builder A <code>Builder</code>.
    */
   private Aggregation(Builder builder)
   {
      myAggregators = builder.myAggregators;
      myProperties = builder.myProperties;
      myParallelism = builder.myParallelism;
      amIUsingMsd = builder.amIUsingMsd;
      myGroupingSets = builder.myGroupingSets;
      amIUsingSuperAggregation = builder.amIUsingSuperAggregation;
   }

   /**
    * Perform one or more aggregate operations on a <code>List&lt;T&gt;</code>.
    * <code>T</code> should have a "natural ordering", that is, it must be
    * <code>Comparable</code>, and <code>compareTo</code> defines the
    * properties with which to "group by" with its consideration of different
    * properties to determine order.  This sorts a copy of the list of values,
    * based how the objects' <code>compareTo</code> method compares them.
    * @param <T> The object type to aggregate, which must be
    *    <code>Comparable</code>.
    * @param values The <code>List&lt;T&gt;</code> of objects to aggregate.
    * @return A <code>List&lt;AggregateValue&lt;T&gt;&gt;</code>.
    */
   public <T extends Comparable<? super T>> List<AggregateValue<T>> groupByComparable(List<T> values)
   {
      List<T> listCopy = new ArrayList<T>(values);
      ComparableComparator<T> comparator = new ComparableComparator<T>();
      //long start = System.nanoTime();
      Collections.sort(listCopy, comparator);
      //long finish = System.nanoTime();
      //System.out.println("Sort time: " + (finish - start));

      return doAggregation(listCopy, comparator);
   }

   /**
    * Perform one or more aggregate operations on a <code>List&lt;T&gt;</code>.
    * <code>T</code> does not need to be <code>Comparable</code>.  This
    * operates on a copy of the list of values, either sorted based on the
    * "group by" properties (if any), or grouped into equivalence classes using
    * multiset discrimination.
    * @param <T> The object type to aggregate.
    * @param values The <code>List&lt;T&gt;</code> of objects to aggregate.
    * @return A <code>List&lt;AggregateValue&lt;T&gt;&gt;</code>.
    */
   public <T> List<AggregateValue<T>> groupBy(List<T> values)
   {
      // If no values and no properties, must return one AggregateValue with
      // all Aggregators' initial values.
      if (values.size() == 0 && myProperties.size() == 0)
      {
         return getEmptyAggregateValues();
      }
      PropertiesDiscriminator<T> disc = new PropertiesDiscriminator<T>(myProperties);
      PropertiesComparator<T> comparator = new PropertiesComparator<T>(myProperties);
      List<T> listCopy = null;
      if (myProperties.size() > 0)
      {
         // There are "group by" properties.
         if (amIUsingMsd)
         {
            //long start = System.nanoTime();
            if (myWorkspace == null)
               myWorkspace = new MsdWorkspace();
            List<List<T>> listOfLists = disc.discriminate(values, myWorkspace);
            // If msd succeeded.
            if (listOfLists != null)
               listCopy = Discriminators.getFlattenedList(listOfLists);
            //long finish = System.nanoTime();
            //System.out.println("Discr time: " + (finish - start));
         }
         if (listCopy == null)
         {
            // Either we tried multiset discrimination, and bummer, not
            // Discriminable, or the user chose not to use it. Fall back to
            // sorting with the PropertiesComparator.
            listCopy = new ArrayList<T>(values);
            Collections.sort(listCopy, comparator);
         }
      }
      else
      {
         // No "group by" properties.  No need to manipulate the list of
         // values. No need to make a list copy.  And yes, nothing modifies the
         // original list of values.
         listCopy = values;
      }

      return doAggregation(listCopy, comparator);
   }

   /**
    * Perform the actual aggregation.  This restricts the parallelism based on
    * the size of the list of values to aggregate, e.g. don't want to have a
    * parallelism of 8 when the list size is 6.  Then it delegates to either
    * the single-threaded or multi-threaded version of
    * <code>getAggregateValues</code>.
    * @param listCopy The sorted copy of the list of values to aggregate.
    * @param comparator A <code>Comparator</code> over T objects.
    * @return A <code>List</code> of <code>AggregateValues</code>.
    */
   private <T> List<AggregateValue<T>> doAggregation(List<T> listCopy,
      Comparator<? super T> comparator)
   {
      List<AggregateValue<T>> aggregatedList;
      int size = listCopy.size();
      int minParallelism = (myParallelism > size) ? size : myParallelism;
      if (minParallelism > 1)
         aggregatedList = getAggregateValues(listCopy, comparator, myParallelism);
      else
         aggregatedList = getAggregateValues(listCopy, comparator);
      if (amIUsingSuperAggregation)
         getSuperAggregateValues(aggregatedList);
      return aggregatedList;
   }

   /**
    * Create an <code>ExecutorCompletionService</code>.
    * @return An <code>ExecutorCompletionService</code>.
    */
   private static <T> ExecutorCompletionService<PositionedAggregatorList<T>> initializeService()
   {
      if (theThreadPool == null)
      {
         int numProcessors = Runtime.getRuntime().availableProcessors();
         theThreadPool = new ThreadPoolExecutor(0, numProcessors,
            0, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
      }
      return new ExecutorCompletionService<PositionedAggregatorList<T>>(theThreadPool);
   }

   /**
    * Get all aggregate values for all aggregators.  This is the multi-threaded
    * version.
    * @param list The sorted list copy of values to aggregate.
    * @param comparator A <code>Comparator</code> over T objects.
    * @param parallelism The degree of parallelism.
    * @return A <code>List</code> of <code>AggregateValues</code>.
    */
   private <T> List<AggregateValue<T>> getAggregateValues(List<T> list,
      Comparator<? super T> comparator, int parallelism)
   {
      List<PositionedAggregatorList<T>> listOfPals = new ArrayList<PositionedAggregatorList<T>>(parallelism);
      // Initialize it with null elements, so that when "set" is called later,
      // Java won't complain about the size being zero.
      for (int p = 0; p < parallelism; p++)
         listOfPals.add(null);

      // Lazy-initialize the ExecutorCompletionService.
      ExecutorCompletionService<PositionedAggregatorList<T>> service = initializeService();
      int size = list.size();
      // Submit AggregateRunners that are specific to this task.
      for (int p = 0; p < parallelism; p++)
      {
         int startIndex = (size * p) / parallelism;
         int endIndex = (size * (p + 1)) / parallelism - 1;
         service.submit(new AggregateRunner<T>(myAggregators, list, p, comparator, startIndex, endIndex, amIUsingSuperAggregation, myProperties));
      }

      // Wait until all Threads have created their PositionedAggregatorList.
      // If an Exception is thrown, it will be caught in the form of an
      // ExecutionException, and wrapped in an JaggException.
      int numPALs = 0;
      while (numPALs < parallelism)
      {
         try
         {
            Future<PositionedAggregatorList<T>> future = service.poll(1, TimeUnit.SECONDS);
            if (future != null)
            {
               // Something completed and is available.  Add it to the list of
               // PALs in its proper position.
               PositionedAggregatorList<T> pal = future.get();
               if (DEBUG)
               {
                  System.err.println("PAL position " + pal.getPosition() + " is finished.");
               }
               listOfPals.set(pal.getPosition(), pal);
               numPALs++;
            }
         }
         catch(InterruptedException ignored) {}
         catch(ExecutionException e)
         {
            throw new JaggException(e.getClass().getName() +
               " caught while aggregating.", e);
         }
      }

      return Aggregations.mergeLists(listOfPals, comparator, amIUsingSuperAggregation, myProperties);
   }

   /**
    * Get all aggregate values for all aggregators.  This is the single-
    * threaded version.
    * @param list The sorted list copy of values to aggregate.
    * @param comparator A <code>Comparator</code> over T objects.
    * @return A <code>List</code> of <code>AggregateValues</code>.
    */
   private <T> List<AggregateValue<T>> getAggregateValues(List<T> list,
      Comparator<? super T> comparator)
   {
      List<AggregateValue<T>> aggValues = new ArrayList<AggregateValue<T>>();
      List<AggregateFunction> aggList = getAggregatorsList();
      int aggSize = myAggregators.size();
      int startIndex = 0;
      int endIndex;
      int listsize = list.size();
      int numProperties = (myProperties != null) ? myProperties.size() : 0;
      List<Integer> defaultGroupingSet = new ArrayList<Integer>(numProperties);
      for (int i = 0; i < numProperties; i++)
         defaultGroupingSet.add(i);
      while (startIndex < listsize)
      {
         AggregateValue<T> aggValue = new AggregateValue<T>(list.get(startIndex));
         endIndex = Aggregations.indexOfLastMatching(list, comparator, startIndex);

         // If no super-aggregation, then use the same Aggregators throughout
         // the entire process; they can be reused each loop.
         // If super-aggregation, then use different Aggregators in each loop.
         if (amIUsingSuperAggregation && startIndex > 0)
            aggList = getAggregatorsList();

         for (int a = 0; a < aggSize; a++)
         {
            AggregateFunction agg = aggList.get(a);
            agg.init();
         }
         for (int i = startIndex; i <= endIndex; i++)
         {
            T value = list.get(i);
            for (int a = 0; a < aggSize; a++)
            {
               AggregateFunction agg = aggList.get(a);
               agg.iterate(value);
            }
         }
         for (int a = 0; a < aggSize; a++)
         {
            AggregateFunction agg = aggList.get(a);
            aggValue.setAggregateValue(agg, agg.terminate());
         }

         if (myProperties != null)
            aggValue.assignPropsAndGroupingSet(myProperties, defaultGroupingSet);
         // Store for super-aggregation later.
         if (amIUsingSuperAggregation)
            aggValue.assignAggregators(aggList);

         aggValues.add(aggValue);

         // Setup for next loop (if any).
         startIndex = endIndex + 1;
      }
      // No longer using the Aggregators unless we need them later for
      // super-aggregation.
      if (!amIUsingSuperAggregation)
      {
         for (int a = 0; a < aggSize; a++)
         {
            AggregateFunction agg = aggList.get(a);
            agg.setInUse(false);
         }
      }
      return aggValues;
   }

   /**
    * Helper function to create a new <code>List</code> of
    * <code>AggregateFunctions</code>, given a list of archetypes.
    * @return A <code>List</code> of <code>AggregateFunctions</code>.
    */
   private List<AggregateFunction> getAggregatorsList()
   {
      int aggSize = myAggregators.size();
      List<AggregateFunction> aggList = new ArrayList<AggregateFunction>(aggSize);
      for (int a = 0; a < aggSize; a++)
      {
         AggregateFunction archetype = myAggregators.get(a);
         aggList.add(Aggregator.getAggregator(archetype));
      }
      return aggList;
   }

   /**
    * Return a <code>List</code> of one <code>AggregateValue</code> that
    * contains initial values for all Aggregators.  This is called when there
    * are no values to aggregate AND there are no properties to group by.
    * @return A <code>List</code> of one <code>AggregateValue</code>.  The
    *    <code>Object</code> associated with this <code>AggregateValue</code>
    *    is <code>null</code>.
    */
   private <T> List<AggregateValue<T>> getEmptyAggregateValues()
   {
      List<AggregateValue<T>> aggValues = new ArrayList<AggregateValue<T>>(1);
      List<AggregateFunction> aggList = new ArrayList<AggregateFunction>(myAggregators.size());
      int aggSize = myAggregators.size();
      for (int a = 0; a < aggSize; a++)
      {
         AggregateFunction archetype = myAggregators.get(a);
         aggList.add(Aggregator.getAggregator(archetype));
      }
      AggregateValue<T> aggValue = new AggregateValue<T>((T) null);
      for (int a = 0; a < aggSize; a++)
      {
         AggregateFunction agg = aggList.get(a);
         agg.init();
         aggValue.setAggregateValue(agg, agg.terminate());
         agg.setInUse(false);
      }
      aggValues.add(aggValue);
      return aggValues;
   }

   /**
    * Perform super-aggregation on <code>AggregateValues</code>.  These represent
    * normal aggregate results, and they are otherwise already done.  Any
    * super-aggregate values are added to the given <code>List</code> of
    * <code>AggregateValues</code>.  It is expected that each
    * <code>AggregateValue</code> has been assigned a reference to the
    * <code>List</code> of <code>Aggregators</code> that will be used to
    * calculate super-aggregate results.  They are released after the
    * calculations are complete.
    * @param aggValues A <code>List</code> of <code>AggregateValues</code>.
    * @param <T> The type of object to super-aggregate.
    */
   private <T> void getSuperAggregateValues(List<AggregateValue<T>> aggValues)
   {
      if (DEBUG)
      {
         System.err.println("A.gSAV begin");
         System.err.println("Grouping Sets:");
         for (List<Integer> groupingSet : myGroupingSets)
         {
            System.err.println(groupingSet);
         }
      }
      boolean includeOrigAggValues = false;
      // Define the chain of grouping sets, which traces its origin back to the
      // original aggValues passed in (-1).
      Map<Integer, Integer> chainedGroupingSets = new HashMap<Integer, Integer>();
      int numGroupingSets = myGroupingSets.size();
      int numProperties = myProperties.size();
      
      for (int i = 0; i < numGroupingSets; i++)
      {
         List<Integer> groupingSet = myGroupingSets.get(i);
         int j;
         for (j = i - 1; j >= 0; j--)
         {
            List<Integer> candidateSet = myGroupingSets.get(j);
            if (candidateSet.containsAll(groupingSet))
            {
               chainedGroupingSets.put(i, j);
               if (DEBUG)
                  System.err.println("  put(" + i + ", " + j + ")");
               break;
            }
         }
         // Must rely on original agg values.
         if (j == -1)
         {
            if (DEBUG)
               System.err.println("  put(" + i + ", -1)");
            chainedGroupingSets.put(i, -1);
         }
      }
      int start = 0;
      if (myGroupingSets.get(0).size() == numProperties)
      {
         // The grouping set consisting of all properties is found.
         // Include the original agg values in the final result set.
         includeOrigAggValues = true;
         start = 1;
      }
      if (DEBUG)
         System.err.println("includeOrigAggValues is " + includeOrigAggValues);
      // Walk through the grouping sets, using a prior grouping set's
      // super-aggregate values to create the current grouping set's
      // super-aggregate values.
      Map<Integer, List<AggregateValue<T>>> aggValuesByGroupingSet = new TreeMap<Integer, List<AggregateValue<T>>>();
      if (includeOrigAggValues)
      {
         aggValuesByGroupingSet.put(0, aggValues);
      }
      for (int g = start; g < numGroupingSets; g++)
      {
         int prevIndex = chainedGroupingSets.get(g);
         if (DEBUG)
            System.err.println("  get(" + g + ") = " + prevIndex);
         List<AggregateValue<T>> useValues;
         if (prevIndex == -1)
            useValues = aggValues;
         else
            useValues = aggValuesByGroupingSet.get(prevIndex);

         // Get list of properties to super-aggregate by and sort the aggregate values.
         List<Integer> groupingSet = myGroupingSets.get(g);
         List<String> properties = new ArrayList<String>();
         for (int propIndex : groupingSet)
         {
            properties.add(myProperties.get(propIndex));
         }
         Comparator<AggregateValue<T>> comparator = new AggregateValuePropertiesComparator<T>(properties);
         if (DEBUG)
            System.err.println("  Sorting on " + properties.toString());
         Collections.sort(useValues, comparator);

         if (DEBUG)
         {
            System.err.println("  useValues: ");
            for (AggregateValue<T> useValue : useValues)
            {
               System.err.println(useValue);
            }
         }

         List<AggregateValue<T>> superAggValues = new ArrayList<AggregateValue<T>>();
         int aggSize = myAggregators.size();
         int startIndex = 0;
         int endIndex;
         int listsize = useValues.size();
         while (startIndex < listsize)
         {
            if (DEBUG)
               System.err.println("    startIndex is " + startIndex);
            AggregateValue<T> superAggValue = new AggregateValue<T>(useValues.get(startIndex));
            endIndex = Aggregations.indexOfLastMatching(useValues, comparator, startIndex);
            if (DEBUG)
               System.err.println("    endIndex is " + endIndex);

            List<AggregateFunction> superAggList = getAggregatorsList();

            for (int a = 0; a < aggSize; a++)
            {
               AggregateFunction agg = superAggList.get(a);
               agg.init();
            }
            for (int i = startIndex; i <= endIndex; i++)
            {
               AggregateValue<T> baseValue = useValues.get(i);
               List<AggregateFunction> baseAggs = baseValue.retrieveAggregators();
               for (int a = 0; a < aggSize; a++)
               {
                  AggregateFunction agg = superAggList.get(a);
                  agg.merge(baseAggs.get(a));
               }
            }
            for (int a = 0; a < aggSize; a++)
            {
               AggregateFunction agg = superAggList.get(a);
               superAggValue.setAggregateValue(agg, agg.terminate());
            }
            superAggValue.assignAggregators(superAggList);
            superAggValue.assignPropsAndGroupingSet(myProperties, groupingSet);
            superAggValues.add(superAggValue);

            // Setup for next loop (if any).
            startIndex = endIndex + 1;
         }
         if (DEBUG)
         {
            System.err.println("  superAggValues created for grouping set # " + g);
            for (AggregateValue<T> superAggValue : superAggValues)
            {
               System.err.println(superAggValue);
            }
         }
         aggValuesByGroupingSet.put(g, superAggValues);
      }
      // Super-aggregation is done.

      // Cleanup all the assigned Aggregators.
      for (AggregateValue aggValue : aggValues)
      {
         aggValue.releaseAggregators();
      }
      for (int groupingSetIndex : aggValuesByGroupingSet.keySet())
      {
         for (AggregateValue superAggValue : aggValuesByGroupingSet.get(groupingSetIndex))
         {
            superAggValue.releaseAggregators();
         }
      }

      // Manipulate the passed in aggregate values to reflect the proper
      // aggregates (if any) and all super-aggregates.
      if (!includeOrigAggValues)
      {
         aggValues.clear();
      }

      // Add in all super-aggregate values.
      for (int groupingSetIndex : aggValuesByGroupingSet.keySet())
      {
         // Don't include orig agg values again!
         if (groupingSetIndex == 0 && includeOrigAggValues)
            continue;

         List<AggregateValue<T>> groupAggValues = aggValuesByGroupingSet.get(groupingSetIndex);

         if (DEBUG)
         {
            System.err.println("For groupingSetIndex " + groupingSetIndex +
               ", adding ");
            for (AggregateValue<T> groupAggValue : groupAggValues)
            {
               System.err.println(groupAggValue);
            }
         }

         aggValues.addAll(groupAggValues);
      }
   }

   /**
    * This <code>Builder</code> class follows the "Builder" pattern to create
    * an <code>Aggregation</code> object.
    */
   public static class Builder
   {
      private List<AggregateFunction> myAggregators;
      private List<String> myProperties;
      private int myParallelism;
      private boolean amIUsingMsd;
      private List<List<Integer>> myGroupingSets;
      private boolean amIUsingSuperAggregation;

      /**
       * Constructs a <code>Builder</code> with no aggregators, no properties,
       * parallelism of 1, and not using multiset discrimination.
       */
      public Builder()
      {
         myAggregators = null;
         myProperties = null;
         myParallelism = 1;
         amIUsingMsd = false;
         myGroupingSets = null;
         amIUsingSuperAggregation = false;
      }

      /**
       * Sets the <code>List</code> of <code>AggregateFunction</code> to use.  The
       * <code>AggregateFunctions</code> define which aggregate operatons to perform.
       * @param aggregators A <code>List</code> of <code>AggregateFunctions</code>.
       *    AggregateFunctions can be created in two ways: direct instantiation, or by
       *    using the factory method.
       *    <ul>
       *        <li><code>AggregateFunction agg = new SumAggregator("value");</code>
       *        <li><code>AggregateFunction agg = Aggregator.getAggregator("Sum(value)");</code>
       *    </ul>
       * @return This <code>Builder</code>.
       * @see Aggregator
       * @see AggregateFunction
       */
      public Builder setAggregators(List<AggregateFunction> aggregators)
      {
         myAggregators = aggregators;
         return this;
      }

      /**
       * Sets the <code>List</code> of properties.  jAgg uses this list of
       * properties to "group by" the different property values.  For optional
       * super aggregation, this is the list to which the 0-based indices
       * reference the properties.
       * @param properties The <code>List&lt;String&gt;</code> of properties to
       *    "group by".  If this call is omitted, then values to be aggregated
       *    must be <code>Comparable</code>.
       * @return This <code>Builder</code>.
       */
      public Builder setProperties(List<String> properties)
      {
         myProperties = properties;
         amIUsingSuperAggregation = false;
         createDefaultGroupingSet();
         return this;
      }

      /**
       * Sets the degree of parallelism.
       * @param parallelism The degree of parallelism desired; if less than 1,
       *    then 1 will be used; if more than 1, then minimum of this number and
       *    the number of processors available to the JVM will be used, as
       *    determined by <code>Runtime.availableProcessors</code>.
       * @return This <code>Builder</code>.
       * @see Runtime#availableProcessors
       */
      public Builder setParallelism(int parallelism)
      {
         if (parallelism < 1)
         parallelism = 1;
         if (parallelism > 1)
         {
            int numProcessors = Runtime.getRuntime().availableProcessors();
            parallelism = (parallelism > numProcessors) ? numProcessors : parallelism;
         }
         myParallelism = parallelism;
         return this;
      }

      /**
       * Sets whether multiset discrimination is to be used to distinguish sets
       * of objects with shared attributes.  If not called, then the Builder
       * defaults to <code>false</code> (use sorting).
       * @param useMsd Whether multiset discrimination is to be used.  This is
       *    ignored if the objects to be aggregated are being distinguished
       *    using the fact that they are <code>Comparable</code>.
       * @return This <code>Builder</code>.
       */
      public Builder setUseMsd(boolean useMsd)
      {
         amIUsingMsd = useMsd;
         return this;
      }

      /**
       * Sets the grouping sets to use.  Each list contains a list of integer
       * references, ranging from 0 to <em>n</em> - 1, if <em>n</em> is the
       * number of properties already specified through
       * <code>setProperties</code>.  If this is not called, then it
       * defaults to one grouping set consisting of all properties, e.g.
       * <code>{{0, 1, ..., n - 1}}</code>.
       * @param groupingSets A <code>List</code> of <code>Lists</code> of
       *    integer references.  E.g. If there are 3 properties, then the
       *    following examples would be valid grouping sets:
       * <ul>
       * <li><code>{{}}</code> Group by no properties.  (The empty grouping set
       *    produces grand totals.)
       * <li><code>{{0, 1, 2}}</code> The default: "group by all".
       * <li><code>{{}, {0}, {1}, {2}, {0, 1}, {0, 2}, {1, 2}, {0, 1, 2}}</code>
       *    Create a data cube.
       * <li><code>{{1, 2}, {0, 1, 2}}</code> Create a rollup on the first
       *    (index 0) property.
       * <li><code>{{1}, {0, 2}, {0, 1, 2}}</code> Other combinations are
       *    possible.
       * </ul>
       * @return This <code>Builder</code>.
       * @throws IllegalArgumentException If the "group by" properties have not
       *    been set yet; if any index is outside the range from 0 to
       *    <em>n</em> -1, where <em>n</em> is the number of "group by"
       *    properties; if any index is repeated within the same grouping set;
       *    if any grouping set is a duplicate of any other, even if the fields
       *    are in a different order.
       * @see #setProperties
       */
      public Builder setGroupingSets(List<List<Integer>> groupingSets)
      {
         if (myProperties == null && groupingSets != null)
         {
            throw new IllegalArgumentException("Grouping sets without group-by properties.");
         }
         if (groupingSets == null || groupingSets.isEmpty())
         {
            createDefaultGroupingSet();
            return this;
         }
         myGroupingSets = validateGroupingSets(groupingSets);

         return this;
      }

      /**
       * <p>Sets the rollup properties to use.  The list contains a list of
       * integer references, ranging from 0 to <em>n</em> - 1, if <em>n</em> is
       * the number of properties already specified through
       * <code>setProperties</code>.  This produces a set of
       * grouping sets that together form a "rollup" of the referenced
       * properties.  E.g.
       * <code>setProperties("prop0", "prop1", "prop2", "prop3", "prop4")
       *    .setRollup({1, 2, 3})</code> yields grouping sets
       * <code>{{0, 1, 2, 3, 4}, {0, 1, 2, 4}, {0, 1, 4}, {0, 4}</code>.</p>
       * <p>This method acts as if all rollup grouping set combinations are
       * found, then they are passed to <code>setGroupingSets</code>.</p>
       * @param rollup A <code>List</code> of integer references.  Grouping
       *    sets are created that each contain all properties not referenced
       *    here, and each individual grouping set contains a different number
       *    of referenced properties <em>in order</em>.
       * @return This <code>Builder</code>.
       * @throws IllegalArgumentException If any index is specified more than
       *    once; if any index is out of range from 0 to <em>n</em> -1, where
       *    <em>n</em> is the number of "group by" properties.
       * @see #setGroupingSets
       * @see #setProperties
       */
      public Builder setRollup(List<Integer> rollup)
      {
         return setRollups(Collections.singletonList(rollup));
      }

      /**
       * <p>Sets multiple sets of rollup properties to use.  Each list contains a
       * list of integer references, ranging from 0 to <em>n</em> - 1, if
       * <em>n</em> is the number of properties already specified through
       * <code>setProperties</code>.  This produces a set of
       * grouping sets that together form "rollups" of the referenced
       * properties.  E.g.
       * <code>setProperties("prop0", "prop1", "prop2", "prop3", "prop4")
       *    .setRollups({{0, 1}, {2, 3})</code> yields grouping sets
       * <code>{{0, 1, 2, 3, 4}, {0, 2, 3, 4}, {0, 1, 2, 4}, {2, 3, 4},
       * {0, 2, 4}, {0, 1, 4}, {2, 4}, {0, 4}, {4}}</code>.  Grouping sets for
       * the first rollup are created, and each grouping set is used to create
       * any subsequent rollups.  Notice how a property reference not listed
       * ("4") exists in every grouping set.</p>
       * <p>This method acts as if all rollup grouping set combinations are
       * found, then they are passed to <code>setGroupingSets</code>.</p>
       * @param rollups A <code>List</code> of integer references.  Grouping
       *    sets are created that each contain all properties not referenced
       *    here, and each individual grouping set contains a different number
       *    of referenced properties <em>in order</em>.
       * @return This <code>Builder</code>.
       * @throws IllegalArgumentException If any index is specified more than
       *    once; if any index is out of range from 0 to <em>n</em> -1, where
       *    <em>n</em> is the number of "group by" properties.
       * @see #setGroupingSets
       * @see #setProperties
       */
      public Builder setRollups(List<List<Integer>> rollups)
      {
         validateSpecialSets(rollups);

         List<List<Integer>> groupingSets = new ArrayList<List<Integer>>();
         groupingSets.add(createDefaultGroupingSet());
         findRollups(groupingSets, rollups);
         return setGroupingSets(groupingSets);
      }

      /**
       * <p>Sets the set of cube properties to use.  The list contains a
       * integer references, ranging from 0 to <em>n</em> - 1, if <em>n</em> is
       * the number of properties already specified through
       * <code>setProperties</code>.  This produces a set of
       * grouping sets that together form a "cube" of the referenced
       * properties.  E.g.
       * <code>setProperties("prop0", "prop1", "prop2", "prop3", "prop4")
       *    .setCube({{0, 1, 3})</code> yields grouping sets
       * <code>{{0, 1, 2, 3, 4}, {0, 1, 2, 4}, {0, 2, 3, 4}, {1, 2, 3, 4},
       * {0, 2, 4}, {1, 2, 4}, {2, 3, 4}, {2, 4}}</code>.  Notice how all
       * property references not listed ("2, 4") exist in every grouping set.</p>
       * <p>This method acts as if all cube grouping set combinations are
       * found, then they are passed to <code>setGroupingSets</code>.</p>
       * <p>Because <code>cube(x, y)</code> is the same as
       * <code>cube(x), cube(y)</code>, this method only accepts a simple list,
       * instead of a list of lists.
       * @param cube A <code>List</code> of integer references.  Grouping
       *    sets are created that each contain all properties not referenced
       *    here, and each individual grouping set contains a different number
       *    of referenced properties <em>in order</em>.
       * @return This <code>Builder</code>.
       * @throws IllegalArgumentException If any index is specified more than
       *    once; if any index is out of range from 0 to <em>n</em> -1, where
       *    <em>n</em> is the number of "group by" properties.
       * @see #setGroupingSets
       * @see #setProperties
       */
      public Builder setCube(List<Integer> cube)
      {
         validateSpecialSets(Collections.singletonList(cube));

         List<List<Integer>> groupingSets = new ArrayList<List<Integer>>();
         groupingSets.add(createDefaultGroupingSet());
         findCubes(groupingSets, cube);
         return setGroupingSets(groupingSets);
      }

      /**
       * Build the <code>Aggregation</code> object.
       * @return An <code>Aggregation</code> object that can be used to perform
       *    the actual aggregate calculations.
       * @throws IllegalArgumentException If at least one
       *    <code>AggregateFunction</code> was not supplied with the
       *    <code>setAggregators</code> method.
       * @see #setAggregators
       */
      public Aggregation build()
      {
         if (myAggregators == null || myAggregators.isEmpty())
            throw new IllegalArgumentException("Aggregation.Builder: Must supply at least one AggregateFunction.");
         return new Aggregation(this);
      }

      /**
       * Add all rollup grouping sets to the given list of grouping sets.
       * Initially, it is expected that the first time this is called, that
       * <code>groupingSets</code> contains only the default grouping set.
       * This method walks through all rollup combinations by processing the
       * first rollup set and calling itself via recursion with the rest of the
       * rollups list.
       * @param groupingSets The <code>List</code> of grouping sets.  This is
       *    modified by adding all grouping sets found through rollup
       *    combinations.
       * @param rollups The <code>List</code> of rollup sets.
       */
      private void findRollups(List<List<Integer>> groupingSets, List<List<Integer>> rollups)
      {
         // Ending condition of recursion.
         if (rollups.isEmpty())
            return;
         if (DEBUG)
            System.err.println("A.findRollups");
         // Process the first rollup in the list.
         List<Integer> rollup = rollups.get(0);
         int rollupSize = rollup.size();
         if (DEBUG)
            System.err.println("  rollup: " + rollup);

         List<List<Integer>> newGroupingSets = new ArrayList<List<Integer>>();
         for (int i = rollupSize - 1; i >= 0; i--)
         {
            List<Integer> remove = rollup.subList(i, rollupSize);
            for (List<Integer> groupingSet : groupingSets)
            {
               List<Integer> newGroupingSet = new ArrayList<Integer>(groupingSet);
               newGroupingSet.removeAll(remove);
               if (DEBUG)
                  System.err.println("  i: " + i + " add: " + newGroupingSet);
               newGroupingSets.add(newGroupingSet);
            }
         }
         groupingSets.addAll(newGroupingSets);
         findRollups(groupingSets, rollups.subList(1, rollups.size()));
      }

      /**
       * Add all cube grouping sets to the given list of grouping sets.
       * Initially, it is expected that the first time this is called, that
       * <code>groupingSets</code> contains only the default grouping set.
       * @param groupingSets The <code>List</code> of grouping sets.  This is
       *    modified by adding all grouping sets found through rollup
       *    combinations.
       * @param cube The <code>List</code> of cube integer references.
       */
      private void findCubes(List<List<Integer>> groupingSets, List<Integer> cube)
      {
         if (cube.isEmpty())
            return;
         if (DEBUG)
            System.err.println("A.findCubes: " + cube);

         // Note: This limits cubes to a maximum of 32 properties.
         int numCubes = cube.size();
         int numCombinations = 1 << numCubes;

         List<Integer> defGroupingSet = groupingSets.get(0);
         List<Integer> remove = new ArrayList<Integer>();
         // Don't add the base grouping set (all props) again!
         for (int i = 1; i < numCombinations; i++)
         {
            remove.clear();
            for (int b = 0, bit = 1; b < numCubes; b++, bit <<= 1)
            {
               if ((i & bit) > 0)
                  remove.add(cube.get(b));
            }
            List<Integer> combo = new ArrayList<Integer>(defGroupingSet);
            combo.removeAll(remove);
            if (DEBUG)
               System.err.println("Adding cube grouping set: " + combo);
            groupingSets.add(combo);
         }
      }

      /**
       * Creates the default grouping set, which is used if grouping sets are
       * not explictly stated.  If there are <em>n</em> group-by properties,
       * then the defaulting grouping set is <code>{0, 1, ..., n - 1}</code>.
       * @return The default <code>List</code> of integer references consisting
       *    of references to all properties.
       */
      private List<Integer> createDefaultGroupingSet()
      {
         int numProperties = myProperties.size();
         List<Integer> groupingSet = new ArrayList<Integer>(numProperties);
         for (int i = 0; i < numProperties; i++)
            groupingSet.add(i);
         return groupingSet;
      }

      /**
       * Validates that all integer references are within range and that no
       * reference is repeated, even if it is repeated across special sets.
       * This validation is intended for cube sets and rollup sets.
       * @param specials A <code>List</code> of <code>Lists</code> of integer
       *    references to properties, means for "special" sets such as cubes
       *    and rollups.
       */
      private void validateSpecialSets(List<List<Integer>> specials)
      {
         int numProperties = myProperties.size();
         // Note: This restricts grouping sets to maximum 32 fields.
         int fieldMask = 0;
         for (List<Integer> special : specials)
         {
            for (int field : special)
            {
               if (field < 0 || field >= numProperties)
                  throw new IllegalArgumentException("Grouping set field index out of range: " + field);
               int bit = 1 << field;
               if ((fieldMask & bit) != 0)
                  throw new IllegalArgumentException("Can't specify same field more than once in any cube/rollup set: " + field);
               fieldMask |= bit;
            }
         }
      }

      /**
       * Validates the grouping sets.
       * @param groupingSets A <code>List</code> of grouping sets, which are
       *    <code>Lists</code> of integers.
       * @return Another <code>List</code> of grouping sets, sorted in a
       *    particular order: <code>Lists</code> are sorted by length
       *    descending, and each individual <code>List</code> has its integer
       *    field references sorted in ascending order.
       * @throws IllegalArgumentException If any index is outside the range
       *    from 0 to <em>n</em> -1, where <em>n</em> is the number of "group
       *    by" properties; if any index is repeated within the same grouping
       *    set; if any grouping set is a duplicate of any other, even if the
       *    fields are in a different order.
       */
      private List<List<Integer>> validateGroupingSets(List<List<Integer>> groupingSets)
      {
         int numProperties = myProperties.size();
         List<List<Integer>> groupingSetsCopy = new ArrayList<List<Integer>>(groupingSets);
         for (List<Integer> groupingSet : groupingSetsCopy)
         {
            if (groupingSet.size() != numProperties)
               amIUsingSuperAggregation = true;

            // Note: This restricts grouping sets to maximum 32 fields.
            int fieldMask = 0;
            for (int field : groupingSet)
            {
               if (field < 0 || field >= numProperties)
                  throw new IllegalArgumentException("Grouping set field index out of range: " + field);
               int bit = 1 << field;
               if ((fieldMask & bit) != 0)
                  throw new IllegalArgumentException("Can't specify same field more than once in a grouping set: " + field);
               fieldMask |= bit;
            }
            // Don't allow duplicate grouping sets.
            // Check for same fields in any order.
            for (List<Integer> otherGroupingSet : groupingSetsCopy)
            {
               // Only check the previous sets versus this one.
               if (otherGroupingSet == groupingSet)
               {
                  break;
               }
               // Different size, different sets.
               if (otherGroupingSet.size() != groupingSet.size())
               {
                  continue;
               }
               Set<Integer> fields = new HashSet<Integer>(groupingSet);
               for (int otherField : otherGroupingSet)
               {
                  // Does this set contain the element of the other set?
                  // If not, then they aren't equal.
                  if (!fields.remove(otherField))
                     break;
               }
               if (fields.isEmpty())
               {
                  // Sets with the same contents, even if in different orders,
                  // will reach here.
                  throw new IllegalArgumentException("Duplicate grouping sets found: " +
                     groupingSet.toString() + " and " + otherGroupingSet.toString());
               }
            }
            // Sort each individual list.
            Collections.sort(groupingSet);
         }
         // Sort the list of lists according to a specific order.
         Collections.sort(groupingSetsCopy, new Comparator<List<Integer>>() {
            /**
             * Sort by length of list descending.
             * @param list1 A <code>List</code> of integers.
             * @param list2 A <code>List</code> of integers.
             * @return An integer less than zero, equal to zero, or greater than
             *    zero, depending on whether the first list is longer, as long
             *    as, or shorter than the second list.
             */
            public int compare(List<Integer> list1, List<Integer> list2)
            {
               return list2.size() - list1.size();
               // The specific elements matter here, e.g. {0, 1} vs. {0, 2} vs.
               // {1, 2} do not matter.
            }
         });
         return groupingSetsCopy;
      }
   }  // End of Builder class
}
