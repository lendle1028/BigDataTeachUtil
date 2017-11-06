package net.sf.jagg;

import net.sf.jagg.model.WindowClause;

/**
 * This class represents the "count" aggregator over any values.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class CountAggregator extends Aggregator implements AnalyticFunction
{
   /**
    * Special pseudo-property indicating "count all", even nulls.
    */
   public static final String COUNT_ALL = "*";

   private long myCount;

   /**
    * Constructs an <code>CountAggregator</code> that operates on the specified
    * property.
    * @param property Count this property's values.
    */
   public CountAggregator(String property)
   {
      setProperty(property);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public CountAggregator replicate()
   {
      return new CountAggregator(getProperty());
   }

   /**
    * Initialize the count to zero.
    */
   public void init()
   {
      myCount = 0;
   }

   /**
    * Count the property if its value is non-null.  If the property is
    * <code>COUNT_ALL</code>, then always count it, null or not.
    *
    * @param value The value to aggregate.
    * @see #COUNT_ALL
    */
   public void iterate(Object value)
   {
      if (value != null)
      {
         String property = getProperty();

         // If the property is "*", then don't even invoke the method.
         // Just count it.
         if (property.equals(COUNT_ALL))
         {
            myCount++;
         }
         else
         {
            Object obj = getValueFromProperty(value, property);
            // Don't count nulls.
            if (obj != null)
            {
               myCount++;
            }
         }
      }
   }

   /**
    * Uncount the property if its value is non-null.  If the property is
    * <code>COUNT_ALL</code>, then always count it, null or not.
    *
    * @param value The value to delete.
    * @see #COUNT_ALL
    * @since 0.9.0
    */
   public void delete(Object value)
   {
      if (value != null)
      {
         String property = getProperty();

         // If the property is "*", then don't even invoke the method.
         // Just un-count it.
         if (property.equals(COUNT_ALL))
         {
            myCount--;
         }
         else
         {
            Object obj = getValueFromProperty(value, property);
            // Don't un-count nulls, which weren't counted in the first place.
            if (obj != null)
            {
               myCount--;
            }
         }
      }
   }

   /**
    * The count function can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * The count function doesn't supply its own window clause.
    * @return <code>null</code>
    * @since 0.9.0
    */
   public WindowClause getWindowClause()
   {
      return null;
   }

   /**
    * Merge the given <code>Aggregator</code> into this one by adding the
    * counts.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof CountAggregator)
      {
         CountAggregator otherAgg = (CountAggregator) agg;
         myCount += otherAgg.myCount;
      }
   }

   /**
    * Return the count.
    *
    * @return The count as a <code>Long</code>.
    */
   public Long terminate()
   {
      return myCount;
   }
}
