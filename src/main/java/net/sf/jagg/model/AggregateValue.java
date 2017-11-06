package net.sf.jagg.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jagg.AggregateFunction;
import net.sf.jagg.Aggregator;

/**
 * This class represents the result of a "group by" operation, where certain
 * fields of a type are selected for a "group by", and certain values can
 * be extracted by referring to <code>AggregateFunctions</code>.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class AggregateValue<T>
{
   private T myObject;
   private Map<AggregateFunction, Object> myValuesMap;
   private List<Object> myValuesList;
   private List<AggregateFunction> myAggregators;
   private List<String> myProperties;
   private List<Integer> myGroupingSet;

   /**
    * Create an <code>AggregateValue</code> that wraps the given object.  It
    * will also store aggregation values.
    *
    * @param object The object for which this <code>AggregateValue</code> will
    *    wrap.
    */
   public AggregateValue(T object)
   {
      myObject = object;
      myValuesMap = new HashMap<AggregateFunction, Object>();
      myValuesList = new ArrayList<Object>();
      myAggregators = null;
   }

   /**
    * Create an <code>AggregateValue</code> using another
    * <code>AggregateValue</code>.  This will wrap the same object that the
    * other <code>AggregateValue</code> wraps.
    * @param other Another <code>AggregateValue</code>.
    * @since 0.7.0
    */
   public AggregateValue(AggregateValue<T> other)
   {
      this(other.myObject);
   }

   /**
    * <p>Retrieves the <code>T</code> representing the "group-by" aggregation.
    * This method is used to directly access the property values, when
    * aggregating by calling <code>Aggregation.groupByComparable</code>.</p>
    * <p>If <code>Aggregation.groupBy</code> is called instead, then this
    * method will still succeed.  However, if super-aggregation is used (cube,
    * rollups, grouping sets), the object returned here will not indicate that
    * this <code>AggregateValue</code> represents "all values" for a certain
    * property.  In this case, either of the two overloaded
    * <code>getPropertyValue</code> methods will correctly return the
    * <code>null</code> value if that property represents "all values".</p>
    *
    * @return The <code>T</code> object representing the "group-by" aggregation.
    * @see net.sf.jagg.Aggregation#groupByComparable
    * @see #getPropertyValue(String)
    * @see #getPropertyValue(int)
    */
   public T getObject()
   {
      return myObject;
   }

   /**
    * <p>Retrieves a property value representing a "group-by" category by name.
    * This method is used to access the property values, when aggregating by
    * calling <code>Aggregation.groupBy</code> after specifying "group-by"
    * properties.  Because super-aggregation is possible in this case, if the
    * referenced property represents "all values", then this method will return
    * <code>null</code> instead of the actual property value.</p>
    * @param property The property name.
    * @return The property value.
    * @since 0.7.0
    * @see net.sf.jagg.Aggregation#groupBy
    */
   public Object getPropertyValue(String property)
   {
      if (myObject == null)
         return null;
      if (myProperties != null)
      {
         int index = myProperties.indexOf(property);
         // Return null for "super-aggregate" columns.
         if (!myGroupingSet.contains(index))
            return null;
      }
      return Aggregator.getValueFromProperty(myObject, property);
   }

   /**
    * <p>Retrieves a property value representing a "group-by" category, with
    * the property specified by a 0-based index into the original list of
    * properties that was specified in <code>Builder.getProperties</code>.</p>
    * <p>If <code>Aggregation.groupByComparable</code> was called, then there
    * are no "group-by" properties, so this method would return
    * <code>null</code>.
    * @param propIndex The 0-based index into the list of properties.
    * @return The property value.
    * @since 0.7.0
    */
   public Object getPropertyValue(int propIndex)
   {
      if (myProperties != null && myObject != null)
      {
         return getPropertyValue(myProperties.get(propIndex));
      }
      return null;
   }

   /**
    * This method is used internally to store the given <code>value</code>
    * associated with the given <code>AggregateFunction</code> for later retrieval.
    * It also appends the given <code>value</code> to an internal list for
    * later retrieval by index.
    *
    * @param agg An <code>AggregateFunction</code>.
    * @param value The aggregated value.
    * @see AggregateFunction
    */
   public void setAggregateValue(AggregateFunction agg, Object value)
   {
      myValuesMap.put(agg, value);
      myValuesList.add(value);
   }

   /**
    * Retrieves the value for the given <code>AggregateFunction</code>.
    *
    * @param agg An <code>AggregateFunction</code>.
    * @return The aggregated value, or <code>null</code> if no such
    *    <code>AggregateFunction</code> is found.
    */
   public Object getAggregateValue(AggregateFunction agg)
   {
      return myValuesMap.get(agg);
   }

   /**
    * Retrieves the value for the <code>AggregateFunction</code> at the given index.
    * @param index The 0-based index.
    * @return The aggregated value.
    * @throws IndexOutOfBoundsException If the index is out of range.
    * @since 0.3.0
    */
   public Object getAggregateValue(int index)
   {
      return myValuesList.get(index);
   }

   /**
    * <p>Determines whether the referenced field represents the set of all
    * values in a super-aggregate value.  This can be used to distinguish an
    * actual <code>null</code> value in normal aggregation vs. a
    * <code>null</code> that represents "all values" in super-aggregation.</p>
    * <p>For example, if there are 4 group-by properties, and super-aggregation
    * is used, and this aggregate value happens to represent the grouping set
    * {0, 1}, then properties 2 and 3 are "all values", and ...</p>
    * <ul>
    * <li><code>isGrouping(0) == isGrouping(1) == false</code>
    * <li><code>isGrouping(2) == isGrouping(3) == true</code>
    * </ul>
    * @param field A field reference integer, from 0 to <em>n</em> - 1, where
    *    <em>n</em> is the the number of group-by properties.
    * @return <code>true</code> if the field represents "all values" in
    *    super-aggregation, <code>false</code> otherwise.
    * @throws IllegalArgumentException If the integer field reference is out of
    *    range.
    * @since 0.7.0
    */
   public boolean isGrouping(int field)
   {
      if (field < 0 || field >= myProperties.size())
         throw new IllegalArgumentException("isGrouping: integer field reference out of range: " + field);
      return !myGroupingSet.contains(field);
   }

   /**
    * <p>Determines whether the referenced field represents the set of all
    * values in a super-aggregate value.  This can be used to distinguish an
    * actual <code>null</code> value in normal aggregation vs. a
    * <code>null</code> that represents "all values" in super-aggregation.</p>
    * <p>For example, if there are 4 group-by properties ({"prop0", "prop1",
    * "prop2", "prop3"}), and super-aggregation is used, and this aggregate
    * value happens to represent the grouping set {0, 1}, then properties 2
    * and 3 are "all values", and ...</p>
    * <ul>
    * <li><code>isGrouping("prop0") == isGrouping("prop1") == false</code>
    * <li><code>isGrouping("prop2") == isGrouping("prop3") == true</code>
    * </ul>
    * @param propertyName A property name.
    * @return <code>true</code> if the field represents "all values" in
    *    super-aggregation, <code>false</code> otherwise.
    * @throws IllegalArgumentException If the property name is not a group-by
    *    property.
    * @since 0.7.0
    */
   public boolean isGrouping(String propertyName)
   {
      int index = myProperties.indexOf(propertyName);
      if (index == -1)
      {
         // Property name not found.
         // Try as integer field reference.
         try
         {
            index = Integer.parseInt(propertyName);
            return isGrouping(index);
         }
         catch (NumberFormatException e)
         {
            // Property name not found and it's not an integer.
            throw new IllegalArgumentException("isGrouping: Not a group-by property name or an integer field reference: " +
               propertyName);
         }
      }
      return isGrouping(myProperties.indexOf(propertyName));
   }

   /**
    * <p>Determines the distinct grouping ID of the given referenced fields by
    * determining whether each given referenced field represents "all values"
    * in super-aggregation.</p>
    * <p>For example, if there are 4 group-by properties ({"prop0", "prop1",
    * "prop2", "prop3"}), and super-aggregation is used, and this aggregate
    * value happens to represent the grouping set {0, 1}, then properties 2 and
    * 3 are "all values", and ...</p>
    * <ul>
    * <li><code>getGroupingId({0}) == getGroupingId({1}) == 0</code>
    * <li><code>getGroupingId({2}) == getGroupingId({3}) == 1</code>
    * <li><code>getGroupingId({0, 1}) == getGroupingId({1, 0}) == 0</code>
    * <li><code>getGroupingId({0, 2}) == getGroupingId({0, 3}) == 1</code>
    * <li><code>getGroupingId({1, 2}) == getGroupingId({1, 3}) == 1</code>
    * <li><code>getGroupingId({2, 0}) == getGroupingId({2, 1}) == 2</code>
    * <li><code>getGroupingId({3, 0}) == getGroupingId({3, 1}) == 2</code>
    * <li><code>getGroupingId({2, 3}) == getGroupingId({3, 2}) == 3</code>
    * </ul>
    * <p>In the above examples, each integer <em>n</em> reference can be freely
    * substituted with the equivalent property name, e.g. <code>0</code> is
    * equivalent to <code>"prop0"</code>.</p> 
    * @param fields A <code>List</code> of field references, which can be
    *    integer field references, from 0 to <em>n</em> - 1, where <em>n</em>
    *    is the number of group-by properties, or they can be property names.
    *    Each field reference maps to a bit in the returned number.
    * @return An integer, with each set bit corresponding to an "all values"
    *    determination.  The most significant bit corresponds to the first
    *    element.
    * @throws IllegalArgumentException If any of the fields represent integer
    *    field references that are out of range, or they represent string
    *    property names that aren't group-by properties.
    * @since 0.7.0
    */
   public int getGroupingId(List<?> fields)
   {
      // Note: This assumes that there are 32 or less fields passed in.
      int groupingId = 0;

      for (Object field : fields)
      {
         groupingId <<= 1;
         int fieldRef;
         if (field instanceof Number)
         {
            fieldRef = ((Number) field).intValue();
         }
         else
         {
            String propertyName = field.toString();
            fieldRef = myProperties.indexOf(propertyName);
            if (fieldRef == -1)
            {
               // Property name not found.
               // Try as integer field reference.
               try
               {
                  fieldRef = Integer.parseInt(propertyName);
               }
               catch (NumberFormatException e)
               {
                  // Property name not found and it's not an integer.
                  throw new IllegalArgumentException("getGroupingId: Not a group-by property name or an integer field reference: " +
                     propertyName);
               }
            }
         }
         if (fieldRef < 0 || fieldRef >= myProperties.size())
         {
            throw new IllegalArgumentException("getGroupingId: integer field reference out of range: " + field);
         }
         // Mark the bit if it's "all values".
         if (isGrouping(fieldRef))
         {
            groupingId |= 1;
         }
      }

      return groupingId;
   }

   /**
    * Assign a <code>List</code> of <code>AggregateFunctions</code> used to
    * make this <code>AggregateValue</code>.  These <code>AggregateFunctions</code>
    * are stored here for the purposes of super-aggregation later.
    * @param aggs A <code>List</code> of <code>AggregateFunctions</code>.
    * @since 0.7.0
    */
   public void assignAggregators(List<AggregateFunction> aggs)
   {
      myAggregators = aggs;
   }

   /**
    * Retrieves the <code>List</code> of <code>AggregateFunctions</code> that
    * are responsible for the aggregate values in this <code>AggregateValue</code>.
    * @return The <code>List</code> of <code>AggregateFunctions</code>.
    * @since 0.7.0
    */
   public List<AggregateFunction> retrieveAggregators()
   {
      return myAggregators;
   }

   /**
    * Mark all <code>AggregateFunctions</code> as no longer in use and no
    * longer maintain the list of assigned <code>AggregateFunctions</code>.
    * @since 0.7.0
    */
   public void releaseAggregators()
   {
      for (AggregateFunction agg : myAggregators)
         agg.setInUse(false);
      myAggregators.clear();
   }

   /**
    * Assign a <code>List</code> of properties and a grouping set, which is a
    * <code>List</code> of 0-based property name indexes.
    * @param properties A <code>List</code> of property names.
    * @param groupingSet A <code>List</code> of 0-based property name indexes.
    * @since 0.7.0
    */
   public void assignPropsAndGroupingSet(List<String> properties, List<Integer> groupingSet)
   {
      myProperties = properties;
      myGroupingSet = groupingSet;
   }

   /**
    * Returns the string representation.
    * @return The string representation.
    * @since 0.7.0
    */
   @Override
   public String toString()
   {
      /*StringBuffer buf = new StringBuffer();
      buf.append("AggregateValue:(object => ");
      buf.append(myObject.toString());
      buf.append(", valuesList =>");
      buf.append(myValuesList.toString());
      buf.append(", aggregators =>");
      buf.append(myAggregators.toString());
      buf.append(", properties =>");
      buf.append(myProperties.toString());
      buf.append(", groupingSet =>");
      buf.append(myGroupingSet.toString());
      buf.append(")");
      return buf.toString();*/
      return super.toString();
   }
}
