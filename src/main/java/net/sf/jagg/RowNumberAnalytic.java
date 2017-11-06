package net.sf.jagg;

import net.sf.jagg.model.WindowClause;

/**
 * This class represents the "row number" analytic function over no values.
 * It returns the row number within the partition given the ordering.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class RowNumberAnalytic extends NoPropAnalytic
{
   private long myCount;

   /**
    * Constructs a <code>RowNumberAnalytic</code>, with no property.
    *
    * @param property Should be an empty string or <code>null</code>.
    */
   public RowNumberAnalytic(String property)
   {
      setProperty(property);
   }

   /**
    * Constructs an <code>RowNumberAnalytic</code>, with no property.
    */
   public RowNumberAnalytic()
   {
      setProperty(null);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public RowNumberAnalytic replicate()
   {
      return new RowNumberAnalytic();
   }

   /**
    * Initialize the count to zero.
    */
   public void init()
   {
      myCount = 0;
   }

   /**
    * Count the value.
    *
    * @param value The value to aggregate.
    */
   public void iterate(Object value)
   {
      if (value != null)
      {
         myCount++;
      }
   }

   /**
    * Uncount the value.
    *
    * @param value The value to delete.
    */
   public void delete(Object value)
   {
      if (value != null)
      {
         myCount--;
      }
   }

   /**
    * The row number analytic doesn't take a window clause.
    * @return <code>false</code>.
    */
   public boolean takesWindowClause()
   {
      return false;
   }

   /**
    * The row number analytic supplies its own window clause: <code>rows(, 0)</code>.
    * @return A <code>WindowClause</code>.
    */
   public WindowClause getWindowClause()
   {
      return new WindowClause(WindowClause.Type.ROWS, null, 0);
   }

   /**
    * Merge the given <code>Aggregator</code> into this one by adding the
    * counts.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      // TODO: Is this appropriate for merging analytic functions?  This is how
      // an aggregate function would be merged.  Determine how best to merge
      // when implementing parallelism in analytics.
      if (agg != null && agg instanceof RowNumberAnalytic)
      {
         RowNumberAnalytic otherAna = (RowNumberAnalytic) agg;
         myCount += otherAna.myCount;
      }
   }

   /**
    * Return the count.
    *
    * @return The count as a <code>Long</code>.
    */
   public Long terminate()
   {
      return myCount;
   }
}
