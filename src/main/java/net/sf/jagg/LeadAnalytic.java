package net.sf.jagg;

import net.sf.jagg.model.WindowClause;

/**
 * This class represents the "lead" analytic function over any values.  It
 * returns a property value from a different "row" of values.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class LeadAnalytic extends AbstractOffsetAnalytic
{
   /**
    * Constructs a <code>LeadAnalytic</code> on the specified
    * properties, in the format: <code>property[, offset [, defaultValue]]</code>.
    * @param properties A specification string in the format:
    *    <code>property[, offset [, defaultValue]]</code>.
    */
   public LeadAnalytic(String properties)
   {
      super(properties);
   }

   /**
    * Constructs a <code>LeadAnalytic</code> on the specified
    * property and with the given offset.  The default value is <code>null</code>.
    * @param property The property.
    * @param offset The offset, in number of rows ahead of the current row.
    */
   public LeadAnalytic(String property, int offset)
   {
      this(property, offset, null);
   }

   /**
    * Constructs a <code>LeadAnalytic</code> on the specified
    * property, with the given offset, and the given default value.
    * @param property The property.
    * @param offset The offset, in number of rows ahead of the current row.
    * @param defaultValue The default value.
    */
   public LeadAnalytic(String property, int offset, Object defaultValue)
   {
      this(property + ", " + offset + ((defaultValue != null) ? (", " + defaultValue) : ""));
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public AggregateFunction replicate()
   {
      return new LeadAnalytic(getProperty(), getOffset(), getDefaultValue());
   }

   /**
    * Returns a window that specifies only the row <code>offset</code> ahead of
    * the current row.
    * @return A <code>WindowClause</code>.
    */
   public WindowClause getWindowClause()
   {
      return new WindowClause(WindowClause.Type.ROWS, -getOffset(), getOffset());
   }
}