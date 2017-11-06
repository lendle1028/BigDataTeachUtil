package net.sf.jagg;

import java.util.TreeMap;

import net.sf.jagg.exception.ExpectedComparableException;
import net.sf.jagg.util.FrequencyMapUtil;
import net.sf.jagg.model.WindowClause;

/**
 * This class represents the "max" analytic function over <code>Comparable</code>
 * values.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class MaxAnalyticAggregator extends MaxAggregator implements AnalyticFunction
{
   /**
    * A TreeMap is needed here, so that all of the following operations' costs
    * are minimized: add O(log n), remove O(log n), and find max O(log n).
    */
   private TreeMap<Comparable, Integer> myIteratedElements;

   /**
    * Constructs a <code>MaxAnalyticAggregator</code> that operates on the
    * specified property.
    * @param property Determine the maximum of this property's values.
    */
   public MaxAnalyticAggregator(String property)
   {
      super(property);
      myIteratedElements = new TreeMap<Comparable, Integer>();
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public MaxAnalyticAggregator replicate()
   {
      return new MaxAnalyticAggregator(getProperty());
   }

   /**
    * In addition to what the superclass initialization does, clear this
    * object's own iterated elements collection.
    */
   public void init()
   {
      super.init();
      myIteratedElements.clear();
   }

   /**
    * In addition to what the superclass iteration does, add the property value
    * to this object's own iterated elements collection.
    *
    * @param value The value to aggregate.
    */
   @SuppressWarnings("unchecked")
   public void iterate(Object value)
   {
      super.iterate(value);

      if (value != null)
      {
         String property = getProperty();
         try
         {
            Comparable obj = (Comparable) getValueFromProperty(value, property);
            // Don't count nulls.
            if (obj != null)
            {
               FrequencyMapUtil.add(myIteratedElements, obj);
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
    * Remove the property value from this object's own iterated elements
    * collection.
    *
    * @param value The value to remove.
    */
   @SuppressWarnings("unchecked")
   public void delete(Object value)
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
               Integer frequency = myIteratedElements.get(obj);
               if (frequency != null)
               {
                  if (!FrequencyMapUtil.remove(myIteratedElements, obj))
                  {
                     // Get the new max, which only needs to be done if a distinct
                     // element is removed because the frequency decreased to zero.
                     Comparable currMax = getMax();
                     if (currMax != null && currMax.compareTo(obj) == 0)
                     {
                        if (myIteratedElements.isEmpty())
                           setMax(null);
                        else
                           setMax(myIteratedElements.lastKey());
                     }
                  }
               }
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
    * The max function can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * The max function doesn't supply its own window clause.
    * @return <code>null</code>
    * @since 0.9.0
    */
   public WindowClause getWindowClause()
   {
      return null;
   }

   /**
    * In addition to what the superclass merging does, add the other
    * aggregator's iterated elements to this object's own iterated elements
    * collection.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   @SuppressWarnings("unchecked")
   public void merge(AggregateFunction agg)
   {
      super.merge(agg);
      if (agg != null && agg instanceof MaxAnalyticAggregator)
      {
         MaxAnalyticAggregator otherAgg = (MaxAnalyticAggregator) agg;
         FrequencyMapUtil.combine(otherAgg.myIteratedElements, myIteratedElements);
      }
   }
}
