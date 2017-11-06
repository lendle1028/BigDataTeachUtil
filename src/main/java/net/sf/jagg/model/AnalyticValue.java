package net.sf.jagg.model;

import java.util.HashMap;
import java.util.Map;

import net.sf.jagg.AnalyticAggregator;

/**
 * This class represents the result of an analytics operation, where certain
 * values can be extracted by referring to <code>AnalyticAggregators</code>.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class AnalyticValue<T>
{
   private T myObject;
   private Map<AnalyticAggregator, Object> myValuesMap;
   // Values for analytics may be added out of order.  Use Map instead of List.
   private Map<Integer, Object> myIndexedMap;

   /**
    * Create an <code>AnalyticValue</code> that wraps the given object.  It
    * will also store analytic values.
    *
    * @param object The object for which this <code>AnalyticValue</code> will
    *    wrap.
    */
   public AnalyticValue(T object)
   {
      myObject = object;
      myValuesMap = new HashMap<AnalyticAggregator, Object>();
      myIndexedMap = new HashMap<Integer, Object>();
   }

   /**
    * Create an <code>AnalyticValue</code> using another
    * <code>AnalyticValue</code>.  This will wrap the same object that the
    * other <code>AnalyticValue</code> wraps.
    * @param other Another <code>AnalyticValue</code>.
    */
   public AnalyticValue(AnalyticValue<T> other)
   {
      this(other.myObject);
   }

   /**
    * <p>Retrieves the <code>T</code> representing the "group-by" aggregation.
    * This method is used to directly access the property values, when
    * analyzing by calling <code>Analytic.analyze</code>.</p>
    *
    * @return The <code>T</code> object representing the analytic operation.
    * @see net.sf.jagg.Analytic#analyze
    */
   public T getObject()
   {
      return myObject;
   }

   /**
    * This method is used internally to store the given <code>value</code>
    * associated with the given <code>AnalyticAggregator</code> for later retrieval.
    * It also appends the given <code>value</code> to an internal list for
    * later retrieval by index.
    *
    * @param index The 0-based index of the <code>AnalyticAggregator</code>.
    * @param ana An <code>AnalyticAggregator</code>.
    * @param value The analyzed value.
    * @see AnalyticAggregator
    */
   public void setAnalyzedValue(int index, AnalyticAggregator ana, Object value)
   {
      myValuesMap.put(ana, value);
      myIndexedMap.put(index, value);
   }

   /**
    * Retrieves the value for the given <code>AnalyticAggregator</code>.
    *
    * @param ana An <code>AnalyticAggregator</code>.
    * @return The analyzed value, or <code>null</code> if no such
    *    <code>AnalyticAggregator</code> is found.
    */
   public Object getAnalyzedValue(AnalyticAggregator ana)
   {
      return myValuesMap.get(ana);
   }

   /**
    * Retrieves the value for the <code>AnalyticAggregator</code> at the given index.
    * @param index The 0-based index.
    * @return The analyzed value.
    * @throws IndexOutOfBoundsException If the index is out of range.
    */
   public Object getAnalyzedValue(int index)
   {
      return myIndexedMap.get(index);
   }

    /**
    * Returns the string representation.
    * @return The string representation.
    */
   @Override
   public String toString()
   {
      StringBuffer buf = new StringBuffer();
      buf.append("AnalyticValue:(object => ");
      buf.append(myObject.toString());
      buf.append(", myIndexedMap =>");
      buf.append(myIndexedMap.toString());
      buf.append(", valuesMap =>");
      buf.append(myValuesMap.toString());
      buf.append(")");
      return buf.toString();
   }
}
