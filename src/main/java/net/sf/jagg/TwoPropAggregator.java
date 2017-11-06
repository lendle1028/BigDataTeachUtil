package net.sf.jagg;

/**
 * This abstract class allows for the state necessary to implement aggregate
 * functions over two variables (properties).  The aggregation algorithm is
 * the same as in <code>Aggregator</code>, but <code>TwoPropAggregators</code>
 * have access to two property names.  An example of a
 * <code>TwoPropAggregators</code> is a <code>CovarianceAggregator</code>,
 * which compares samples of two variables to determine the covariance.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public abstract class TwoPropAggregator extends Aggregator
{
   private String myProperty2;

   /**
    * Default constructor is protected so that only subclasses of
    * <code>TwoPropAggregator</code> can be instantiated.
    */
   protected TwoPropAggregator()
   {
      super();
   }

   /**
    * Sets both property <code>Strings</code>.  Subclasses may override
    * this method if they want to extract more information from the property
    * string, e.g. "Name(property, property2, addlInfo)".  The default
    * implementation expects two property names separated by a comma.
    *
    * @param property The property string, with at least one comma separating
    *    two actual property names.
    * @see Aggregator#getProperty()
    * @see #getProperty2()
    */
   @Override
   protected void setProperty(String property)
   {
      String[] fields = property.split(",", -2);
      if (fields.length == 1)
      {
         super.setProperty(property);
         myProperty2 = null;
      }
      else if (fields.length >= 2)
      {
         super.setProperty(fields[0].trim());
         myProperty2 = fields[1].trim();
      }
   }

   /**
    * Retrieves the second property to aggregate.
    *
    * @return A property <code>String</code>.
    */
   public String getProperty2()
   {
      return myProperty2;
   }

   /**
    * A <code>String</code> representation of this
    * <code>TwoPropAggregator</code>.  It takes into account that there are
    * two properties.
    *
    * @return The string representation.
    */
   @Override
   public String toString()
   {
      return getClass().getName() + "(" + getProperty() + "," + getProperty2() + ")";
   }
}
