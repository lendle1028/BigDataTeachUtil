package net.sf.jagg.model;

import java.util.List;

import net.sf.jagg.model.AnalyticValue;
import net.sf.jagg.util.PartitionAndOrderByComparator;

/**
 * An <code>AnalyticContext</code> stores all of the context information
 * necessary to perform one analytic operation on a list of values.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class AnalyticContext<T>
{
   private List<AnalyticValue<T>> mySortedValues;
   private PartitionAndOrderByComparator<T> myComparator;
   private List<Integer> myDependencies;
   private int myWindowStartIndex;
   private int myWindowEndIndex;
   private int myTerminatedThroughIndex;
   private int myEndPartitionIndex;

   /**
    * Constructs an <code>AnalyticContext</code> consisting of the given
    * already-sorted <code>List</code> of <code>AnalyticValues</code>, sorted
    * according to the given <code>PartitionAndOrderByComparator</code>.  The
    * initial values of the starting and ending indexes of the sliding window
    * are <code>-1</code>, before the first value.  The initial values of the
    * terminated through index and the end of the partition index are also
    * <code>-1</code>, before the first value.
    * @param sortedValues An already sorted <code>List</code> of
    *    <code>AnalyticValues</code>.
    * @param comparator The <code>PartitionAndOrderByComparator</code> used to
    *    sort the values.
    */
   public AnalyticContext(List<AnalyticValue<T>> sortedValues, PartitionAndOrderByComparator<T> comparator)
   {
      mySortedValues = sortedValues;
      myComparator = comparator;
      myDependencies = null;
      // Initial empty window starts at 0 and "ends" at -1.
      myWindowStartIndex = 0;
      myWindowEndIndex = -1;
      myTerminatedThroughIndex = -1;
      myEndPartitionIndex = -1;
   }

   /**
    * Returns the <code>List</code> of <code>AnalyticValues</code>.
    * @return The <code>List</code> of <code>AnalyticValues</code>.
    */
   public List<AnalyticValue<T>> getListOfValues()
   {
      return mySortedValues;
   }

   /**
    * Returns the <code>PartitionAndOrderByComparator</code>.
    * @return The <code>PartitionAndOrderByComparator</code>.
    */
   public PartitionAndOrderByComparator<T> getComparator()
   {
      return myComparator;
   }

   /**
    * Returns the <code>List</code> of dependencies, if it was set previously.
    * Each element is an index into <code>Analytic's</code> list of
    * <code>AnalyticAggregators</code>.
    * @return The <code>List</code> of dependencies, or <code>null</code> if it
    *    wasn't set.
    */
   public List<Integer> getDependencies()
   {
      return myDependencies;
   }

   /**
    * Sets the <code>List</code> of dependencies.  Each element is an index
    * into <code>Analytic's</code> list of <code>AnalyticAggregators</code>.
    * @param dependencies A <code>List</code> of dependencies.
    */
   public void setDependencies(List<Integer> dependencies)
   {
      myDependencies = dependencies;
   }

   /**
    * Returns the 0-based index indicating the start of the window.
    * @return The 0-based index indicating the start of the window.
    */
   public int getWindowStartIndex()
   {
      return myWindowStartIndex;
   }

   /**
    * Slide the start of the window down by 1.
    */
   public void incrementWindowStartIndex()
   {
      myWindowStartIndex++;
   }

   /**
    * Advances the start of the window one past the last terminated index.
    */
   public void advanceWindowStartPastLastTerminated()
   {
      myWindowStartIndex = myTerminatedThroughIndex + 1;
   }

   /**
    * Returns the 0-based index indicating the end of the window.
    * @return The 0-based index indicating the end of the window.  A value of
    *    <code>-1</code> indicates "before the start of the window".
    */
   public int getWindowEndIndex()
   {
      return myWindowEndIndex;
   }

   /**
    * Slide the end of the window down by 1.
    */
   public void incrementWindowEndIndex()
   {
      myWindowEndIndex++;
   }

   /**
    * Returns whether the current window is empty.
    * @return Whether the current window is empty.
    */
   public boolean isWindowEmpty()
   {
      return myWindowStartIndex > myWindowEndIndex;
   }

   /**
    * Returns the "terminated through" index, through which all analytic values
    * have been assigned a terminated value for this context.
    * @return The "terminated through" index, or <code>-1</code> if no analytic
    *    values have any values yet.
    */
   public int getTerminatedThroughIndex()
   {
      return myTerminatedThroughIndex;
   }

   /**
    * Increments the "terminated through" index, when an analytic value has
    * been assigned a terminated value for this context.
    */
   public void incrementTerminatedThroughIndex()
   {
      myTerminatedThroughIndex++;
   }

   /**
    * Returns the 0-based index of the end of the current partition.
    * @return The 0-based index of the end of the current partition, or
    *    <code>-1</code> if there is no current partition yet.
    */
   public int getEndOfPartitionIndex()
   {
      return myEndPartitionIndex;
   }

   /**
    * Sets the 0-based index of the end of the current partition.
    * @param index The 0-based index of the end of the current partition, or
    *    <code>-1</code> if there is no current partition yet.
    */
   public void setEndOfPartitionIndex(int index)
   {
      myEndPartitionIndex = index;
   }
}
