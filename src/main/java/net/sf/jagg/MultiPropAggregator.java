package net.sf.jagg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This abstract class allows for the state necessary to implement aggregate
 * functions over any number of variables (properties).  The aggregation
 * algorithm is the same as in <code>Aggregator</code>, but
 * <code>MultiPropAggregators</code> have access to a <code>List</code> of
 * properties.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public abstract class MultiPropAggregator extends Aggregator
{
   private List<String> myProperties;

   /**
    * Default constructor is protected so that only subclasses of
    * <code>MultiPropAggregator</code> can be instantiated.
    */
   protected MultiPropAggregator()
   {
      super();
   }

   /**
    * Sets any number of property <code>Strings</code>.  The default
    * implementation expects propert names separated by commas.
    *
    * @param property The property <code>String</code>, with commas
    *    separating multiple actual property names.
    * @see Aggregator#getProperty()
    * @see #getProperty(int)
    */
   @Override
   protected void setProperty(String property)
   {
      String[] fields = property.split(",", 0);
      if (fields != null)
      {
         myProperties = Arrays.asList(fields);
         if (fields.length >= 1)
            super.setProperty(fields[0]);
      }
      else
      {
         myProperties = new ArrayList<String>();
      }
   }

   /**
    * Retrieves the property name specified by the given zero-based index.
    *
    * @param index The zero-based index.
    * @return The property name.
    */
   public String getProperty(int index)
   {
      return myProperties.get(index);
   }

   /**
    * Returns the number of properties.
    * @return The number of properties.
    */
   public int getNumProperties()
   {
      return myProperties.size();
   }

   /**
    * A <code>String</code> representation of this
    * <code>MultiPropAggregator</code>.  It takes into account that there are
    * multiple properties.
    *
    * @return The string representation.
    */
   @Override
   public String toString()
   {
      StringBuilder buf = new StringBuilder();
      buf.append(getClass().getName());
      buf.append("(");

      int size = myProperties.size();
      for (int i = 0; i < size; i++)
      {
         String property = myProperties.get(i);
         if (i > 0)
            buf.append(",");
         buf.append(property);
      }
      buf.append(")");

      return buf.toString();
   }
}
