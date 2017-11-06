package net.sf.jagg;

import net.sf.jagg.exception.ParseException;
import net.sf.jagg.model.WindowClause;
import net.sf.jagg.util.PropertyParser;

/**
 * This abstract class represents lag/lead-like analytic calculations over
 * any values.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public abstract class AbstractOffsetAnalytic extends MultiPropAggregator implements AnalyticFunction
{
   private int myOffset;
   private Object myDefaultValue;
   private Object myCurrentValue;

   /**
    * Constructs an <code>AbstractOffsetAnalytic</code> on the specified
    * properties, in the format: <code>property[, offset [, defaultValue]]</code>.
    * @param properties A specification string in the format:
    *    <code>property[, offset [, defaultValue]]</code>.
    */
   protected AbstractOffsetAnalytic(String properties)
   {
      setProperty(properties);
   }

   /**
    * Constructs an <code>AbstractOffsetAnalytic</code> on the specified
    * property and with the given offset.  The default value is <code>null</code>.
    * @param property The property.
    * @param offset The offset, in number of rows from the current row.
    */
   protected AbstractOffsetAnalytic(String property, int offset)
   {
      this(property, offset, null);
   }

   /**
    * Constructs an <code>AbstractOffsetAnalytic</code> on the specified
    * property, with the given offset, and the given default value.
    * @param property The property.
    * @param offset The offset, in number of rows from the current row.
    * @param defaultValue The default value.
    */
   protected AbstractOffsetAnalytic(String property, int offset, Object defaultValue)
   {
      this(property + ", " + offset + ((defaultValue != null) ? (", " + defaultValue) : ""));
   }

   /**
    * Extracts the first property as the actual property, the second property
    * into the offset (default specified by a subclass), and the third property
    * into the default value (default null).
    * @param properties A specification string in the format:
    *    <code>property[, offset [, defaultValue]]</code>.
    */
   @Override
   public void setProperty(String properties)
   {
      super.setProperty(properties);

      int numProperties = getNumProperties();
      switch(numProperties)
      {
         case 1:
            myOffset = getDefaultOffset();
            myDefaultValue = null;
            break;
         case 2:
            extractOffset();
            myDefaultValue = null;
            break;
         case 3:
            extractOffset();
            extractDefaultValue();
            break;
         default:
            throw new ParseException(
                    "Properties must be in the format \"(property [, offset [, defaultValue]])\"");
      }
   }

   /**
    * Extracts the offset from the second property.  It is expected to be an
    * integer.
    */
   protected void extractOffset()
   {
      try
      {
         myOffset = Integer.parseInt(getProperty(1).trim());
      }
      catch (NumberFormatException e)
      {
         throw new ParseException("Offset must be an integer: " + getProperty(1), e);
      }
   }

   /**
    * Extracts the typed parameter from the third property.
    */
   protected void extractDefaultValue()
   {
      String text = getProperty(2).trim();
      if (text.startsWith("\"") && text.endsWith("\""))
      {
         myDefaultValue = PropertyParser.getTypedParameter(text.substring(1, text.length() - 1), true);
      }
      else
      {
         myDefaultValue = PropertyParser.getTypedParameter(text, false);
      }
   }

   /**
    * Returns the default offset, in number of rows, from the current row being
    * evaluated.  It can be zero or negative.
    * 
    * @return The default offset, in number of rows.
    */
   protected int getDefaultOffset()
   {
      return 1;
   }

   /**
    * Returns the offset, in number of rows from the current row.
    * @return The offset, in number of rows from the current row.
    */
   protected int getOffset()
   {
      return myOffset;
   }

   /**
    * Returns the default value.
    * @return The default value.
    */
   protected Object getDefaultValue()
   {
      return myDefaultValue;
   }

   /**
    * Initialize the property value to the default value.
    */
   public void init()
   {
      myCurrentValue = myDefaultValue;
   }

   /**
    * Store the property value.
    *
    * @param value The value to aggregate.
    */
   public void iterate(Object value)
   {
      if (value != null)
      {
         String property = getProperty();
         myCurrentValue = getValueFromProperty(value, property);
      }
   }

   /**
    * Remove the property value.
    *
    * @param value The value to delete.
    */
   public void delete(Object value)
   {
      if (value != null)
      {
         String property = getProperty();
         Object obj = getValueFromProperty(value, property);
         if ((obj == null && myCurrentValue == null) ||
             (obj != null && (obj == myCurrentValue || obj.equals(myCurrentValue))))
         {
            myCurrentValue = myDefaultValue;
         }
         // Ignore values that don't match.  They could be deleted after
         // another item was already iterated.  That would have effectively
         // deleted the item already.
      }
   }

   /**
    * The offset analytic functions don't take window clauses.
    * @return <code>false</code>.
    */
   public boolean takesWindowClause()
   {
      return false;
   }

   /**
    * Returns the <code>WindowClause</code>, based on the offset.
    * @return The <code>WindowClause</code>, based on the offset.
    */
   public abstract WindowClause getWindowClause();

   /**
    * Merge the given <code>Aggregator</code> into this one.  Not supported
    * (yet).
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    * @throws UnsupportedOperationException Unimplemented.
    */
   public void merge(AggregateFunction agg)
   {
      // TODO: Decide how to implement this when adding parallel support for analytics.
      throw new UnsupportedOperationException("Merge not implemented!");
   }

   /**
    * Returns the currently stored value.
    *
    * @return The currently stored value, or the default value if no value is
    *    currently being stored.
    */
   public Object terminate()
   {
      return myCurrentValue;
   }
}
