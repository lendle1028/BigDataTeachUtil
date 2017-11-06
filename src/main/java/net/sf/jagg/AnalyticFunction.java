package net.sf.jagg;

import net.sf.jagg.model.WindowClause;

/**
 * This interface allows for the functionality necessary to implement
 * analytic functions, based on aggregate functions.  Subclasses define all of
 * the steps of the {@link AggregateFunction Aggregation algorithm}, plus these
 * methods:
 * <ol>
 * <li>Deletion, with the <code>delete</code> method.  This removes an element
 * from the state of the <code>AnalyticFunction</code>.  This is used when a
 * sliding window applies to an analytic function and an window "slides past"
 * an element, such that an element is no longer being considered in the
 * current value of the analytic function.</li>
 * <li>Whether it accepts the windowing clause, with the
 * <code>takesWindowClause</code> method.  Window clauses may not make sense
 * with certain analytic functions.  If a window clause is supplied to such an
 * analytic function, then an error will result.</li>
 * </ol>
 *
 * @see AggregateFunction
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public interface AnalyticFunction extends AggregateFunction
{
   /**
    * Removes the given value from the aggregation.  This assumes that the
    * value to be removed was already processed with the <code>iterate</code>
    * method.  E.g., a "sum" aggregation will subtract this object's property
    * value from a sum object.
    *
    * @param value The value to delete.
    */
   public abstract void delete(Object value);

   /**
    * Determines whether this <code>AnalyticFunction</code> can operate with a
    * window clause.  Most will return <code>true</code>, but some
    * analytic-only functions don't take a window clause.  For those that
    * return <code>false</code>, they must also return a non-<code>null</code>
    * <code>WindowClause</code>
    *
    * @return Whether this <code>AnalyticFunction</code> can operate with a
    *    window clause.
    * @see WindowClause
    */
   public abstract boolean takesWindowClause();

   /**
    * For <code>AnalyticFunctions</code> that don't take a window clause, this
    * method returns the implicit <code>WindowClause</code>.
    * 
    * @return A <code>WindowClause</code> if <code>takesWindowClause</code>
    *    returns <code>false</code>, else <code>null</code>.
    * @see #takesWindowClause
    * @see WindowClause
    */
   public abstract WindowClause getWindowClause();
}
