package net.sf.jagg;

import net.sf.jagg.model.WindowClause;

/**
 * This class represents the "rank" analytic function over no values.
 * It returns the rank within the partition given the ordering.  Ties are
 * given the same rank number, and this can result in "gaps" in the rankings.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class RankAnalytic extends NoPropAnalytic
{
   private long myCount;
   private long myRun;
   private boolean doIStartRunOver;

   /**
    * Constructs a <code>RankAnalytic</code>, with no property.
    *
    * @param property Should be an empty string or <code>null</code>.
    */
   public RankAnalytic(String property)
   {
      setProperty(property);
   }

   /**
    * Constructs an <code>RankAnalytic</code>, with no property.
    */
   public RankAnalytic()
   {
      setProperty(null);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public RankAnalytic replicate()
   {
      return new RankAnalytic();
   }

   /**
    * Initialize the count to zero.
    */
   public void init()
   {
      myCount = 0;
      myRun = 0;
      doIStartRunOver = false;
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
         if (doIStartRunOver)
            myRun = 1;
         else
            myRun++;
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
    * The rank analytic doesn't take a window clause.
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
      return myCount + 1 - myRun;
   }
}