package net.sf.jagg;

import net.sf.jagg.math.DoubleDouble;

/**
 * This class represents the "population standard deviation" aggregator over
 * numeric values.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class StdDevPopAggregator extends AbstractVarianceAggregator
{
   /**
    * Constructs an <code>StdDevPopAggregator</code> that operates on the specified
    * property.
    * @param property Calculate the population standard deviation of this property's values.
    */
   public StdDevPopAggregator(String property)
   {
      super(property);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public StdDevPopAggregator replicate()
   {
      return new StdDevPopAggregator(getProperty());
   }
   /**
    * Return the population standard deviation by taking the square root of the
    * population variance.
    *
    * @return The population standard deviation as a <code>Double</code>.  Returns
    *    <code>NaN</code> if no items have been accumulated, or 0 if exactly
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
    * @return The population standard deviation as a <code>DoubleDouble</code>,
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
      result.sqrtSelf();
      return result;
   }
}
