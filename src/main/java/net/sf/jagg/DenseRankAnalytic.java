package net.sf.jagg;

import net.sf.jagg.model.WindowClause;

/**
 * This class represents the "dense rank" analytic function over no values.
 * It returns the rank within the partition given the ordering.  Ties are
 * given the same rank number, but this does not result in "gaps" in the
 * rankings.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class DenseRankAnalytic extends NoPropAnalytic
{
   private long myCount;
   private boolean doIStartRunOver;

   /**
    * Constructs a <code>RankAnalytic</code>, with no property.
    *
    * @param property Should be an empty string or <code>null</code>.
    */
   public DenseRankAnalytic(String property)
   {
      setProperty(property);
   }

   /**
    * Constructs an <code>RankAnalytic</code>, with no property.
    */
   public DenseRankAnalytic()
   {
      setProperty(null);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public DenseRankAnalytic replicate()
   {
      return new DenseRankAnalytic();
   }

   /**
    * Initialize the count to zero.
    */
   public void init()
   {
      myCount = 0;
      doIStartRunOver = true;
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
         if (doIStartRunOver)
            myCount++;
         doIStartRunOver = false;
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
         doIStartRunOver = true;
      }
   }

   /**
    * The dense rank analytic doesn't take a window clause.
    * @return <code>false</code>.
    */
   public boolean takesWindowClause()
   {
      return false;
   }

   /**
    * The row number analytic supplies its own window clause: <code>range(0, 0)</code>.
    * @return A <code>WindowClause</code>.
    */
   public WindowClause getWindowClause()
   {
      return new WindowClause(WindowClause.Type.RANGE, 0, 0);
   }

   /**
    * Merge the given <code>Aggregator</code> into this one by adding the
    * counts.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      // TODO: Decide how to implement this when adding parallel support for analytics.
      throw new UnsupportedOperationException("Merge not implemented!");
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