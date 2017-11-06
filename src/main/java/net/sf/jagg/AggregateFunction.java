package net.sf.jagg;

/**
 * An <code>AggregateFunction</code> is an object that can compute aggregate
 * statistics on a list of values.  Implementations define the following steps
 * of the Aggregation algorithm:
 * <ol>
 * <li>Initialization, with the <code>init</code> method.  This initializes
 *    the state of the Aggregator.  <code>Aggregators</code> may be reused, so
 *    this method must be prepared to instantiate or reset any state objects it
 *    maintains.</li>
 * <li>Iteration, with the <code>iterate</code> method.  This adds a value to
 *    the aggregation.  This will be called exactly once per object to
 *    aggregate.</li>
 * <li>Merging, with the <code>merge</code> method.  In parallel execution,
 *    this merges results from two <code>Aggregator</code> objects resulting
 *    from parallel execution.  After the <code>merge</code> method completes,
 *    then this <code>Aggregator</code> reflects the combined state of both
 *    this <code>Aggregator</code> and another <code>Aggregator</code>. Merging
 *    takes place during parallel execution and during super aggregation
 *    (rollups, cubes, and grouping sets).</li>
 * <li>Termination, with the <code>terminate</code> method.  At this point, all
 *    aggregation is complete, and only a final result needs to be constructed.</li>
 * </ol>
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public interface AggregateFunction
{
   /**
    * Initializes the <code>Aggregator</code>.  Subclasses should override this
    * method to instantiate state objects that will hold the state of the
    * aggregation.  E.g., a "sum" aggregation will initialize a sum object to
    * zero.  This <code>Aggregator</code> may be reused, so any objects may
    * already be instantiated, but their state must be reset.
    */
   public abstract void init();

   /**
    * Processes the given value into the aggregation.  E.g., a "sum"
    * aggregation will add this object's property value to a sum object.
    *
    * @param value The value to aggregate.
    */
   public abstract void iterate(Object value);

   /**
    * Merges the state of the given <code>Aggregator</code> into this own
    * <code>Aggregator</code>'s state.  Called when parallel execution
    * yields more than one <code>Aggregator</code> to combine into one.
    *
    * @param agg The <code>Aggregator</code> whose state needs to be merged
    *    into this one.
    */
   public abstract void merge(AggregateFunction agg);

   /**
    * At this point the aggregation of values is complete, and a final result
    * needs to be constructed.  This method constructs that final result.
    *
    * @return A value representing the result of the aggregation.
    */
   public abstract Object terminate();

   /**
    * Retrieves the property that this <code>Aggregator</code> aggregates.
    *
    * @return A property name.
    */
   public abstract String getProperty();

   /**
    * Determines whether this <code>AggregateFunction</code> is in use.
    * @return A boolean indicating whether it's in use.
    */
   public boolean isInUse();

   /**
    * Sets whether this <code>AggregateFunction</code> is in use.
    * @param inUse A boolean indicating whether it's in use.
    */
   public void setInUse(boolean inUse);

   /**
    * Returns an uninitialized copy of this <code>AggregateFunction</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>AggregateFunction</code> object.
    */
   public AggregateFunction replicate();
}
