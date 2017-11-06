package net.sf.jagg;

import net.sf.jagg.model.WindowClause;

/**
 * A <code>DependentAnalyticFunction</code> is an <code>AnalyticFunction</code>
 * that depends on the results of multiple other <code>AnalyticFunctions</code>
 * that have different <code>WindowClauses</code>.  The <code>Analytic</code>
 * class detects <code>DependentAnalyticFunctions</code>, loads the functions
 * they're dependent on, processes them normally, and feeds the values back to
 * this function to calculate the value depending on the these values.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public interface DependentAnalyticFunction extends AnalyticFunction
{
   /**
    * Returns the number of <code>AnalyticFunctions</code> on which this
    * <code>DependentAnalyticFunction</code> depends.  This controls the domain
    * of index values that <code>getAnalyticFunction</code> and
    * <code>getWindowClause</code> can take.
    *
    * @return The number of <code>AnalyticFunctions</code> on which this
    * <code>DependentAnalyticFunction</code> depends.
    */
   public int getNumDependentFunctions();

   /**
    * Returns an <code>AnalyticFunction</code> on which this
    * <code>DependentAnalyticFunction</code> depends.
    * @param index A 0-based index, ranging from <code>0</code> through
    *    <code>getNumDependentFunctions() - 1</code>.
    * @return An <code>AnalyticFunction</code>.
    * @see #getNumDependentFunctions
    */
   public AnalyticFunction getAnalyticFunction(int index);

   /**
    * Returns a <code>WindowClause</code> that is associated with the
    * <code>AnalyticFunction</code> at the same index.
    * @param index A 0-based index, ranging from <code>0</code> through
    *    <code>getNumDependentFunctions() - 1</code>.
    * @return A <code>WindowClause</code> that is associated with the
    * <code>AnalyticFunction</code> at the same index.
    * @see #getAnalyticFunction
    * @see #getNumDependentFunctions
    */
   public WindowClause getWindowClause(int index);

   /**
    * Sets the current value of the <code>AnalyticFunction</code> at the given
    * index.  <code>Analytic</code> calls this <code>getNumDependentFunctions()</code>
    * times prior to calling <code>terminate()</code>, so that <code>terminate</code>
    * can make a calculation based on these values.
    * @param index A 0-based index, ranging from <code>0</code> through
    *    <code>getNumDependentFunctions() - 1</code>.
    * @param value The current value of the <code>AnalyticFunction</code>.
    * @see #terminate
    */
   public void setValue(int index, Object value);
}
