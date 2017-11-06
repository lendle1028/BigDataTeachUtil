package net.sf.jagg;

import java.util.HashMap;
import java.util.Map;

import net.sf.jagg.model.WindowClause;

/**
 * <p>An <code>AbstractDependentAnalyticFunction</code> is an <code>Aggregator</code>.
 * It implements <code>DependentAnalyticFunction</code> by keeping track of the
 * values from the <code>AnalyticFunctions</code> that get supplied by the
 * <code>setValue</code> method.</p>
 *
 * <p>A concrete subclass must implement the following methods to complete the
 * <code>DependentAnalyticFunction</code> interface:</p>
 * <ul>
 *    <li><code>getNumDependentFunctions()</code> - The number of
 * <code>AnalyticFunctions</code> on which this function depends.</li>
 *    <li><code>getAnalyticFunction(int index)</code> - Supply the functions
 * by index.</li>
 *    <li><code>getWindowClause(int index)</code> - Supply the corresponding
 * <code>WindowClauses</code> by index.</li>
 *    <li><code>terminate()</code> - Call <code>getValue</code> to get the
 * values from the functions on which this function depends, and calculate or
 * construct the result of this function.</li>
 *    <li><code>replicate()</code> Create an unitialized copy of this function.</li>
 * </ul>
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public abstract class AbstractDependentAnalyticFunction extends Aggregator implements DependentAnalyticFunction
{
   private Map<Integer, Object> myValues = new HashMap<Integer, Object>();

   /**
    * Performs no initialization.  Initialization will however occur in any
    * <code>AnalyticFunctions</code> on which this
    * <code>AbstractDependentAnalyticFunction</code> depends.
    */
   public final void init() { }

   /**
    * Performs no iteration.  Iteration will however occur in any
    * <code>AnalyticFunctions</code> on which this
    * <code>AbstractDependentAnalyticFunction</code> depends.
    * @param value Ignored.
    */
   public final void iterate(Object value) { }

   /**
    * Performs no merging.  Merging will however occur in any
    * <code>AnalyticFunctions</code> on which this
    * <code>AbstractDependentAnalyticFunction</code> depends.
    * @param agg Ignored.
    */
   public final void merge(AggregateFunction agg) { }

   /**
    * Performs no deleting.  Deleting will however occur in any
    * <code>AnalyticFunctions</code> on which this
    * <code>AbstractDependentAnalyticFunction</code> depends.
    * @param value Ignored.
    */
   public final void delete(Object value) { }

   /**
    * No <code>AbstractDependentAnalyticFunctions</code> can take a
    * user-supplied <code>WindowClause</code>.
    * @return <code>false</code>.
    */
   public final boolean takesWindowClause()
   {
      return false;
   }

   /**
    * The default <code>WindowClause</code> for an
    * <code>AbstractDependentAnalyticFunction</code> is <code>range()</code>.
    * This ensures that nothing is terminated until the entire partition is
    * processed.  This guarantees that all values are available for calculation.
    * @return <code>range()</code>.
    */
   public final WindowClause getWindowClause()
   {
      return new WindowClause(WindowClause.Type.RANGE, null, null);
   }

   /**
    * Stores the current value of the <code>AnalyticFunction</code> at the given
    * index.  <code>Analytic</code> calls this <code>getNumDependentFunctions()</code>
    * times prior to calling <code>terminate()</code>, so that <code>terminate</code>
    * can make a calculation based on these values.
    * @param index A 0-based index, ranging from <code>0</code> through
    *    <code>getNumDependentFunctions() - 1</code>.
    * @param value The current value of the <code>AnalyticFunction</code>.
    */
   public final void setValue(int index, Object value)
   {
      myValues.put(index, value);
   }

   /**
    * Returns the current value of the <code>AnalyticFunction</code> at the
    * given index.  In <code>terminate</code>, call this method to retrieve the
    * values necessary for the calculation.
    * @param index A 0-based index, ranging from <code>0</code> through
    *    <code>getNumDependentFunctions() - 1</code>.
    * @return The current value of the <code>AnalyticFunction</code>.
    */
   protected final Object getValue(int index)
   {
      return myValues.get(index);
   }
}
