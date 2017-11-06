package net.sf.jagg;

import net.sf.jagg.math.DoubleDouble;
import net.sf.jagg.model.LinearRegressionStats;
import net.sf.jagg.model.WindowClause;

/**
 * This class represents the "linear regression" aggregator over two sets of
 * numeric values.  Many values can be returned by this
 * <code>Aggregator</code>, all encapsulated in the object
 * <code>LinearRegressionStats</code>.
 *
 * @author Randy Gettman
 * @since 0.1.0
 *
 * @see LinearRegressionStats
 */
public class LinearRegressionAggregator extends TwoPropAggregator implements AnalyticFunction
{
   private CovariancePopAggregator myCovarianceAgg = null;
   private VariancePopAggregator myFirstVarAgg = null;
   private VariancePopAggregator mySecondVarAgg = null;
   private AvgAggregator myFirstAvgAgg = null;
   private AvgAggregator mySecondAvgAgg = null;

   private long myCount;

   /**
    * Constructs a <code>LinearRegressionAggregator</code> on the specified
    * properties, in the format: <code>property, property2</code>.
    * @param properties A specification string in the format:
    *    <code>property, property2</code>.
    */
   public LinearRegressionAggregator(String properties)
   {
      setProperty(properties);
   }

   /**
    * Constructs a <code>LinearRegressionAggregator</code> that operates on the specified
    * properties.
    * @param property Calculate linear regression statistics of this property with the other.
    * @param property2 Calculate linear regression statistics of this property with the other.
    */
   public LinearRegressionAggregator(String property, String property2)
   {
      setProperty(property + "," + property2);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public LinearRegressionAggregator replicate()
   {
      return new LinearRegressionAggregator(getProperty(), getProperty2());
   }

   /**
    * Initialize the internal aggregators: A <code>CovarianceAggregator</code>,
    * 2 <code>VarianceAggregators</code> (one each for both properties), and 2
    * <code>AvgAggregators</code> (one each for both properties).  Initialize a
    * count to zero.
    *
    * @see CovariancePopAggregator
    * @see VariancePopAggregator
    * @see AvgAggregator
    */
   public void init()
   {
      if (myCovarianceAgg == null)
         myCovarianceAgg = new CovariancePopAggregator(getProperty(), getProperty2());
      if (myFirstVarAgg == null)
         myFirstVarAgg = new VariancePopAggregator(getProperty());
      if (mySecondVarAgg == null)
         mySecondVarAgg = new VariancePopAggregator(getProperty2());
      if (myFirstAvgAgg == null)
         myFirstAvgAgg = new AvgAggregator(getProperty());
      if (mySecondAvgAgg == null)
         mySecondAvgAgg = new AvgAggregator(getProperty2());

      myCovarianceAgg.init();
      myFirstVarAgg.init();
      mySecondVarAgg.init();
      myFirstAvgAgg.init();
      mySecondAvgAgg.init();

      myCount = 0;
   }

   /**
    * If both property values are non-null, then iterate the internal
    * aggregators and increment the count.
    *
    * @param value The value to aggregate.
    */
   public void iterate(Object value)
   {
      // Do the count here to detect only when both properties are non-null.
      if (value != null)
      {
         String property1 = getProperty();
         String property2 = getProperty2();

         Object obj1 = getValueFromProperty(value, property1);
         Object obj2 = getValueFromProperty(value, property2);
         // Don't count nulls.
         if (obj1 != null && obj2 != null)
         {
            myCount++;
            // Only iterate our internal aggregators if both values are
            // non-null.
            myCovarianceAgg.iterate(value);
            myFirstVarAgg.iterate(value);
            mySecondVarAgg.iterate(value);
            myFirstAvgAgg.iterate(value);
            mySecondAvgAgg.iterate(value);
         }
      }
   }

   /**
    * If both property values are non-null, then delete from the internal
    * aggregators and decrement the count.
    *
    * @param value The value to delete.
    * @since 0.9.0
    */
   public void delete(Object value)
   {
      // Do the count here to detect only when both properties are non-null.
      if (value != null)
      {
         String property1 = getProperty();
         String property2 = getProperty2();

         Object obj1 = getValueFromProperty(value, property1);
         Object obj2 = getValueFromProperty(value, property2);
         // Don't count nulls.
         if (obj1 != null && obj2 != null)
         {
            myCount--;
            // Only iterate our internal aggregators if both values are
            // non-null.
            myCovarianceAgg.delete(value);
            myFirstVarAgg.delete(value);
            mySecondVarAgg.delete(value);
            myFirstAvgAgg.delete(value);
            mySecondAvgAgg.delete(value);
         }
      }
   }

   /**
    * The linear regression function can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * The linear regression function doesn't supply its own window clause.
    * @return <code>null</code>
    * @since 0.9.0
    */
   public WindowClause getWindowClause()
   {
      return null;
   }

   /**
    * Merge the given <code>Aggregator</code> into this one.  Add the internal
    * counts.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof LinearRegressionAggregator)
      {
         LinearRegressionAggregator otherAgg = (LinearRegressionAggregator) agg;
         myCovarianceAgg.merge(otherAgg.myCovarianceAgg);
         myFirstVarAgg.merge(otherAgg.myFirstVarAgg);
         mySecondVarAgg.merge(otherAgg.mySecondVarAgg);
         myFirstAvgAgg.merge(otherAgg.myFirstAvgAgg);
         mySecondAvgAgg.merge(otherAgg.mySecondAvgAgg);

         myCount += otherAgg.myCount;
      }
   }

   /**
    * Return a <code>LinearRegressionStats</code>, with the following
    * calculations:
    * <ul>
    * <li>slope = cov(prop1, prop2) / var(prop2)
    * <li>intercept = avg(prop1) - slope * avg(prop2)
    * <li>count = number of pairs where both elements were non-null, and thus
    *    counted in these calculations.
    * <li>rSquared = if (var(prop2)) is 0, then NaN,<br>
    *    else if (var(prop1)) is 0, then 1,<br>
    *    else correlation(prop1, prop2) squared.
    * <li>correlation = covariancePop(prop1, prop2) / Math.sqrt(variancePop(prop1) * variancePop(prop2))
    * <li>avg1 = avg(prop1)
    * <li>avg2 = avg(prop2)
    * </ul>
    *
    * @return A <code>LinearRegressionStats</code>.
    */
   public LinearRegressionStats terminate()
   {
      DoubleDouble covariance = myCovarianceAgg.terminateDoubleDouble();
      DoubleDouble variance1 = myFirstVarAgg.terminateDoubleDouble();
      DoubleDouble variance2 = mySecondVarAgg.terminateDoubleDouble();
      DoubleDouble avg1 = myFirstAvgAgg.terminateDoubleDouble();
      DoubleDouble avg2 = mySecondAvgAgg.terminateDoubleDouble();

      if (myCount <= 0)
      {
         return new LinearRegressionStats(Double.NaN, Double.NaN, 0, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
      }
      
      // Same calculation that the CorrelationAggregator makes.
      DoubleDouble correlation;
      if (variance1.compareTo(DoubleDouble.ZERO) == 0 || variance2.compareTo(DoubleDouble.ZERO) == 0)
         correlation = DoubleDouble.NaN;
      else
      {
         correlation = new DoubleDouble(covariance);
         DoubleDouble temp = new DoubleDouble(variance1);
         temp.multiplySelfBy(variance2);
         temp.sqrtSelf();
         correlation.divideSelfBy(temp);
      }

      double slope, intercept, rSquared;
      if (variance2.compareTo(DoubleDouble.ZERO) == 0)
      {
         slope = Double.NaN;
         intercept = Double.NaN;
         rSquared = Double.NaN;
      }
      else if (variance1.compareTo(DoubleDouble.ZERO) == 0)
      {
         slope = 0;
         intercept = avg1.doubleValue();
         rSquared = 1;
      }
      else
      {
         DoubleDouble temp = new DoubleDouble(covariance);
         temp.divideSelfBy(variance2);
         slope = temp.doubleValue();

         temp.multiplySelfBy(avg2);
         temp.negateSelf();
         temp.addToSelf(avg1);
         intercept = temp.doubleValue();

         temp = new DoubleDouble(correlation);
         temp.squareSelf();
         rSquared = temp.doubleValue();
      }

      return new LinearRegressionStats(slope, intercept, myCount, rSquared,
         correlation.doubleValue(), avg1.doubleValue(), avg2.doubleValue());
   }
}
