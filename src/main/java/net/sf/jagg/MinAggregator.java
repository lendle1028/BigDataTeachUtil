package net.sf.jagg;

import net.sf.jagg.exception.ExpectedComparableException;

/**
 * This class represents the "min" aggregator over <code>Comparable</code>
 * values.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class MinAggregator extends Aggregator
{
   private Comparable myMin;

   /**
    * Constructs a <code>MinAggregator</code> that operates on the specified
    * property.
    * @param property Determine the minimum of this property's values.
    */
   public MinAggregator(String property)
   {
      setProperty(property);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public MinAggregator replicate()
   {
      return new MinAggregator(getProperty());
   }

   /**
    * Initialize the minimum to <code>null</code>.
    */
   public void init()
   {
      myMin = null;
   }

   /**
    * Store the property value if it's lower than the current minimum.
    *
    * @param value The value to aggregate.
    */
   @SuppressWarnings("unchecked")
   public void iterate(Object value)
   {
      if (value != null)
      {
         String property = getProperty();
         try
         {
            Comparable obj = (Comparable) getValueFromProperty(value, property);
            // Don't count nulls.
            if (obj != null)
            {
               if (myMin == null || obj.compareTo(myMin) < 0)
                  myMin = obj;
            }
         }
         catch (ClassCastException e)
         {
            throw new ExpectedComparableException("Property \"" + property +
               "\" must be Comparable.", e);
         }
      }
   }

   /**
    * Merge the given <code>Aggregator</code> into this one by taking the
    * minimum of the two minimums.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   @SuppressWarnings("unchecked")
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof MinAggregator)
      {
         MinAggregator otherAgg = (MinAggregator) agg;
         if (otherAgg.myMin != null)
         {
            if (myMin == null || otherAgg.myMin.compareTo(myMin) < 0)
               myMin = otherAgg.myMin;
         }
      }
   }

   /**
    * Return the minimum.
    *
    * @return The minimum as a <code>Comparable</code>, or <code>null</code> if
    *    no values were processed.
    */
   public Comparable terminate()
   {
      return myMin;
   }

   /**
    * Returns the current minimum iterated element.
    * @return The current minimum iterated element.
    * @since 0.9.0
    */
   protected final Comparable getMin()
   {
      return myMin;
   }

   /**
    * Sets the current minimum iterated element.
    * @param min The current minimum iterated element.
    */
   protected final void setMin(Comparable min)
   {
      myMin = min;
   }
}
