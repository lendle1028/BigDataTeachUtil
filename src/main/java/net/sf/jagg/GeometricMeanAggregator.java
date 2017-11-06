package net.sf.jagg;

import net.sf.jagg.exception.ExpectedNumberException;
import net.sf.jagg.math.DoubleDouble;
import net.sf.jagg.model.WindowClause;

/**
 * This class represents the "geometric mean" aggregator over numeric values.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class GeometricMeanAggregator extends Aggregator implements AnalyticFunction
{
   private DoubleDouble myProduct = new DoubleDouble();
   private long myCount;
   private int myNumZeroes;

   /**
    * Constructs an <code>GeometricMeanAggregator</code> that operates on the specified
    * property.
    * @param property Calculate the geometric mean of this property's values.
    */
   public GeometricMeanAggregator(String property)
   {
      setProperty(property);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public GeometricMeanAggregator replicate()
   {
      return new GeometricMeanAggregator(getProperty());
   }
   
   /**
    * Initialize the product to one and count to zero.
    */
   public void init()
   {
      myProduct.reset();
      myProduct.addToSelf(1.0);
      myCount = 0;
      myNumZeroes = 0;
   }

   /**
    * If not null, multiply the property value into the product and count it.
    *
    * @param value The value to aggregate.
    */
   public void iterate(Object value)
   {
      if (value != null)
      {
         String property = getProperty();
         try
         {
            Number obj = (Number) getValueFromProperty(value, property);
            // Don't count nulls.
            if (obj != null)
            {
               myCount++;
               if (obj.doubleValue() == 0.0)
               {
                  myNumZeroes++;
               }
               else
               {
                  myProduct.multiplySelfBy(obj.doubleValue());
               }
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
    * If not null, divide the property value out of the product and unccount it.
    *
    * @param value The value to remove.
    */
   public void delete(Object value)
   {
      if (value != null)
      {
         String property = getProperty();
         try
         {
            Number obj = (Number) getValueFromProperty(value, property);
            // Don't count nulls.
            if (obj != null)
            {
               myCount--;
               double d = obj.doubleValue();
               if (d == 0.0)
               {
                  myNumZeroes--;
               }
               else
               {
                  myProduct.divideSelfBy(d);
               }
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
    * The gemoetric mean function can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * The geometric mean function doesn't supply its own window clause.
    * @return <code>null</code>
    * @since 0.9.0
    */
   public WindowClause getWindowClause()
   {
      return null;
   }

   /**
    * Merge the given <code>Aggregator</code> into this one by multiplying
    * products and adding sums.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof GeometricMeanAggregator)
      {
         GeometricMeanAggregator otherAgg = (GeometricMeanAggregator) agg;
         myProduct.multiplySelfBy(otherAgg.myProduct);
         myCount += otherAgg.myCount;
         myNumZeroes += otherAgg.myNumZeroes;
      }
   }

   /**
    * Return the geometric mean by taking the <em>n</em>th root of the product
    * of all values, where <em>n</em> is the count of all non-null values.
    *
    * @return The geometric mean as a <code>Double</code>.  Could return
    *    <code>NaN</code> if no values have been accumulated.
    */
   public Double terminate()
   {
      return terminateDoubleDouble().doubleValue();
   }

   /**
    * Return the result as a <code>DoubleDouble</code>.  This is used mainly
    * when other <code>Aggregators</code> that use this result must maintain a
    * high precision.
    * @return The geometric mean as a <code>DoubleDouble</code>, or
    *    <code>NaN</code> if no values have been accumulated.
    * @since 0.4.0
    */
   public DoubleDouble terminateDoubleDouble()
   {
      if (myCount > 0)
      {
         if (myNumZeroes > 0)
         {
            return new DoubleDouble(0.0);
         }
         DoubleDouble result = new DoubleDouble(myProduct);
         result.nthRootSelf(myCount);
         return result;
      }
      return new DoubleDouble(DoubleDouble.NaN);
   }
}
