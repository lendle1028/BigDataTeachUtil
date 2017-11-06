package net.sf.jagg;

import net.sf.jagg.math.DoubleDouble;
import net.sf.jagg.model.WindowClause;

/**
 * This class represents the "coefficient of correlation" aggregator over two
 * sets of numeric values.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class CorrelationAggregator extends TwoPropAggregator implements AnalyticFunction
{
   private CovariancePopAggregator myCovarianceAgg = null;
   private VariancePopAggregator myFirstVarAgg = null;
   private VariancePopAggregator mySecondVarAgg = null;

   /**
    * Constructs a <code>CorrelationAggregator</code> on the specified
    * properties, in the format: <code>property, property2</code>.
    * @param properties A specification string in the format:
    *    <code>property, property2</code>.
    */
   public CorrelationAggregator(String properties)
   {
      setProperty(properties);
   }

   /**
    * Constructs a <code>CorrelationAggregator</code> that operates on the specified
    * properties.
    * @param property Correlate this property with the other.
    * @param property2 Correlate this property with the other.
    */
   public CorrelationAggregator(String property, String property2)
   {
      setProperty(property + "," + property2);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public CorrelationAggregator replicate()
   {
      return new CorrelationAggregator(getProperty(), getProperty2());
   }

   /**
    * Initialize the internal aggregators: a <code>CovarianceAggregator</code>,
    * and 2 <code>VarianceAggregators</code> (one each for both properties).
    *
    * @see CovarianceAggregator
    * @see VarianceAggregator
    */
   public void init()
   {
      if (myCovarianceAgg == null)
         myCovarianceAgg = new CovariancePopAggregator(getProperty(), getProperty2());
      if (myFirstVarAgg == null)
         myFirstVarAgg = new VariancePopAggregator(getProperty());
      if (mySecondVarAgg == null)
         mySecondVarAgg = new VariancePopAggregator(getProperty2());

      myCovarianceAgg.init();
      myFirstVarAgg.init();
      mySecondVarAgg.init();
   }

   /**
    * Iterate the internal aggregators.
    *
    * @param value The value to aggregate.
    */
   public void iterate(Object value)
   {
      myCovarianceAgg.iterate(value);
      myFirstVarAgg.iterate(value);
      mySecondVarAgg.iterate(value);
   }

   /**
    * Delete from the internal aggregators.
    *
    * @param value The value to delete.
    * @since 0.9.0
    */
   public void delete(Object value)
   {
      myCovarianceAgg.delete(value);
      myFirstVarAgg.delete(value);
      mySecondVarAgg.delete(value);
   }

   /**
    * The correlation function can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * The correlation function doesn't supply its own window clause.
    * @return <code>null</code>
    * @since 0.9.0
    */
   public WindowClause getWindowClause()
   {
      return null;
   }

   /**
    * Merge the given <code>Aggregator</code> into this one by merging each
    * individual internal <code>Aggregator</code>.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof CorrelationAggregator)
      {
         CorrelationAggregator otherAgg = (CorrelationAggregator) agg;
         myCovarianceAgg.merge(otherAgg.myCovarianceAgg);
         myFirstVarAgg.merge(otherAgg.myFirstVarAgg);
         mySecondVarAgg.merge(otherAgg.mySecondVarAgg);
      }
   }

   /**
    * Return the coefficient of correlation, calculated as follows:<br>
    * <code>CovariancePop(prop1, prop2) / Math.sqrt(VariancePop(prop1) * VariancePop(prop2))</code>
    *
    * @return The coefficient of correlation as a <code>Double</code>.
    */
   public Double terminate()
   {
      return terminateDoubleDouble().doubleValue();
   }

   /**
    * Return the result as a <code>DoubleDouble</code>.  This is used mainly
    * when other <code>Aggregators</code> that use this result must maintain a
    * high precision.
    * @return The coefficient of correlation as a <code>DoubleDouble</code>, or
    *    <code>NaN</code> if no values have been accumulated or the variance
    *    for one of the properties is zero.
    * @since 0.4.0
    */
   public DoubleDouble terminateDoubleDouble()
   {
      DoubleDouble covariance = myCovarianceAgg.terminateDoubleDouble();
      DoubleDouble variance1 = myFirstVarAgg.terminateDoubleDouble();
      DoubleDouble variance2 = mySecondVarAgg.terminateDoubleDouble();

      if (covariance.isNaN() || variance1.isNaN() || variance2.isNaN())
         return new DoubleDouble(DoubleDouble.NaN);
      if (variance1.compareTo(DoubleDouble.ZERO) == 0 || variance2.compareTo(DoubleDouble.ZERO) == 0)
         return new DoubleDouble(DoubleDouble.NaN);

      variance1.multiplySelfBy(variance2);
      variance1.sqrtSelf();
      covariance.divideSelfBy(variance1);
      return covariance;
   }
}
