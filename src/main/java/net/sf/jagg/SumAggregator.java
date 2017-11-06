package net.sf.jagg;

import net.sf.jagg.exception.ExpectedNumberException;
import net.sf.jagg.math.DoubleDouble;
import net.sf.jagg.model.WindowClause;

/**
 * This class represents the "sum" aggregator over numeric values.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class SumAggregator extends Aggregator implements AnalyticFunction
{
   private DoubleDouble mySum = new DoubleDouble();

   /**
    * Constructs an <code>SumAggregator</code> that operates on the specified
    * property.
    * @param property Add up all this property's values.
    */
   public SumAggregator(String property)
   {
      setProperty(property);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public SumAggregator replicate()
   {
      return new SumAggregator(getProperty());
   }

   /**
    * Initialize the sum to zero.
    */
   public void init()
   {
      mySum.reset();
   }

   /**
    * Add the property value to the sum.
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
    * Subtract the property value from the sum.
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
    * The sum function can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * The sum function doesn't supply its own window clause.
    * @return <code>null</code>
    * @since 0.9.0
    */
   public WindowClause getWindowClause()
   {
      return null;
   }

   /**
    * Merge the given <code>Aggregator</code> into this one by adding the
    * respective sums.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof SumAggregator)
      {
         SumAggregator otherAgg = (SumAggregator) agg;
         mySum.addToSelf(otherAgg.mySum);
      }
   }

   /**
    * Return the sum.
    * 
    * @return The sum as a <code>Double</code>, or <code>0</code> if
    *    no values have been accumulated.
    */
   public Double terminate()
   {
      return terminateDoubleDouble().doubleValue();
   }

   /**
    * Return the result as a <code>DoubleDouble</code>.  This is used mainly
    * when other <code>Aggregators</code> that use this result must maintain a
    * high precision.
    * @return The sum as a <code>DoubleDouble</code>, or <code>0</code> if
    *    no values have been accumulated.
    * @since 0.4.0
    */
   public DoubleDouble terminateDoubleDouble()
   {
      return new DoubleDouble(mySum);
   }
}
