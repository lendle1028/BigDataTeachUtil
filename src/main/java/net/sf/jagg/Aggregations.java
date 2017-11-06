package net.sf.jagg;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.sf.jagg.model.AggregateValue;
import net.sf.jagg.model.PositionedAggregatorList;

/**
 * <p>This class is a utility class that represents an alternate API for some
 * jAgg operations.  Before 0.7.0, this class was the sole API.  As of 0.7.0,
 * this class no longer represents the complete API.  For the operations it
 * does support, it defers to the new API (<code>Aggregation</code> class).</p>
 * <p>It supports "group by" functionality that allows aggregate
 * functions, or <code>Aggregators</code>, to return aggregate values
 * over a <code>List</code> of <code>Objects</code>.</p>
 * <p>It can aggregate over <code>Comparable</code> objects or, with a supplied
 * list of properties, any objects.  Parallel execution is also avaialable.</p>
 * <p>To use super-aggregation, e.g. rollups, cubes, or grouping sets, please
 * use the <code>Aggregation</code> class (and its <code>Builder</code> class)
 * directly.</p>
 *
 * @author Randy Gettman
 * @since 0.1.0
 * @see Aggregation
 * @see Aggregation.Builder
 */
public class Aggregations
{
   // Don't instantiate this class!
   private Aggregations() {}

   /**
    * Perform one or more aggregate operations on a <code>List&lt;T&gt;</code>.
    * <code>T</code> should have a "natural ordering", that is, it must be
    * <code>Comparable</code>, and <code>compareTo</code> defines the
    * properties with which to "group by" with its consideration of different
    * properties to determine order.
    *
    * @param <T> The object type to aggregate.
    * @param values The <code>List&lt;T&gt;</code> of objects to aggregate.
    * @param aggregators The <code>List</code> of <code>AggregateFunction</code> to
    *    apply to <code>values</code>.
    * @return A <code>List&lt;AggregateValue&lt;T&gt;&gt;</code>.
    * @see Aggregator
    */
   public static <T extends Comparable<? super T>> List<AggregateValue<T>> groupBy(
      List<T> values, List<AggregateFunction> aggregators)
   {
      return new Aggregation.Builder().setAggregators(aggregators).build().groupByComparable(values);
   }

   /**
    * Perform one or more aggregate operations on a <code>List&lt;T&gt;</code>.
    * <code>T</code> should have a "natural ordering", that is, it must be
    * <code>Comparable</code>, and <code>compareTo</code> defines the
    * properties with which to "group by" with its consideration of different
    * properties to determine order.  This version accepts an integer argument
    * corresponding to the parallelization desired.
    *
    * @param <T> The object type to aggregate.
    * @param values The <code>List&lt;T&gt;</code> of objects to aggregate.
    * @param aggregators The <code>List</code> of <code>AggregateFunctions</code> to
    *    apply to <code>values</code>.
    * @param parallelism The degree of parallelism desired; if less than 1,
    *    then 1 will be used; if more than 1, then minimum of this number and
    *    the number of processors available to the JVM will be used, as
    *    determined by <code>Runtime.availableProcessors</code>.
    * @return A <code>List&lt;AggregateValue&lt;T&gt;&gt;</code>.
    * @see Aggregator
    * @see Runtime#availableProcessors
    */
   public static <T extends Comparable<? super T>> List<AggregateValue<T>> groupBy(
      List<T> values, List<AggregateFunction> aggregators, int parallelism)
   {
      return new Aggregation.Builder().setAggregators(aggregators).setParallelism(parallelism)
         .build().groupByComparable(values);
   }

   /**
    * Perform one or more aggregate operations on a <code>List&lt;T&gt;</code>.
    * <code>T</code> does not need to be <code>Comparable</code>.  The given
    * properties list defines the properties with which to "group by".  This
    * method sorts a copy of the list based on the list of properties.
    *
    * @param <T> The object type to aggregate.
    * @param values The <code>List&lt;T&gt;</code> of objects to aggregate.
    * @param properties The <code>List&lt;String&gt;</code> of properties to
    *    "group by".
    * @param aggregators The <code>List</code> of <code>AggregateFunctions</code> to
    *    apply to <code>values</code>.
    * @return A <code>List&lt;AggregateValue&lt;T&gt;&gt;</code>
    * @see Aggregator
    */
   public static <T> List<AggregateValue<T>> groupBy(List<T> values,
      List<String> properties, List<AggregateFunction> aggregators)
   {
      return new Aggregation.Builder().setAggregators(aggregators).setProperties(properties)
         .build().groupBy(values);
   }

   /**
    * Perform one or more aggregate operations on a <code>List&lt;T&gt;</code>.
    * <code>T</code> does not need to be <code>Comparable</code>.  The given
    * properties list defines the properties with which to "group by".  This
    * method allows a choice to use multiset discrimination to group the
    * elements.
    *
    * @param <T> The object type to aggregate.
    * @param values The <code>List&lt;T&gt;</code> of objects to aggregate.
    * @param properties The <code>List&lt;String&gt;</code> of properties to
    *    "group by".
    * @param aggregators The <code>List</code> of <code>AggregateFunctions</code> to
    *    apply to <code>values</code>.
    * @param useMsd <code>true</code> to use multiset discrimination,
    *    <code>false</code> to only use sorting.  If multiset discrimination
    *    fails, then this method falls back on sorting.
    * @return A <code>List&lt;AggregateValue&lt;T&gt;&gt;</code>
    * @see Aggregator
    * @since 0.5.0
    */
   public static <T> List<AggregateValue<T>> groupBy(List<T> values,
      List<String> properties, List<AggregateFunction> aggregators, boolean useMsd)
   {
      return new Aggregation.Builder().setAggregators(aggregators).setProperties(properties)
         .setUseMsd(useMsd).build().groupBy(values);
   }

   /**
    * Perform one or more aggregate operations on a <code>List&lt;T&gt;</code>.
    * <code>T</code> does not need to be <code>Comparable</code>.  The given
    * properties list defines the properties with which to "group by".  This
    * version accepts an integer argument corresponding to the parallelization
    * desired.  This method sorts a copy of the list based on the list of
    * properties.
    *
    * @param <T> The object type to aggregate.
    * @param values The <code>List&lt;T&gt;</code> of objects to aggregate.
    * @param properties The <code>List&lt;String&gt;</code> of properties to
    *    "group by".
    * @param aggregators The <code>List</code> of <code>AggregateFunctions</code> to
    *    apply to <code>values</code>.
    * @param parallelism The degree of parallelism desired; if less than 1,
    *    then 1 will be used; if more than 1, then minimum of this number and
    *    the number of processors available to the JVM will be used, as
    *    determined by <code>Runtime.availableProcessors</code>.
    * @return A <code>List&lt;AggregateValue&lt;T&gt;&gt;</code>
    * @see Aggregator
    * @see Runtime#availableProcessors
    */
   public static <T> List<AggregateValue<T>> groupBy(List<T> values,
      List<String> properties, List<AggregateFunction> aggregators, int parallelism)
   {
      return new Aggregation.Builder().setAggregators(aggregators).setProperties(properties)
         .setParallelism(parallelism).build().groupBy(values);
   }

   /**
    * Perform one or more aggregate operations on a <code>List&lt;T&gt;</code>.
    * <code>T</code> does not need to be <code>Comparable</code>.  The given
    * properties list defines the properties with which to "group by".  This
    * version accepts an integer argument corresponding to the parallelization
    * desired.  This method allows a choice to use multiset discrimination to
    * group the elements.
    *
    * @param <T> The object type to aggregate.
    * @param values The <code>List&lt;T&gt;</code> of objects to aggregate.
    * @param properties The <code>List&lt;String&gt;</code> of properties to
    *    "group by".
    * @param aggregators The <code>List</code> of <code>AggregateFunctions</code> to
    *    apply to <code>values</code>.
    * @param parallelism The degree of parallelism desired; if less than 1,
    *    then 1 will be used; if more than 1, then minimum of this number and
    *    the number of processors available to the JVM will be used, as
    *    determined by <code>Runtime.availableProcessors</code>.
    * @param useMsd <code>true</code> to use multiset discrimination,
    *    <code>false</code> to only use sorting.  If multiset discrimination
    *    fails, then this method falls back on sorting.
    * @return A <code>List&lt;AggregateValue&lt;T&gt;&gt;</code>
    * @see Aggregator
    * @see Runtime#availableProcessors
    * @since 0.5.0
    */
   public static <T> List<AggregateValue<T>> groupBy(List<T> values,
      List<String> properties, List<AggregateFunction> aggregators, int parallelism,
      boolean useMsd)
   {
      return new Aggregation.Builder().setAggregators(aggregators).setProperties(properties)
         .setParallelism(parallelism).setUseMsd(useMsd).build().groupBy(values);
   }

   private static final boolean DEBUG = false;

   /**
    * <p>Merge <code>Lists</code> of <code>PositionedAggregatorLists</code>, by
    * taking the following structure and merging and terminating any unfinished
    * <code>Aggregators</code>:
    * </p>
    * <p>
    * <code>
    * listOfPals[0] {initObject: T, initAggregators: List&lt;Aggregator&gt;,
    *    midAggValues: List&lt;AggregateValue&lt;T&gt;&gt;,
    *    endingObject: T, endingAggregators; List&lt;Aggregator&gt;}
    * listOfPals[1] {initObject: T, initAggregators: List&lt;Aggregator&gt;,
    *    midAggValues: List&lt;AggregateValue&lt;T&gt;&gt;,
    *    endingObject: T, endingAggregators; List&lt;Aggregator&gt;}
    * ...
    * listOfPals[n - 1] {initObject: T, initAggregators: List&lt;Aggregator&gt;,
    *    midAggValues: List&lt;AggregateValue&lt;T&gt;&gt;,
    *    endingObject: T, endingAggregators; List&lt;Aggregator&gt;}
    * </code>
    * </p>
    * <p>Above, this will terminate all <code>AggregateFunctions</code> in the initial
    * list of the first <code>PositionedAggregatorLists</code> and create the
    * first <code>AggregateValue</code>.  Then it will include all middle
    * <code>AggregateValues</code>, which have already been calculated. On the
    * borders between <code>PositionedAggregatorLists</code>, it will determine
    * if the ending object of one is equal to the initial object of the next.
    * If so, it will merge the <code>AggregateFunctions</code> before creating one
    * <code>AggregateValue</code>, else it will create separate
    * <code>AggregateValues</code>.  It will determine if the same object is
    * represented in multiple <code>PositionedAggregatorLists</code>, and merge
    * them all together.  Finally, it will terminate all
    * <code>AggregateFunctions</code> in the ending list of the last
    * <code>PositionedAggregatorLists</code> and create the last
    * <code>AggregateValue</code>.</p>
    * <p>If super-aggregation will occur, then this will store the associated
    * <code>List</code> of <code>Aggregators</code> with each completed
    * <code>AggregateValue</code>.</p>
    * @param listOfPals A <code>List</code> of
    *    <code>PositionedAggregatorLists</code>.
    * @param comparator A <code>Comparator</code> of T values.
    * @param useSuperAggregation If true, store the list of aggregators with
    *    each <code>AggregateValue</code>, instead of setting them as not used.
    * @param properties The <code>List</code> of properties.
    * @return A merged <code>List</code> (of one item) of <code>Lists</code> of
    *    <code>AggregateFunctions</code>.
    */
   public static <T> List<AggregateValue<T>> mergeLists(List<PositionedAggregatorList<T>> listOfPals,
      Comparator<? super T> comparator, boolean useSuperAggregation, List<String> properties)
   {
      int numProperties = (properties != null) ? properties.size() : 0;
      List<Integer> defaultGroupingSet = new ArrayList<Integer>(numProperties);
      for (int i = 0; i < numProperties; i++)
         defaultGroupingSet.add(i);
      List<AggregateValue<T>> aggValues = new ArrayList<AggregateValue<T>>();
      PositionedAggregatorList<T> prev = listOfPals.get(0);
      List<AggregateFunction> prevAggsList;
      T prevObject;
      if (prev.getEndingAggList() == null)
      {
         if (DEBUG)
         {
            System.err.println("Case 1.1: Init only");
         }
         // 1.1 Initial only.
         // Nothing to be terminated yet.
         // Will merge and/or terminate the INITIAL list later.
         prevObject = prev.getInitialObject();
         prevAggsList = prev.getInitialAggList();
      }
      else
      {
         if (DEBUG)
         {
            System.err.println("Case 1.2: Init/End");
         }
         // 1.2 Init/End.
         // Terminate the first PAL's init agg list.
         List<AggregateFunction> aggs = prev.getInitialAggList();
         int aggSize = aggs.size();
         T initObject = prev.getInitialObject();
         AggregateValue<T> firstValue = new AggregateValue<T>(initObject);
         for (int a = 0; a < aggSize; a++)
         {
            AggregateFunction agg = aggs.get(a);
            firstValue.setAggregateValue(agg, agg.terminate());
            if (!useSuperAggregation)
               agg.setInUse(false);
         }
         if (properties != null)
            firstValue.assignPropsAndGroupingSet(properties, defaultGroupingSet);
         if (useSuperAggregation)
            firstValue.assignAggregators(aggs);
         aggValues.add(firstValue);

         // Add any middle values, which are already terminated.  Middles would
         // only exist if there is an ending object and aggs list.
         aggValues.addAll(prev.getMiddleAggValues());

         // Will merge and/or terminate the ENDING list later.
         prevObject = prev.getEndingObject();
         prevAggsList = prev.getEndingAggList();
      }

      // 2. In the middle of the list, if the previous object matches PAL(i)'s
      // initial object, then we'll need to merge them.  Either way, we'll need
      // to terminate those results.
      int aggListSize = prevAggsList.size();
      int listOfPalsSize = listOfPals.size();
      for (int i = 1; i < listOfPalsSize; i++)
      {
         if (DEBUG)
         {
            System.err.println("  i = " + i);
         }
         PositionedAggregatorList<T> curr = listOfPals.get(i);

         T currObject = curr.getInitialObject();
         List<AggregateFunction> currAggsList = curr.getInitialAggList();
         // Compare the previous object and the current PAL's initial object.
         boolean objMatch = (comparator.compare(prevObject, currObject) == 0);
         if (curr.getEndingAggList() == null)
         {
            // 2.1 Initial only.
            if (objMatch)
            {
               if (DEBUG)
               {
                  System.err.println("Case 2.1 (Match): Init only");
               }
               // If objects match, then merge current initial into previous.
               for (int a = 0; a < aggListSize; a++)
               {
                  AggregateFunction prevAgg = prevAggsList.get(a);
                  AggregateFunction currAgg = currAggsList.get(a);
                  prevAgg.merge(currAgg);
                  currAgg.setInUse(false);
               }
               // Keep the previous object and aggs list.
            }
            else
            {
               if (DEBUG)
               {
                  System.err.println("Case 2.1 (No Match): Init only");
               }
               // If not a match, then terminate previous only.
               AggregateValue<T> prevValue = new AggregateValue<T>(prevObject);
               for (int a = 0; a < aggListSize; a++)
               {
                  AggregateFunction prevAgg = prevAggsList.get(a);
                  prevValue.setAggregateValue(prevAgg, prevAgg.terminate());
                  if (!useSuperAggregation)
                     prevAgg.setInUse(false);
               }
               if (properties != null)
                  prevValue.assignPropsAndGroupingSet(properties, defaultGroupingSet);
               if (useSuperAggregation)
                  prevValue.assignAggregators(prevAggsList);
               aggValues.add(prevValue);

               // Make the current initial the previous.
               prevObject = currObject;
               prevAggsList = curr.getInitialAggList();
            }
         }
         else  // ending object and aggs list exist.
         {
            // 2.2 Init/End.
            if (objMatch)
            {
               if (DEBUG)
               {
                  System.err.println("Case 2.2 (Match): Init/End");
               }
               // If objects match, then merge current initial into previous and
               // terminate.
               AggregateValue<T> prevValue = new AggregateValue<T>(prevObject);
               for (int a = 0; a < aggListSize; a++)
               {
                  AggregateFunction prevAgg = prevAggsList.get(a);
                  AggregateFunction currAgg = currAggsList.get(a);
                  prevAgg.merge(currAgg);
                  currAgg.setInUse(false);
                  prevValue.setAggregateValue(prevAgg, prevAgg.terminate());
                  if (!useSuperAggregation)
                     prevAgg.setInUse(false);
               }
               if (properties != null)
                  prevValue.assignPropsAndGroupingSet(properties, defaultGroupingSet);
               if (useSuperAggregation)
                  prevValue.assignAggregators(prevAggsList);
               aggValues.add(prevValue);
            }
            else
            {
               if (DEBUG)
               {
                  System.err.println("Case 2.2 (No Match): Init/End");
               }
               // If no match, then terminate both previous and current initial.
               AggregateValue<T> prevValue = new AggregateValue<T>(prevObject);
               AggregateValue<T> currValue = new AggregateValue<T>(currObject);
               for (int a = 0; a < aggListSize; a++)
               {
                  AggregateFunction prevAgg = prevAggsList.get(a);
                  prevValue.setAggregateValue(prevAgg, prevAgg.terminate());
                  AggregateFunction currAgg = currAggsList.get(a);
                  currValue.setAggregateValue(currAgg, currAgg.terminate());
                  if (properties != null)
                  {
                     prevValue.assignPropsAndGroupingSet(properties, defaultGroupingSet);
                     currValue.assignPropsAndGroupingSet(properties, defaultGroupingSet);
                  }
                  if (!useSuperAggregation)
                  {
                     prevAgg.setInUse(false);
                     currAgg.setInUse(false);
                  }
               }
               if (useSuperAggregation)
               {
                  prevValue.assignAggregators(prevAggsList);
                  currValue.assignAggregators(currAggsList);
               }
               aggValues.add(prevValue);
               aggValues.add(currValue);
            }
            // Add any current middles.
            aggValues.addAll(curr.getMiddleAggValues());
            // Make the current ending the previous.
            prevObject = curr.getEndingObject();
            prevAggsList = curr.getEndingAggList();
         }
      }
      // 3. Off the end of the List.  Wrap up the last one.
      // In any above case, the last one is "previous".
      // Terminate it.
      if (DEBUG)
      {
         System.err.println("Case 3: End");
      }
      AggregateValue<T> prevValue = new AggregateValue<T>(prevObject);
      for (int a = 0; a < aggListSize; a++)
      {
         AggregateFunction prevAgg = prevAggsList.get(a);
         prevValue.setAggregateValue(prevAgg, prevAgg.terminate());
         if (!useSuperAggregation)
            prevAgg.setInUse(false);
      }
      if (properties != null)
         prevValue.assignPropsAndGroupingSet(properties, defaultGroupingSet);
      if (useSuperAggregation)
         prevValue.assignAggregators(prevAggsList);
      aggValues.add(prevValue);

      return aggValues;
   }

   /**
    * In the already sorted list, return the highest index whose item in the
    * list compares equal to the item at the given start index.
    *
    * @param <T> The type of objects in the <code>List</code> of values.
    * @param list The <code>List</code> of values.
    * @param comparator Decides how to compare values for equality.
    * @param startIdx Start looking for the last match at this index.
    * @return The index that represents the last object in the given
    *    <code>List</code> that compares equal to the object represented by
    *    the start index.
    */
   public static <T> int indexOfLastMatching(List<T> list,
      Comparator<? super T> comparator, int startIdx)
   {
      return indexOfLastMatching(list, comparator, startIdx, list.size() - 1);
   }

   /**
    * In the already sorted list, return the highest index whose item in the
    * list compares equal to the item at the given start index, except that no
    * value larger than the maximum index will be returned.
    * @param <T> The type of objects in the <code>List</code> of values.
    * @param list The <code>List</code> of values.
    * @param comparator Decides how to compare values for equality.
    * @param startIdx Start looking for the last match at this index.
    * @param maxIdx Don't look past this index.
    * @return The lesser of the index that represents the last object in the
    *    given <code>List</code> that compares equal to the object represented
    *    by the start index, and <code>maxIdx</code>.
    */
   public static <T> int indexOfLastMatching(List<T> list,
      Comparator<? super T> comparator, int startIdx, int maxIdx)
   {
      T value = list.get(startIdx);
      int addMatchIdx = 1;
      // lowerBoundMatchIdx is what will be returned.
      int lowerBoundMatchIdx = startIdx;
      int upperBoundMatchIdx = startIdx + addMatchIdx;

      // Find out lower/upper bound of last match by repeatedly doubling
      // addMatchIdx until the end of the list is reached or we have found an
      // item that doesn't match.
      while (true)
      {
         // Don't go off the end of the List.
         if (upperBoundMatchIdx >= maxIdx)
         {
            upperBoundMatchIdx = maxIdx;
            break;
         }

         if (comparator.compare(value, list.get(upperBoundMatchIdx)) == 0)
         {
            // The item at upperBoundMatchIdx matches.  Adjust lower/upper
            // bounds.
            lowerBoundMatchIdx = upperBoundMatchIdx;
            //addMatchIdx *= 2;
            addMatchIdx <<= 1;
            upperBoundMatchIdx += addMatchIdx;
         }
         else
         {
            // The item at startIdx + addMatchIdx doesn't match.  Lower/upper
            // bounds are correct.
            break;
         }
      }

      // Binary search between lower/upper bound to find the last item.
      while (true)
      {
         //int midMatchIdx = (lowerBoundMatchIdx + upperBoundMatchIdx) / 2;
         int midMatchIdx = (lowerBoundMatchIdx + upperBoundMatchIdx) >> 1;

         // If we have a match, then we are done.
         if (lowerBoundMatchIdx == upperBoundMatchIdx) break;
         // If two possibilities, left, special logic to determine which one:
         // lower or upper.  It's necessary because otherwise an infinite loop
         // would result where midMatchIdx continuously evaluates to
         // lowerBoundMatchIdx.
         boolean downToTwo = (lowerBoundMatchIdx == upperBoundMatchIdx - 1);

         if (comparator.compare(value, list.get(midMatchIdx)) == 0)
         {
            if (downToTwo)
            {
               // lowerBoundMatchIdx == midMatchIdx
               if (comparator.compare(value, list.get(upperBoundMatchIdx)) == 0)
               {
                  // Upper bound compares equal also.  It's the upper bound.
                  lowerBoundMatchIdx = upperBoundMatchIdx;
               }
               // Else, upper bound doesn't compare equal.  It's the lower bound.
               // Either way, we've found our index.  Break.
               break;
            }
            // The item at midMatchIdx matches.  Set lower bound here.
            lowerBoundMatchIdx = midMatchIdx;
         }
         else
         {
            // The item at midMatchIdx doesn't match.  Set upper bound at here - 1.
            // Note that in this case it is impossible to be down to two.
            // Otherwise at least the lower bound would have matched.
            upperBoundMatchIdx = midMatchIdx - 1;
         }
      }

      return lowerBoundMatchIdx;
   }
}