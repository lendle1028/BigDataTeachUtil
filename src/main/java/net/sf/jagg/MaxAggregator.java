package net.sf.jagg;

import net.sf.jagg.exception.ExpectedComparableException;

/**
 * This class represents the "max" aggregator over <code>Comparable</code>
 * values.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class MaxAggregator extends Aggregator
{
   private Comparable myMax;

   /**
    * Constructs a <code>MaxAggregator</code> that operates on the specified
    * property.
    * @param property Determine the maximum of this property's values.
    */
   public MaxAggregator(String property)
   {
      setProperty(property);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public MaxAggregator replicate()
   {
      return new MaxAggregator(getProperty());
   }
   
   /**
    * Initialize the maximum to <code>null</code>.
    */
   public void init()
   {
      myMax = null;
   }

   /**
    * Store the property value if it's higher than the current maximum.
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
               if (myMax == null || obj.compareTo(myMax) > 0)
                  myMax = obj;
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
    * maximum of the two maximums.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   @SuppressWarnings("unchecked")
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof MaxAggregator)
      {
         MaxAggregator otherAgg = (MaxAggregator) agg;
         if (otherAgg.myMax != null)
         {
            if (myMax == null || otherAgg.myMax.compareTo(myMax) > 0)
               myMax = otherAgg.myMax;
         }
      }
   }

   /**
    * Return the maximum.
    *
    * @return The maximum as a <code>Comparable</code>, or <code>null</code> if
    *    no values were processed.
    */
   public Comparable terminate()
   {
      return myMax;
   }

   /**
    * Returns the current maximum iterated element.
    * @return The current maximum iterated element.
    * @since 0.9.0
    */
   protected final Comparable getMax()
   {
      return myMax;
   }

   /**
    * Sets the current maximum iterated element.
    * @param max The current maximum iterated element.
    */
   protected final void setMax(Comparable max)
   {
      myMax = max;
   }
}
