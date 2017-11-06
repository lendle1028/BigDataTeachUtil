package net.sf.jagg;

/**
 * This abstract class allows the implementation of analytic functions over no
 * variables (properties).  The analytic algorithm is
 * the same as in <code>AnalyticAggregator</code>, but <code>NoPropAnlaytics</code>
 * don't allow any property names (<code>null</code> or an empty string
 * <code>""</code> are acceptable as "property" names).  No-property
 * <code>Aggregtors</code> don't make sense by themselves; they only make sense
 * as an <code>AnalyticFunction</code>, so this implements
 * <code>AnalyticFunction</code>.  An example of a  <code>NoPropAnalytic</code>
 * is a <code>RowNumberAnalytic</code>, which assigns row numbers through its
 * partitions and orderings, regardless of the value(s).
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public abstract class NoPropAnalytic extends Aggregator implements AnalyticFunction
{
   /**
    * Default constructor is protected so that only subclasses of
    * <code>NoPropAnalytic</code> can be instantiated.
    */
   protected NoPropAnalytic()
   {
      super();
   }

   /**
    * Sets no property <code>Strings</code>.  Enforces that any property passed
    * in here is either <code>null</code> or an empty string <code>""</code>.
    *
    * @param property The property string, expected tobe either <code>null</code>
    *     or an empty string <code>""</code>.
    */
   @Override
   protected void setProperty(String property)
   {
      if (property != null && property.length() > 0)
         throw new IllegalArgumentException("No property expected: " + property);
   }

   /**
    * There is no property; throws an <code>UnsupportedOperationException</code>.
    * 
    * @return Doesn't return normaly.
    * @throws UnsupportedOperationException There is no property.
    */
   @Override
   public String getProperty()
   {
      throw new UnsupportedOperationException("No property is supported!");
   }

   /**
    * A <code>String</code> representation of this
    * <code>NoPropAnalytic</code>.  It displays no property.
    *
    * @return The string representation.
    */
   @Override
   public String toString()
   {
      return getClass().getName() + "()";
   }
}
