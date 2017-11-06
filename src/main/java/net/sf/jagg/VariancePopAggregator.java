package net.sf.jagg;

import net.sf.jagg.math.DoubleDouble;

/**
 * This class represents the "population variance" aggregator over numeric values.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class VariancePopAggregator extends AbstractVarianceAggregator
{
   /**
    * Constructs an <code>VariancePopAggregator</code> that operates on the specified
    * property.
    * @param property Calculate the population variance of this property's values.
    */
   public VariancePopAggregator(String property)
   {
      super(property);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public VariancePopAggregator replicate()
   {
      return new VariancePopAggregator(getProperty());
   }

   /**
    * Return the population variance by dividing the variance numerator by
    * <em>n</em>, where <em>n</em> is the number of non-null pairs
    * of numbers.
    *
    * @return The population variance as a <code>Double</code>,
    *    <code>NaN</code> if no values have been accumulated, or 0 if exactly
    *    one value has been accumulated.
    */
   public Double terminate()
   {
      return terminateDoubleDouble().doubleValue();
   }

   /**
    * Return the result as a <code>DoubleDouble</code>.  This is used mainly
    * when other <code>Aggregators</code> that use this result must maintain a
    * high precision.
    * @return The population variance as a <code>DoubleDouble</code>,
    *    <code>NaN</code> if no values have been accumulated, or 0 if exactly
    *    one value has been accumulated.
    * @since 0.4.0
    */
   public DoubleDouble terminateDoubleDouble()
   {
      if (myCount <= 0)
         return new DoubleDouble(DoubleDouble.NaN);
      if (myCount == 1)
         return new DoubleDouble(0);
      DoubleDouble result = new DoubleDouble(myVarNumerator);
      result.divideSelfBy(myCount);
      return result;
   }
}
