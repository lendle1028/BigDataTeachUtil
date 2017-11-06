package net.sf.jagg;

import net.sf.jagg.model.WindowClause;

/**
 * This class represents the "concatenation" aggregator over any values for
 * which <code>toString</code> is well-defined, with an optional separator
 * <code>String</code> between values.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class ConcatAggregator extends Aggregator implements AnalyticFunction
{
   private StringBuilder myBuf;
   private String mySeparator;
   private boolean hasContent;

   /**
    * Constructs a <code>ConcatAggregator</code> that operates on the specified
    * property, with an empty separator string.
    * @param property Concatenate this property's values.
    */
   public ConcatAggregator(String property)
   {
      setProperty(property);
   }

   /**
    * Constructs a <code>ConcatAggregator</code> that operates on the specified
    * property, with the given separator string.
    * @param property Concatenate this property's values.
    * @param separator The separator <code>String</code>.
    */
   public ConcatAggregator(String property, String separator)
   {
      setProperty(property);
      mySeparator = separator;
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public ConcatAggregator replicate()
   {
      return new ConcatAggregator(getProperty(), mySeparator);
   }

   /**
    * Extracts a possible separator <code>String</code> from the property in
    * the format: property, "separator".
    *
    * @param property A property name and possible separator.
    */
   @Override
   protected void setProperty(String property)
   {
      String[] fields = property.split(",", 2);
      if (fields.length == 1)
      {
         super.setProperty(property);
         mySeparator = "";
      }
      else if (fields.length >= 2)
      {
         super.setProperty(fields[0]);
         String sepExpression = fields[1].trim();

         // Lose any optionally enclosing double-quotes.
         if (sepExpression.startsWith("\"") && sepExpression.endsWith("\""))
         {
            if (sepExpression.length() == 1)
               sepExpression = "\"";
            else
               sepExpression = sepExpression.substring(1, sepExpression.length() - 1);
         }
         mySeparator = sepExpression;
      }
   }

   /**
    * Initialize the ConcatAggregator to an empty <code>StringBuilder</code>.
    */
   public void init()
   {
      myBuf = new StringBuilder();
      hasContent = false;
   }

   /**
    * Concatenate the value as a <code>String</code>, and possibly a separator.
    *
    * @param value The value to aggregate.
    */
   public void iterate(Object value)
   {
      if (value != null)
      {
         // Value.
         String property = getProperty();

         Object obj = getValueFromProperty(value, property);
         // Don't count nulls.
         if (obj != null)
         {
            // Separator.
            if (hasContent && mySeparator != null && mySeparator.length() > 0)
               myBuf.append(mySeparator);
            else
               hasContent = true;
            myBuf.append(obj.toString());
         }
      }
   }

   /**
    * Remove the <code>String</code> value, and possibly a separator.
    *
    * @param value The value to remove.
    * @since 0.9.0
    */
   public void delete(Object value)
   {
      if (value != null)
      {
         // Value.
         String property = getProperty();

         Object obj = getValueFromProperty(value, property);
         // Don't count nulls.
         if (obj != null)
         {
            String strObj = obj.toString();
            String ifAtBeginning = strObj + mySeparator;
            int startsWithIndex = myBuf.indexOf(ifAtBeginning);
            if (startsWithIndex == 0)
            {
               // value + sep + rest => rest
               myBuf.replace(0, ifAtBeginning.length(), "");
            }
            else
            {
               String ifInInterior = mySeparator + ifAtBeginning;
               int interiorIndex = myBuf.indexOf(ifInInterior);
               if (interiorIndex != -1)
               {
                  // before + sep + value + sep + after => before + sep + after
                  myBuf.replace(interiorIndex, interiorIndex + ifInInterior.length(), mySeparator);
               }
               else if (myBuf.toString().equals(strObj))
               {
                  // value => (empty)
                  myBuf.setLength(0);
               }
            }
         }

         hasContent = myBuf.length() > 0;
      }
   }

   /**
    * The concat function can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * The concat function doesn't supply its own window clause.
    * @return <code>null</code>
    * @since 0.9.0
    */
   public WindowClause getWindowClause()
   {
      return null;
   }

   /**
    * Merge the given <code>Aggregator</code> into this one.  Any separator
    * <code>String</code> will be maintained by this
    * <code>ConcatAggregator</code>.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof ConcatAggregator)
      {
         ConcatAggregator otherAgg = (ConcatAggregator) agg;
         // If there is something in the other ConcatAggregator...
         if (otherAgg.hasContent)
         {
            if (!hasContent)
               hasContent = true;
            else
               myBuf.append(mySeparator);
            myBuf.append(otherAgg.myBuf);
         }
      }
   }

   /**
    * Return the concatenation of all aggregated values.
    *
    * @return The concatenated <code>String</code>, or an empty
    *    <code>String</code> if no values were processed.
    */
   public String terminate()
   {
      return myBuf.toString();
   }

   /**
    * A <code>String</code> representation of this
    * <code>ConcatAggregator</code>, which takes into account the possible
    * existence of a separator <code>String</code> specified in the property.
    */
   @Override
   public String toString()
   {
      return getClass().getName() + "(" + getProperty() +
         ((mySeparator.length() > 0) ? (",\"" + mySeparator + "\"") : "" ) + ")";
   }
}
