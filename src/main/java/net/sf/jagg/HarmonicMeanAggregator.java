package net.sf.jagg;

import net.sf.jagg.exception.ExpectedNumberException;
import net.sf.jagg.math.DoubleDouble;
import net.sf.jagg.model.WindowClause;

/**
 * This class represents the "harmonic mean" aggregator over numeric values.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class HarmonicMeanAggregator extends Aggregator implements AnalyticFunction
{
   private DoubleDouble mySum = new DoubleDouble();
   private long myCount;
   private long myNumZeroes;

   /**
    * Constructs an <code>HarmonicMeanAggregator</code> that operates on the specified
    * property.
    * @param property Calculate the harmonic mean of this property's values.
    */
   public HarmonicMeanAggregator(String property)
   {
      setProperty(property);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public HarmonicMeanAggregator replicate()
   {
      return new HarmonicMeanAggregator(getProperty());
   }
   
   /**
    * Initialize the sum and count to zero.
    */
   public void init()
   {
      mySum.reset();
      myCount = 0;
      myNumZeroes = 0;
   }

   /**
    * If not null, add the reciprocal of the property value to the sum and
    * count it.
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
               double d = obj.doubleValue();
               if (d == 0.0)
               {
                  myNumZeroes++;
               }
               else
               {
                  DoubleDouble temp = new DoubleDouble(1.0);
                  temp.divideSelfBy(obj.doubleValue());
                  mySum.addToSelf(temp);
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
    * If not null, subtract the reciprocal of the property value from the sum
    * and uncount it.
    *
    * @param value The value to remove.
    * @since 0.9.0
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
                  DoubleDouble temp = new DoubleDouble(1.0);
                  temp.divideSelfBy(obj.doubleValue());
                  mySum.subtractFromSelf(temp);
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
    * The harmonic mean function can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * The harmonic mean function doesn't supply its own window clause.
    * @return <code>null</code>
    * @since 0.9.0
    */
   public WindowClause getWindowClause()
   {
      return null;
   }

   /**
    * Merge the given <code>Aggregator</code> into this one by adding counts
    * and sums.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof HarmonicMeanAggregator)
      {
         HarmonicMeanAggregator otherAgg = (HarmonicMeanAggregator) agg;
         mySum.addToSelf(otherAgg.mySum);
         myCount += otherAgg.myCount;
         myNumZeroes += otherAgg.myNumZeroes;
      }
   }

   /**
    * Return the harmonic mean by dividing the count by the sum.
    *
    * @return The harmonic mean as a <code>Double</code>.  Could return
    *    <code>NaN</code> if no values have been accumulated or if a zero
    *    exists in the values.
    */
   public Double terminate()
   {
      return terminateDoubleDouble().doubleValue();
   }

   /**
    * Return the result as a <code>DoubleDouble</code>.  This is used mainly
    * when other <code>Aggregators</code> that use this result must maintain a
    * high precision.
    * @return The harmonic mean as a <code>DoubleDouble</code>, or
    *    <code>NaN</code> if no values have been accumulated.
    * @since 0.4.0
    */
   public DoubleDouble terminateDoubleDouble()
   {
      if (myCount <= 0 || myNumZeroes > 0 || mySum.compareTo(DoubleDouble.ZERO) == 0)
         return new DoubleDouble(DoubleDouble.NaN);
      DoubleDouble result = new DoubleDouble(myCount);
      result.divideSelfBy(mySum);
      return result;
   }
}
