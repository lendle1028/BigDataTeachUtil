package net.sf.jagg;

import net.sf.jagg.math.DoubleDouble;
import net.sf.jagg.model.WindowClause;

/**
 * <p>A <code>CumeDistAnalytic</code> is an
 * <code>AbstractDependentAnalyticFunction</code> that performs this
 * calculation:</p>
 * <code>count(*) range(, 0) / count(*) range()</code>
 * <p>I.e. The count of values so far divided by the overall count.</p>
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class CumeDistAnalytic extends AbstractDependentAnalyticFunction
{
   /**
    * Constructs a <code>CumeDistAnalytic</code>, with no property.
    *
    * @param property Should be an empty string or <code>null</code>.
    */
   public CumeDistAnalytic(String property)
   {
      setProperty(property);
   }

   /**
    * Constructs an <code>CumeDistAnalytic</code>, with no property.
    */
   public CumeDistAnalytic()
   {
      setProperty(null);
   }
   
   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public CumeDistAnalytic replicate()
   {
      return new CumeDistAnalytic();
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
    * Depends on two <code>CountAggregators</code>.
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
    * <code>WindowClause</code> 0 is <code>range(, 0)</code> and
    * <code>WindowClause</code> 1 is <code>range()</code>.
    * @param index A 0-based index, ranging from <code>0</code> through
    *    <code>getNumDependentFunctions() - 1</code>.
    * @return <ul>
    *    <li><code>0</code> => <code>range(, 0)</code></li>
    *    <li><code>1</code> => <code>range()</code></li>
    * </ul>
    */
   public WindowClause getWindowClause(int index)
   {
      switch(index)
      {
         case 0:
            return new WindowClause(WindowClause.Type.RANGE, null, 0);
         case 1:
            return new WindowClause(WindowClause.Type.RANGE, null, null);
         default:
            throw new IndexOutOfBoundsException("Invalid index: " + index);
      }
   }

   /**
    * Returns the cumulative distribution as a <code>Double</code>.
    * @return The cumulative distribution as a <code>Double</code>.
    */
   public Double terminate()
   {
      return terminateDoubleDouble().doubleValue();
   }

   /**
    * Returns the cumulative distribution as a <code>DoubleDouble</code>.
    * @return the cumulative distribution as a <code>DoubleDouble</code>.
    */
   public DoubleDouble terminateDoubleDouble()
   {
      DoubleDouble result = new DoubleDouble((Long) getValue(0));
      result.divideSelfBy((Long) getValue(1));
      return result;
   }
}
