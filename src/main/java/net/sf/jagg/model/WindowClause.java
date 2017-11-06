package net.sf.jagg.model;

/**
 * A <code>WindowClause</code> represents the sliding "window" of data that is
 * considered for analytic function processing.  It controls the extent of data
 * considered for analytic calculations.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class WindowClause
{
   /**
    * The type of the windowing clause.
    */
   public enum Type
   {
      /**
       * The window is specified as a range of physical rows.
       */
      ROWS,
      /**
       * The window is specified as all rows within a range of values.
       */
      RANGE
   }

   /**
    * The default window clause if none is explicitly specified.  This is
    * <code>range(null, 0)</code>.
    */
   public static final WindowClause DEFAULT = new WindowClause(Type.RANGE, null, 0);

   private Type myWindowType;
   private Number myStartValue;
   private Number myEndValue;

   /**
    * Constructs a <code>WindowClause</code> with the given <code>WindowType</code>
    * and the given start and end values.
    * @param windowType A <code>WindowType</code> constant.
    * @param startValue If <code>windowType</code> is <code>ROWS</code>, then
    *    this is the number of rows backwards that are still in the window.  If
    *    <code>windowType</code> is <code>RANGE</code>, then this is the range
    *    behind the value in the current row, within which rows are still in
    *    the window.  If this is <code>null</code>, then it defaults to all
    *    previously encountered rows.
    * @param endValue If <code>windowType</code> is <code>ROWS</code>, then
    *    this is the number of rows forwards that are included in the window.
    *    If <code>windowType</code> is <code>RANGE</code>, then this is the
    *    range ahead of the value in the current row, within which rows are
    *    included in the window.  If this is <code>null</code>, then it
    *    defaults to the current row/value.
    */
   public WindowClause(Type windowType, Number startValue, Number endValue)
   {
      if (windowType == null)
         throw new IllegalArgumentException("windowType must not be null!");
      myWindowType = windowType;
      myStartValue = startValue;
      myEndValue = endValue;
   }

   /**
    * Returns the type of the window.
    * @return The type of the window, as a <code>WindowClause.Type</code>.
    */
   public Type getWindowType()
   {
      return myWindowType;
   }

   /**
    * Returns the start value.
    * @return The start value.
    */
   public Number getStartValue()
   {
      return myStartValue;
   }

   /**
    * Returns the end value.
    * @return The end value.
    */
   public Number getEndValue()
   {
      return myEndValue;
   }

   /**
    * Returns the string representation.
    * @return The string representation.
    */
   public String toString()
   {
      StringBuilder buf = new StringBuilder();
      buf.append(myWindowType.toString());
      buf.append("(");
      if (myStartValue != null)
         buf.append(myStartValue);
      buf.append(", ");
      if (myEndValue != null)
         buf.append(myEndValue);
      buf.append(")");

      return buf.toString();
   }
}
