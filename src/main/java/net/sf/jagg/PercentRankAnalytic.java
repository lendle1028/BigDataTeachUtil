package net.sf.jagg;

import net.sf.jagg.math.DoubleDouble;
import net.sf.jagg.model.WindowClause;

/**
 * <p>A <code>PercentRankAnalytic</code> is an
 * <code>AbstractDependentAnalyticFunction</code> that performs this
 * calculation:</p>
 * <code>(rank() - 1) / (count(*) range() - 1)</code>
 * <p>I.e. The rank, minus 1, divided by the overall count
 * minus 1.  If the overall count is only 1, then the value is 0.</p>
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class PercentRankAnalytic extends AbstractDependentAnalyticFunction
{
   /**
    * Constructs a <code>PercentRankAnalytic</code>, with no property.
    *
    * @param property Should be an empty string or <code>null</code>.
    */
   public PercentRankAnalytic(String property)
   {
      setProperty(property);
   }

   /**
    * Constructs an <code>PercentRankAnalytic</code>, with no property.
    */
   public PercentRankAnalytic()
   {
      setProperty(null);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public PercentRankAnalytic replicate()
   {
      return new PercentRankAnalytic();
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
    * Depends on a <code>RankAnalytic</code> and a <code>CountAggregator</code>.
    * @param index A 0-based index, ranging from <code>0</code> through
    *    <code>getNumDependentFunctions() - 1</code>.
    * @return <ul>
    *    <li><code>0</code> => <code>RankAnalytic()</code></li>
    *    <li><code>1</code> => <code>CountAggregator("*")</code></li>
    * </ul>
    */
   public AnalyticFunction getAnalyticFunction(int index)
   {
      switch(index)
      {
         case 0:
            return new RankAnalytic();
         case 1:
            return new CountAggregator("*");
         default:
            throw new IndexOutOfBoundsException("Invalid index: " + index);
      }
   }

   /**
    * <code>WindowClause</code> 0 is <code>null</code> and
    * <code>WindowClause</code> 1 is <code>range()</code>.
    * @param index A 0-based index, ranging from <code>0</code> through
    *    <code>getNumDependentFunctions() - 1</code>.
    * @return <ul>
    *    <li><code>0</code> => <code>null</code></li>
    *    <li><code>1</code> => <code>range()</code></li>
    * </ul>
    */
   public WindowClause getWindowClause(int index)
   {
      switch(index)
      {
         case 0:
            // No window clause for RankAnalytic.
            return null;
         case 1:
            return new WindowClause(WindowClause.Type.RANGE, null, null);
         default:
            throw new IndexOutOfBoundsException("Invalid index: " + index);
      }
   }

   /**
    * Returns the percent rank as a <code>Double</code>.
    * @return The percent rank as a <code>Double</code>.
    */
   public Double terminate()
   {
      return terminateDoubleDouble().doubleValue();
   }

   /**
    * Returns the percent rank as a <code>DoubleDouble</code>.
    * @return the percent rank as a <code>DoubleDouble</code>.
    */
   public DoubleDouble terminateDoubleDouble()
   {
      Long value1 = (Long) getValue(1);
      if (value1 == 0)
      {
         return new DoubleDouble();
      }
      else
      {
         DoubleDouble result = new DoubleDouble((Long) getValue(0) - 1);
         result.divideSelfBy(value1 - 1);
         return result;
      }
   }
}