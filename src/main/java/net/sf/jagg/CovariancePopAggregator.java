package net.sf.jagg;

import net.sf.jagg.math.DoubleDouble;

/**
 * This class represents the "population covariance" aggregator over two sets
 * of numeric values.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class CovariancePopAggregator extends AbstractCovarianceAggregator
{
   /**
    * Constructs a <code>CovariancePopAggregator</code> on the specified
    * properties, in the format: <code>property, property2</code>.
    * @param properties A specification string in the format:
    *    <code>property, property2</code>.
    */
   public CovariancePopAggregator(String properties)
   {
      super(properties);
   }
   
   /**
    * Constructs a <code>CovariancePopAggregator</code> that operates on the specified
    * properties.
    * @param property Determine the population covariance of this property with the other.
    * @param property2 Determine the population covariance of this property with the other.
    */
   public CovariancePopAggregator(String property, String property2)
   {
      super(property, property2);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public CovariancePopAggregator replicate()
   {
      return new CovariancePopAggregator(getProperty(), getProperty2());
   }

   /**
    * Return the population covariance by dividing the variance numerator by
    * <em>n</em>, where <em>n</em> is the number of non-null pairs of numbers
    * present in the aggregation.
    *
    * @return The population covariance as a <code>Double</code>,
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
    * @return The population covariance as a <code>DoubleDouble</code>,
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
