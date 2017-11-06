package net.sf.jagg;

import java.util.TreeMap;

import net.sf.jagg.exception.ExpectedComparableException;
import net.sf.jagg.util.FrequencyMapUtil;
import net.sf.jagg.model.WindowClause;

/**
 * This class represents the "mode" aggregator over <code>Comparable</code>
 * values.
 *
 * @author Randy Gettman
 * @since 0.6.0
 */
public class ModeAggregator extends Aggregator implements AnalyticFunction
{
   private TreeMap<Comparable<?>, Integer> myIteratedElements;

   /**
    * Constructs a <code>ModeAggregator</code> that operates on the specified
    * property.
    * @param property Determine the statistical mode of this property's values.
    */
   public ModeAggregator(String property)
   {
      setProperty(property);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public ModeAggregator replicate()
   {
      return new ModeAggregator(getProperty());
   }

   /**
    * Initialize an internal list to empty.
    */
   public void init()
   {
      myIteratedElements = new TreeMap<Comparable<?>, Integer>();
   }

   /**
    * Make sure the second property's value is not null, then add the entire
    * <code>Object</code> to an internal list.
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
            // The property must be Comparable.
            Comparable<?> comp = (Comparable<?>) getValueFromProperty(value, property);

            // Don't count nulls.
            if (comp != null)
            {
               FrequencyMapUtil.add(myIteratedElements, comp);
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
    * Make sure the second property's value is not null, then remove the entire
    * <code>Object</code> from an internal list.
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
            // The property must be Comparable.
            Comparable<?> comp = (Comparable<?>) getValueFromProperty(value, property);

            // Don't count nulls.
            if (comp != null)
            {
               FrequencyMapUtil.remove(myIteratedElements, comp);
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
    * The mode function can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * The mode function doesn't supply its own window clause.
    * @return <code>null</code>
    * @since 0.9.0
    */
   public WindowClause getWindowClause()
   {
      return null;
   }

   /**
    * Merge the given <code>Aggregator</code> into this one by adding the
    * contents of the given <code>Aggregator's</code> internal list into this
    * <code>Aggregator's</code> internal list.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof ModeAggregator)
      {
         ModeAggregator otherAgg = (ModeAggregator) agg;
         FrequencyMapUtil.combine(otherAgg.myIteratedElements, myIteratedElements);
      }
   }

   /**
    * Return the value among the values in the specified property that occurs
    * most often (the statistical mode), or any of the modes if there is more
    * than one, with the following algorithm:
    * <ol>
    * <li>The internal tree map keeps values sorted.</li>
    * <li>Walk through the tree map of values, keeping track of the current
    *    value and the current value's frequency.</li>
    * <li>If the current value changes, and the frequency is greater than the
    *    maximum frequency found so far, then note the previous item as the new
    *    mode with the new frequency.</li>
    * <li>Return the mode.</li>
    * </ol>
    *
    * @return The statistical mode.
    * @see java.util.Collections#sort
    * @see net.sf.jagg.util.ComparableComparator
    */
   public Comparable<?> terminate()
   {
      int numItems = myIteratedElements.size();

      if (numItems == 0)
         return null;

      Comparable<?> mode = null;
      int maxFrequency = 0;

      for (Comparable<?> c : myIteratedElements.keySet())
      {
         int frequency = myIteratedElements.get(c);
         if (frequency > maxFrequency)
         {
            maxFrequency = frequency;
            mode = c;
         }
      }
      return mode;
   }
}
