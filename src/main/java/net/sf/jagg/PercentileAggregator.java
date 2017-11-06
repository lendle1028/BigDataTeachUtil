package net.sf.jagg;

import java.util.Collections;
import java.util.TreeMap;

import net.sf.jagg.exception.ExpectedNumberException;
import net.sf.jagg.math.DoubleDouble;
import net.sf.jagg.util.FrequencyMapUtil;
import net.sf.jagg.model.WindowClause;

/**
 * This class allows represents the "percentile" aggregator over numeric
 * values.
 *
 * The first property is the desired percentile, between 0 and 1 inclusive, and
 * the second property denotes the desired sort order and return value.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class PercentileAggregator extends TwoPropAggregator implements AnalyticFunction
{
   private TreeMap<Double, Integer> myIteratedElements;
   private double  myPercentile;

   /**
    * Constructs a <code>PercentileAggregator</code> on the specified
    * properties, in the format: <code>percentile, property</code>.
    * @param properties A specification string in the format:
    *    <code>percentile, property</code>.
    */
   public PercentileAggregator(String properties)
   {
      setProperty(properties);
   }

   /**
    * Constructs a <code>PercentileAggregator</code> that operates on the specified
    * properties.
    * @param percentile The percentile value, between zero and one.
    * @param property Determine the percentile of this property.
    */
   public PercentileAggregator(double percentile, String property)
   {
      setProperty("" + percentile + "," + property);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public PercentileAggregator replicate()
   {
      return new PercentileAggregator(myPercentile, getProperty2());
   }

   /**
    * Expects that the first "property" given is the actual desired percentile,
    * from 0 to 1 inclusive.  The second "property" is the sort parameter.
    *
    * @param property The property string, with one comma separating two actual
    *    properties.
    * @throws IllegalArgumentException If the first property, the percentile, is
    *    not a number or is not between 0 and 1, inclusive, or if the second
    *    property, the sort parameter, is null.
    * @see Aggregator#getProperty()
    * @see TwoPropAggregator#getProperty2()
    */
   @Override
   protected void setProperty(String property)
   {
      super.setProperty(property);
      try
      {
         myPercentile = Double.parseDouble(getProperty());
         if (myPercentile < 0 || myPercentile > 1)
         {
            throw new ExpectedNumberException("First property (percentile) must be between 0 and 1 inclusive: " +
                    myPercentile);
         }
      }
      catch (NumberFormatException e)
      {
         throw new IllegalArgumentException("First property (percentile) must be between 0 and 1 inclusive: " +
                 myPercentile, e);
      }
      if (getProperty2() == null)
      {
         throw new IllegalArgumentException("Second property (sort parameter) must not be null.");
      }
   }

   /**
    * Initialize an internal list to empty.
    */
   public void init()
   {
      myIteratedElements = new TreeMap<Double, Integer>();
   }

   /**
    * The percentile function can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * The percentile function doesn't supply its own window clause.
    * @return <code>null</code>
    * @since 0.9.0
    */
   public WindowClause getWindowClause()
   {
      return null;
   }

   /**
    * Make sure the second property's value is not null, then add the value.
    *
    * @param value The value to aggregate.
    */
   public void iterate(Object value)
   {
      if (value != null)
      {
         String property = getProperty2();

         try
         {
            // Examine the second property which is the sort order.
            Number obj = (Number) getValueFromProperty(value, property);

            // Don't count nulls.
            if (obj != null)
            {
               Double d = obj.doubleValue();
               FrequencyMapUtil.add(myIteratedElements, d);
            }
         }
         catch (ClassCastException e)
         {
            throw new ExpectedNumberException("Property \"" + property +
               "\" must represent a Number.", e);
         }
      }
   }

   /**
    * Make sure the second property's value is not null, then remove the value.
    *
    * @param value The value to delete.
    * @since 0.9.0
    */
   public void delete(Object value)
   {
      if (value != null)
      {
         String property = getProperty2();

         try
         {
            // Examine the second property which is the sort order.
            Number obj = (Number) getValueFromProperty(value, property);

            // Don't count nulls.
            if (obj != null)
            {
               Double d = obj.doubleValue();
               FrequencyMapUtil.remove(myIteratedElements, d);
            }
         }
         catch (ClassCastException e)
         {
            throw new ExpectedNumberException("Property \"" + property +
               "\" must represent a Number.", e);
         }
      }
   }

   /**
    * Merge the given <code>Aggregator</code> into this one by adding the
    * contents of the given <code>Aggregator's</code> internal list into this
    * <code>Aggregator's</code> internal list.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof PercentileAggregator)
      {
         PercentileAggregator otherAgg = (PercentileAggregator) agg;
         FrequencyMapUtil.combine(otherAgg.myIteratedElements, myIteratedElements);
      }
   }

   /**
    * Return the value among the values in the specified property that matches
    * the given percentile value, with the following algorithm:
    * <ol>
    * <li>Sort the internal list with respect to the second property, using
    *    <code>Collections.sort</code>, using a
    *    <code>PropertiesComparator</code> that compares values based on the
    *    second property given.
    * <li>Calculate a zero-based "row number" based on the percentile value
    *    (the first property given), with the formula <em>r</em> = <em>p</em> *
    *    (<em>n</em> - 1), where <em>r</em> is the row number, <em>p</em> is
    *    the percentile value, and <em>n</em> is the number of non-null values
    *    processed.
    * <li>If <em>r</em> is an integer, then return the value of that row's
    *    property.
    * <li>Else, return a linear interpolation of the values of the two rows'
    *    properties bounding <em>r</em>.
    * </ol>
    *
    * @return The desired <code>Double</code> that best matches the given
    *    percentile value, or <code>null</code> if no items were processed.
    * @see Collections#sort
    * @see net.sf.jagg.util.PropertiesComparator
    */
   public Double terminate()
   {
      return terminateDoubleDouble().doubleValue();
   }

   /**
    * Return the result as a <code>DoubleDouble</code>.  This is used mainly
    * when other <code>Aggregators</code> that use this result must maintain a
    * high precision.
    * @return The desired <code>DoubleDouble</code> that best matches the given
    *    percentile value, or <code>NaN</code> if no values have been
    *    accumulated.
    * @since 0.4.0
    */
   public DoubleDouble terminateDoubleDouble()
   {
      int numItems = myIteratedElements.size();
      DoubleDouble rownum = new DoubleDouble(myPercentile);
      rownum.multiplySelfBy(numItems - 1);
      // Don't need to implement DoubleDouble.floor() and DoubleDouble.ceil(),
      // unless we are aggregating over 2^52 items (4 quadrillion).  That's
      // impossible, because the ArrayList.get method takes an int, which is
      // limited to 2 billion or so.
      double floor = Math.floor(rownum.doubleValue());
      double ceiling = Math.ceil(rownum.doubleValue());

      if (numItems == 0)
         return new DoubleDouble(DoubleDouble.NaN);

      // Now check if the mapped row number maps directly to a specific row or
      // somewhere in between two rows.
      if (rownum.doubleValue() == floor && rownum.doubleValue() == ceiling)
      {
         // Return value of property at specified row.
         return FrequencyMapUtil.get(myIteratedElements, (int) floor);
      }
      else
      {
         // Return linear interpolation of the values at the floor row and the
         // ceiling row.
         return FrequencyMapUtil.get(myIteratedElements, rownum.doubleValue());
      }
   }
}
