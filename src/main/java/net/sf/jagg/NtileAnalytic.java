package net.sf.jagg;

import net.sf.jagg.exception.ExpectedNumberException;
import net.sf.jagg.model.WindowClause;

/**
 * <p>A <code>NtileAnalytic</code> is an
 * <code>AbstractDependentAnalyticFunction</code> that assigns a bucket number
 * to each row, from <code>1</code> through <code>n</code>.  If the number of
 * rows is not a multiple of <code>n</code>, then the remainder of rows get
 * assigned, one each, to buckets <code>1</code> through the remainder.</p>
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class NtileAnalytic extends AbstractDependentAnalyticFunction
{
   private int myN;

   /**
    * Constructs a <code>NtileAnalytic</code>, with no property.
    *
    * @param property Should be convertible to a positive integer.
    */
   public NtileAnalytic(String property)
   {
      setProperty(property);
   }

   /**
    * Expects that the only "property" given is the actual desired number of,
    * buckets, convertible to a positive integer.
    *
    * @param property The property string.
    * @throws IllegalArgumentException If the the number of buckets is not a
    *    positive integer.
    * @see Aggregator#getProperty()
    */
   @Override
   protected void setProperty(String property)
   {
      super.setProperty(property);
      try
      {
         myN = Integer.parseInt(getProperty());
         if (myN <= 0)
         {
            throw new ExpectedNumberException("The number of buckets must be a positive integer, but got \"" +
                 getProperty() + "\".");
         }
      }
      catch (NumberFormatException e)
      {
         throw new ExpectedNumberException("The number of buckets must be a positive integer, but got \"" +
                 getProperty() + "\".");
      }
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public NtileAnalytic replicate()
   {
      return new NtileAnalytic(String.valueOf(myN));
   }

   /**
    * This depends on 2 functions.
    * @return <code>2</code>.
    */
   public int getNumDependentFunctions()
   {
      return 2;
   }

   /**
    * Depends on 2 <code>CountAggregators</code>.
    * @param index A 0-based index, ranging from <code>0</code> through
    *    <code>getNumDependentFunctions() - 1</code>.
    * @return <ul>
    *    <li><code>0</code> => <code>CountAggregator("*")</code></li>
    *    <li><code>1</code> => <code>CountAggregator("*")</code></li>
    * </ul>
    */
   public AnalyticFunction getAnalyticFunction(int index)
   {
      switch(index)
      {
         case 0:
            return new CountAggregator("*");
         case 1:
            return new CountAggregator("*");
         default:
            throw new IndexOutOfBoundsException("Invalid index: " + index);
      }
   }

   /**
    * <code>WindowClause</code> 0 is <code>rows(, 0)</code> and
    * <code>WindowClause</code> 1 is <code>range()</code>.
    * @param index A 0-based index, ranging from <code>0</code> through
    *    <code>getNumDependentFunctions() - 1</code>.
    * @return <ul>
    *    <li><code>0</code> => <code>rows(, 0)</code></li>
    *    <li><code>1</code> => <code>range()</code></li>
    * </ul>
    */
   public WindowClause getWindowClause(int index)
   {
      switch(index)
      {
         case 0:
            return new WindowClause(WindowClause.Type.ROWS, null, 0);
         case 1:
            return new WindowClause(WindowClause.Type.RANGE, null, null);
         default:
            throw new IndexOutOfBoundsException("Invalid index: " + index);
      }
   }

   /**
    * Returns the bucket number as a <code>Long</code>.
    * @return The bucket number as a <code>Long</code>.
    */
   public Long terminate()
   {
      long rownum = (Long) getValue(0);
      long count = (Long) getValue(1);
      // remainder of items; also number of buckets with one extra item
      long y = count % myN;
      // items per bucket without remainder
      long z = count / myN;
      // number of items in buckets with extra items
      long x = y * (z + 1);

      if (rownum <= x)
      {
         // z+1 items in the first y buckets.
         return (rownum - 1) / (z + 1) + 1;
      }
      else
      {
         // z items past the first y buckets.
         return y + (rownum - 1 - x) / z + 1;
      }
   }
}
