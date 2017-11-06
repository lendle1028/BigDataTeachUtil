package net.sf.jagg;

import java.util.ArrayList;
import java.util.List;

import net.sf.jagg.model.WindowClause;

/**
 * This class allows represents the "collect" aggregator over any values.
 *
 * @author Randy Gettman
 * @since 0.6.0
 */
public class CollectAggregator extends Aggregator implements AnalyticFunction
{
   private List<Object> myObjects;

   /**
    * Constructs a <code>CollectAggregator</code> that operates on the
    * specified property.
    * @param property Collect this property's values into a <code>List</code>.
    */
   public CollectAggregator(String property)
   {
      setProperty(property);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public CollectAggregator replicate()
   {
      return new CollectAggregator(getProperty());
   }

   /**
    * Initialize the list to empty.
    */
   public void init()
   {
      myObjects = new ArrayList<Object>();
   }

   /**
    * If not null, append the property to the list.
    *
    * @param value The value to aggregate.
    */
   public void iterate(Object value)
   {
      if (value != null)
      {
         String property = getProperty();
         Object obj = getValueFromProperty(value, property);
         // Don't count nulls.
         if (obj != null)
         {
            myObjects.add(obj);
         }
      }
   }

   /**
    * If not null, remove the property from the list.
    *
    * @param value The value to remove.
    * @since 0.9.0
    */
   public void delete(Object value)
   {
      if (value != null)
      {
         String property = getProperty();
         Object obj = getValueFromProperty(value, property);
         // Don't count nulls.
         if (obj != null)
         {
            myObjects.remove(obj);
         }
      }
   }

   /**
    * The collect function can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * The collect function doesn't supply its own window clause.
    * @return <code>null</code>
    * @since 0.9.0
    */
   public WindowClause getWindowClause()
   {
      return null;
   }

   /**
    * Merge the given <code>Aggregator</code> into this one by adding all
    * elements of the other list to this one.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof CollectAggregator)
      {
         CollectAggregator otherAgg = (CollectAggregator) agg;
         myObjects.addAll(otherAgg.myObjects);
      }
   }

   /**
    * Return the list.
    *
    * @return The <code>List</code> of objects.
    */
   public List<Object> terminate()
   {
      return myObjects;
   }
}
