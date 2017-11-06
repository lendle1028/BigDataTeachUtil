package net.sf.jagg.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.jagg.model.AggregateValue;
import net.sf.jagg.AggregateFunction;

/**
 * <p>This class is necessary for parallel processing.  It is possible for
 * some <code>Aggregators</code> to return different results based on the
 * order in which threads finish, e.g. <code>ConcatAggregator</code>.  When
 * a thread finishes and creates its <code>Future</code>, its position is
 * also stored here so that order can be preserved by the consumer.</p>
 * <p>This stores an unfinished list of Aggregators at the front, a finished
 * list of AggregateValues in the middle, and an unfinished list of
 * Aggregators at the end.</p>
 * <p>This used to be a static class within the <code>Aggregations</code>
 * utility class, but it was extracted for version 0.7.0.</p>
 *
 * @author Randy Gettman
 * @since 0.7.0
 */
public class PositionedAggregatorList<T>
{
   private int myPos;
   private T myInitialObject;
   private List<AggregateFunction> myInitialAggList;
   private List<AggregateValue<T>> myMiddleAggValues;
   private T myEndingObject;
   private List<AggregateFunction> myEndingAggList;

   /**
    * Create a <code>PositionedAggregatorList</code> that represents the
    * work done by a Thread in parallel mode.  It consist of a (possibly)
    * unfinished initial <code>List</code> of <code>AggregateFunctions</code>,
    * a finished middle <code>List</code> of <code>AggregateValues</code>, and
    * a (possibly) unfinished ending <code>List</code> of
    * <code>AggregateFunctions</code>.
    * @param pos The 0-based position for ordering purposes.
    */
   public PositionedAggregatorList(int pos)
   {
      myInitialObject = null;
      myInitialAggList = null;
      myMiddleAggValues = new ArrayList<AggregateValue<T>>();
      myEndingObject = null;
      myEndingAggList = null;
      myPos = pos;
   }

   /**
    * Sets the results from the initial run (of the thread).
    * @param initObject The initial object.
    * @param initAggregators The initial <code>List</code> of
    *    <code>AggregateFunctions</code>.
    */
   public void setInitialList(T initObject, List<AggregateFunction> initAggregators)
   {
      myInitialObject = initObject;
      myInitialAggList = initAggregators;
   }

   /**
    * Sets the finished results from the middle runs (of the thread).
    * @param aggValues The finished <code>List</code> of
    *    <code>AggregateValues</code>.
    */
   public void setMiddleAggValues(List<AggregateValue<T>> aggValues)
   {
      myMiddleAggValues.addAll(aggValues);
   }

   /**
    * Sets the results from the ending run (of the thread).
    * @param endingObject The ending object.
    * @param endingAggregators The ending <code>List</code> of
    *    <code>AggregateFunctions</code>.
    */
   public void setEndingList(T endingObject, List<AggregateFunction> endingAggregators)
   {
      myEndingObject = endingObject;
      myEndingAggList = endingAggregators;
   }

   /**
    * Returns the position, as a 0-based index.
    * @return The position, as a 0-based index.
    */
   public int getPosition()
   {
      return myPos;
   }

   /**
    * Returns the object from the initial run.
    * @return The object from the initial run.
    */
   public T getInitialObject()
   {
      return myInitialObject;
   }

   /**
    * Returns the initial <code>List</code> of <code>AggregateFunctions</code>.
    * @return The initial <code>List</code> of <code>AggregateFunctions</code>.
    */
   public List<AggregateFunction> getInitialAggList()
   {
      return myInitialAggList;
   }

   /**
    * Returns the middle <code>List</code>of <code>AggregateValues</code>.
    * @return The middle <code>List</code>of <code>AggregateValues</code>.
    */
   public List<AggregateValue<T>> getMiddleAggValues()
   {
      return myMiddleAggValues;
   }

   /**
    * Returns the object from the ending run.
    * @return The object from the ending run.
    */
   public T getEndingObject()
   {
      return myEndingObject;
   }

   /**
    * Returns the ending <code>List</code> of <code>AggregateFunctions</code>.
    * @return The ending <code>List</code> of <code>AggregateFunctions</code>.
    */
   public List<AggregateFunction> getEndingAggList()
   {
      return myEndingAggList;
   }

   /**
    * Returns the string representation.
    * @return The string representation.
    */
   @Override
   public String toString()
   {
      StringBuilder buf = new StringBuilder();
      buf.append("PAL: pos=");
      buf.append(myPos);
      buf.append("\n  Initial object=");
      buf.append((myInitialObject == null) ? "(null)" : myInitialObject.toString());
      for (int i = 0; i < myMiddleAggValues.size(); i++)
      {
         buf.append("\n  MiddleAggValue object(");
         buf.append(i);
         buf.append(")=");
         buf.append(myMiddleAggValues.get(i).toString());
      }
      buf.append("\n  Ending object=");
      buf.append((myEndingObject == null) ? "(null)" : myEndingObject.toString());
      return buf.toString();
   }
}
