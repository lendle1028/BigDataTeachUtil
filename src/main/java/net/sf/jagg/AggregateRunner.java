package net.sf.jagg;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import net.sf.jagg.model.AggregateValue;
import net.sf.jagg.model.PositionedAggregatorList;

/**
 * <p>Gets aggregate values for a sub-range of a <code>List</code> of Objects.
 * Running Time: <em>O(n * a)</em>, where <em>n</em> is the number of
 * items to process (<code>end - start + 1</code>), and <em>a</em> is the
 * number of aggregators desired.  That assumes that property access in
 * <code>T</code> is constant-time.</p>
 * <p>This used to be a static class within the <code>Aggregations</code>
 * utility class, but it was extracted for version 0.7.0.</p>
 *
 * @author Randy Gettman
 * @since 0.7.0
 */
class AggregateRunner<T> implements Callable<PositionedAggregatorList<T>>
{
   private static final boolean DEBUG = false;

   private List<AggregateFunction> myAggregators;
   private List<T> myValuesList;
   private Comparator<? super T> myComparator;
   private int myStart;
   private int myEnd;
   private int myPosition;
   private boolean amIUsingSuperAggregation;
   private List<String> myProperties;

   /**
    * Construct an <code>AggregateRunner</code> that in a separate
    * <code>Thread</code> will create a
    * <code>PositionedAggregatorList</code>.
    *
    * @param aggregators The <code>List</code> of <code>AggregateFunctions</code>.
    * @param valuesList The list of values to aggregate.
    * @param pos The position in the order, as a 0-based index.
    * @param comparator A <code>Comparator</code> used to identify runs of
    *    equivalent T objects.
    * @param start The start index.
    * @param end The end index.
    * @param useSuperAggregation If using super-aggregation, then this must
    *    store all <code>AggregateFunctions</code> in each finished
    *    <code>AggregateValue</code> for user later in super-aggregation.
    * @param properties The <code>List</code> of properties.
    */
   public AggregateRunner(List<AggregateFunction> aggregators, List<T> valuesList, int pos,
      Comparator<? super T> comparator, int start, int end, boolean useSuperAggregation,
      List<String> properties)
   {
      myAggregators = aggregators;
      myValuesList = valuesList;
      myComparator = comparator;
      myStart = start;
      myEnd = end;
      myPosition = pos;
      amIUsingSuperAggregation = useSuperAggregation;
      myProperties = properties;
      if (DEBUG)
      {
         System.err.println("AggregateRunner: " + start + "-" + end);
         System.err.println("AggregateRunner: " + start + "-" + end);
      }
   }

   /**
    * <p>Runs through a section of the values list from start to end, getting
    * additional <code>AggregateFunctions</code> that are necessary, initializing
    * them, and iterating through all values from start to end.</p>
    * <p>If this completes an entire run for one or more objects, and
    * super-aggregation will be used, then the associated <code>List</code> of
    * <code>AggregateFunctions</code> is stored in the associated
    * <code>AggregateValue</code>.</p>
    *
    * @return A <code>PositionedAggregatorList</code>.
    */
   public PositionedAggregatorList<T> call()
   {
      int numProperties = (myProperties != null) ? myProperties.size() : 0;
      List<Integer> defaultGroupingSet = new ArrayList<Integer>(numProperties);
      for (int i = 0; i < numProperties; i++)
         defaultGroupingSet.add(i);
      PositionedAggregatorList<T> pal = new PositionedAggregatorList<T>(myPosition);
      int startIndex = myStart;
      // Don't let endIndex be greater than myEnd.
      int endIndex = Aggregations.indexOfLastMatching(myValuesList, myComparator, startIndex, myEnd);

      // Initial run.
      T currObject = myValuesList.get(startIndex);
      int aggSize = myAggregators.size();
      List<AggregateFunction> initAggList = getAggregatorsList();
      for (int i = startIndex; i <= endIndex; i++)
      {
         T value = myValuesList.get(i);

         for (int a = 0; a < aggSize; a++)
         {
            AggregateFunction agg = initAggList.get(a);
            agg.iterate(value);
         }
      }
      // First run is to be left unfinished (it may be merged into the
      // ending run of a previous PAL later).
      // If the first run is the only run, then we're done.
      pal.setInitialList(currObject, initAggList);
      if (endIndex == myEnd)
         return pal;
      // Not off the end yet?  Continue with other Aggregations with
      // different values of T.
      startIndex = endIndex + 1;
      List<AggregateValue<T>> aggValues = new ArrayList<AggregateValue<T>>();
      List<AggregateFunction> currAggList = getAggregatorsList();
      boolean currInEndingAggList = false;
      while (startIndex <= myEnd)
      {
         currObject = myValuesList.get(startIndex);
         // Don't let endIndex be greater than myEnd.
         endIndex = Aggregations.indexOfLastMatching(myValuesList, myComparator, startIndex, myEnd);

         // If no super-aggregation, then use the same Aggregators throughout
         // the entire process; they can be reused each loop.
         // If super-aggregation, then use different Aggregators in each loop.
         if (amIUsingSuperAggregation && startIndex > 0)
            currAggList = getAggregatorsList();
         else
         {
            for (int a = 0; a < aggSize; a++)
            {
               AggregateFunction agg = currAggList.get(a);
               agg.init();
            }
         }
         for (int i = startIndex; i <= endIndex; i++)
         {
            T value = myValuesList.get(i);
            for (int a = 0; a < aggSize; a++)
            {
               AggregateFunction agg = currAggList.get(a);
               agg.iterate(value);
            }
         }
         if (endIndex == myEnd)
         {
            // Last run is to be left unfinished (it may be merged with the
            // initial run of another PAL later).
            pal.setEndingList(currObject, currAggList);
            currInEndingAggList = true;
         }
         else
         {
            // Note that we can be sure HERE that no other Thread is
            // working on this particular T object (currObject).  We can
            // generate the full AggregateValue<T> here.
            AggregateValue<T> aggValue = new AggregateValue<T>(currObject);
            for (int a = 0; a < aggSize; a++)
            {
               AggregateFunction agg = currAggList.get(a);
               Object result = agg.terminate();
               agg.setInUse(false);
               aggValue.setAggregateValue(agg, result);
            }
            if (myProperties != null)
               aggValue.assignPropsAndGroupingSet(myProperties, defaultGroupingSet);
            // Store for super-aggregation later.
            if (amIUsingSuperAggregation)
               aggValue.assignAggregators(currAggList);

            aggValues.add(aggValue);
         }
         // Setup for next loop (if any).
         startIndex = endIndex + 1;
      }
      // No longer using the Aggregators if we don't need them for
      // super-aggregation and we don't need them in the ending list.
      if (!amIUsingSuperAggregation && !currInEndingAggList)
      {
         for (int a = 0; a < aggSize; a++)
         {
            AggregateFunction agg = currAggList.get(a);
            agg.setInUse(false);
         }
      }
      pal.setMiddleAggValues(aggValues);

      return pal;
   }

   /**
    * Helper function to create a new <code>List</code> of
    * <code>AggregateFunctions</code>, given a list of archetypes.  Each newly
    * acquired <code>AggregateFunction</code> is initialized by calling
    * <code>init()</code> before adding it to the returned list.
    * @return A <code>List</code> of <code>AggregateFunctions</code>.
    * @since 0.7.0
    */
   private List<AggregateFunction> getAggregatorsList()
   {
      int aggSize = myAggregators.size();
      List<AggregateFunction> aggList = new ArrayList<AggregateFunction>(aggSize);
      for (int a = 0; a < aggSize; a++)
      {
         AggregateFunction archetype = myAggregators.get(a);
         AggregateFunction agg = Aggregator.getAggregator(archetype);
         agg.init();
         aggList.add(agg);
      }
      return aggList;
   }
}
