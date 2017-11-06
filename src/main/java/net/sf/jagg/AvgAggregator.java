package net.sf.jagg;

import net.sf.jagg.exception.ExpectedNumberException;
import net.sf.jagg.math.DoubleDouble;
import net.sf.jagg.model.WindowClause;

/**
 * This class represents the "avg" aggregator over numeric values.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class AvgAggregator extends Aggregator implements AnalyticFunction
{
   private DoubleDouble mySum = new DoubleDouble();
   private long   myCount;

   /**
    * Constructs an <code>AvgAggregator</code> that operates on the specified
    * property.
    * @param property Average this property's values.
    */
   public AvgAggregator(String property)
   {
      setProperty(property);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public AvgAggregator replicate()
   {
      return new AvgAggregator(getProperty());
   }

   /**
    * Initialize the sum and count to zero.
    */
   public void init()
   {
      mySum.reset();
      myCount = 0;
   }

   /**
    * If not null, add the property to the sum and count it.
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
               mySum.addToSelf(obj.doubleValue());
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
    * If not null, subtract the property from the sum and un-count it.
    *
    * @param value The value to delete.
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
            // Don't un-count nulls.
            if (obj != null)
            {
               myCount--;
               mySum.subtractFromSelf(obj.doubleValue());
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
    * The average function can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * The average function doesn't supply its own window clause.
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
      if (agg != null && agg instanceof AvgAggregator)
      {
         AvgAggregator otherAgg = (AvgAggregator) agg;
         mySum.addToSelf(otherAgg.mySum);
         myCount += otherAgg.myCount;
      }
   }

   /**
    * Return the average by dividing the sum by the count.
    *
    * @return The average as a <code>Double</code>, or <code>NaN</code> if no
    *    values have been accumulated.
    */
   public Double terminate()
   {
      return terminateDoubleDouble().doubleValue();
   }

   /**
    * Return the result as a <code>DoubleDouble</code>.  This is used mainly
    * when other <code>Aggregators</code> that use this result must maintain a
    * high precision.
    * @return The average as a <code>Double</code>, or <code>NaN</code> if no
    *    values have been accumulated.
    * @since 0.4.0
    */
   public DoubleDouble terminateDoubleDouble()
   {
      if (myCount > 0)
      {
         DoubleDouble result = new DoubleDouble(mySum);
         result.divideSelfBy(myCount);
         return result;
      }
      return DoubleDouble.NaN;
   }
}
