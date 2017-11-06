package net.sf.jagg;

import net.sf.jagg.math.DoubleDouble;
import net.sf.jagg.model.WindowClause;

/**
 * <p>A <code>RatioToReportAnalytic</code> is an
 * <code>AbstractDependentAnalyticFunction</code> that performs this
 * calculation:</p>
 * <code>sum(property) rows(0, 0) / sum(property) range()</code>
 * <p>I.e. The sum of values so far divided by the overall sum.</p>
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class RatioToReportAnalytic extends AbstractDependentAnalyticFunction
{
   /**
    * Constructs a <code>RatioToReportAnalytic</code>, with no property.
    *
    * @param property Should be an empty string or <code>null</code>.
    */
   public RatioToReportAnalytic(String property)
   {
      setProperty(property);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public RatioToReportAnalytic replicate()
   {
      return new RatioToReportAnalytic(getProperty());
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
    * Depends on two <code>SumAggregators</code>.
    * @param index A 0-based index, ranging from <code>0</code> through
    *    <code>getNumDependentFunctions() - 1</code>.
    * @return <ul>
    *    <li><code>0</code> => <code>SumAggregator(getProperty())</code></li>
    *    <li><code>1</code> => <code>SumAggregator(getProperty())</code></li>
    * </ul>
    */
   public AnalyticFunction getAnalyticFunction(int index)
   {
      String property = getProperty();
      switch(index)
      {
         case 0:
            return new SumAggregator(property);
         case 1:
            return new SumAggregator(property);
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
    *    <li><code>0</code> => <code>rows(0, 0)</code></li>
    *    <li><code>1</code> => <code>range()</code></li>
    * </ul>
    */
   public WindowClause getWindowClause(int index)
   {
      switch(index)
      {
         case 0:
            return new WindowClause(WindowClause.Type.ROWS, 0, 0);
         case 1:
            return new WindowClause(WindowClause.Type.RANGE, null, null);
         default:
            throw new IndexOutOfBoundsException("Invalid index: " + index);
      }
   }

   /**
    * Returns the ratio to report as a <code>Double</code>.
    * @return The ratio to report as a <code>Double</code>.
    */
   public Double terminate()
   {
      return terminateDoubleDouble().doubleValue();
   }

   /**
    * Returns the ratio to report as a <code>DoubleDouble</code>.
    * @return the ratio to report as a <code>DoubleDouble</code>.
    */
   public DoubleDouble terminateDoubleDouble()
   {
      DoubleDouble result = new DoubleDouble((Double) getValue(0));
      result.divideSelfBy((Double) getValue(1));
      return result;
   }
}