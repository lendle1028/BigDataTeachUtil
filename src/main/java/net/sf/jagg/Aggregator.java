package net.sf.jagg;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.sf.jagg.exception.AggregatorCreationException;
import net.sf.jagg.exception.ParseException;
import net.sf.jagg.exception.PropertyAccessException;
import net.sf.jagg.util.AggregatorCache;
import net.sf.jagg.util.MethodCache;
import net.sf.jagg.math.DoubleDouble;

/**
 * This abstract class allows for the state necessary to implement aggregate
 * functions.  Subclasses define the following steps of the Aggregation
 * algorithm:
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
 * <p>The factory method <code>getAggregator</code> creates
 * <code>AggregateFunctions</code> and marks them as in use.  They are stored
 * in a cache (a <code>HashMap</code>) so they may be reused.  After an
 * <code>AggregateFunction</code> is used, it will be marked as not in use; it
 * remains in the cache and it may be reused.</p>
 *
 * <p>The abstract method <code>replicate</code> must be defined for every
 * <code>Aggregator</code>.  This method returns an uninitialized copy of the
 * <code>Aggregator</code>, with the same type and the same properties to
 * analyze as the original <code>Aggregator</code>.</p>
 *
 * <p>The concrete method <code>terminateDoubleDouble</code> may be overridden
 * by <code>Aggregators</code> that operate on floating-point numbers.  This
 * allows other <code>Aggregators</code> to use the high-precision result, a
 * <code>DoubleDouble</code>, internally in their calculations.  The default
 * implementation simply returns <code>DoubleDouble.NaN</code>.</p>
 *
 * <p>Currently, <code>AggregateFunctions</code> do not need to be thread-safe.
 * The <code>Aggregation</code> class is the only class that uses
 * <code>AggregateFunctions</code>, and only one <code>Thread</code> at a time
 * uses any <code>AggregateFunction</code>.</p>
 *
 * <p>However, internally, the <code>Aggregator</code> class uses synchronized
 * access to internal <code>HashMaps</code> to cache <code>AggregateFunction</code>
 * (and <code>Methods</code>).</p>
 *
 * <p>The {@link #getValueFromProperty(Object, String) getValueProperty} method
 * has been made <code>public</code> as of version 0.7.2.</p>
 *
 * @see net.sf.jagg.math.DoubleDouble
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public abstract class Aggregator implements AggregateFunction
{
   /**
    * Special pseudo-property indicating that the object itself is to be
    * aggregated, instead of a property of the object.
    */
   public static final String PROP_SELF = ".";

   // Cache Method objects to save on instantiation/garbage collection costs.
   private static final MethodCache myMethodCache = MethodCache.getMethodCache();
   // Cache Aggregator objects to save on instantiation/garbage collection
   // costs.  The key is the aggregator specification string.
   private static final AggregatorCache myAggregatorCache = new AggregatorCache();

   private String myProperty;
   private boolean amIInUse = false;

   /**
    * Default constructor is protected so that only subclasses of
    * <code>Aggregator</code> can be instantiated.
    */
   protected Aggregator() {}

   /**
    * Adds the given <code>AggregateFunction</code> to an internal cache.  If
    * it's not in use, then it marks it as "in use" and returns it.  Else, it
    * searches the cache for an <code>AggregateFunction</code> that matches the
    * given <code>AggregateFunction</code> and is not already in use.  If none
    * exist in the cache, then it replicates the given
    * <code>AggregateFunction</code>, adds it to the cache, and returns it.
    *
    * @param archetype The <code>AggregateFunction</code> whose properties (and
    *    type) need to be matched.
    * @return A matching <code>AggregateFunction</code> object.  It could be
    *    <code>archetype</code> itself if it's not already in use, or it could
    *    be <code>null</code> if <code>archetype</code> was null.
    */
   public static AggregateFunction getAggregator(AggregateFunction archetype)
   {
      return myAggregatorCache.getFunction(archetype);
   }

   /**
    * Creates an <code>AggregateFunction</code> based on an <em>aggregator
    * specification string</em>.  Does not mark it as in use.  Does not add it
    * to the internal cache.  This is meant to aid the caller in creating an
    * <code>AggregateFunction</code> based on the following specification
    * string format: <code>aggName(property/-ies)</code>.
    * This assumes that the desired <code>AggregateFunction</code> has a
    * one-argument constructor with a <code>String</code> argument for its
    * property or properties.
    *
    * @param aggSpec The String specification of an <code>AggregateFunction</code>.
    * @return An <code>AggregateFunction</code> object.
    * @throws ParseException If the aggregator specification was mal-
    *    formed.
    * @throws AggregatorCreationException If there was a problem instantiating
    *    the <code>AggregateFunction</code>.
    */
   public static AggregateFunction getAggregator(String aggSpec)
   {
      int leftParenIdx = aggSpec.indexOf("(");
      int rightParenIdx = aggSpec.lastIndexOf(")");
      if (leftParenIdx == -1 || rightParenIdx == -1 || leftParenIdx > rightParenIdx)
         throw new ParseException("Malformed Aggregator specification: " + aggSpec);

      String aggName = aggSpec.substring(0, leftParenIdx);
//      int dotIndex = aggSpec.indexOf(".");
//      // Any dot past a "(" isn't a package specifier; it could be part of an argument.
//      if (dotIndex == -1 || dotIndex > leftParenIdx)
//      {
//         aggName = Aggregator.class.getPackage().getName() + "." + aggName;
//      }
      if (aggName.indexOf(".") == -1)
         aggName = Aggregator.class.getPackage().getName() + "." + aggName;
      if (!aggName.endsWith("Aggregator"))
         aggName = aggName + "Aggregator";
      String property = aggSpec.substring(leftParenIdx + 1, rightParenIdx);

      try
      {
         Class aggClass = Class.forName(aggName);
         Constructor ctor = aggClass.getConstructor(String.class);
         return (AggregateFunction) ctor.newInstance(property);
      }
      catch (ClassNotFoundException e)
      {
         throw new AggregatorCreationException("Unknown AggregateFunction class \"" + aggName + "\".", e);
      }
      catch (NoSuchMethodException e)
      {
         throw new AggregatorCreationException("Can't find constructor for AggregateFunction class \"" +
            aggName + "\" that contains exactly one String parameter.", e);
      }
      catch (InstantiationException e)
      {
         throw new AggregatorCreationException("AggregateFunction specified is not a concrete class: \"" +
            aggName + "\".", e);
      }
      catch (IllegalAccessException e)
      {
         throw new AggregatorCreationException("Unable to construct AggregateFunction \"" +
            aggName + "\".", e);
      }
      catch (InvocationTargetException e)
      {
         throw new AggregatorCreationException("Exception caught instantiating AggregateFunction \"" +
            aggName + "\": " + e.getCause().getClass().getName(), e);
      }
      catch (ClassCastException e)
      {
         throw new AggregatorCreationException("Class found is not an AggregateFunction: \"" +
            aggName + "\".", e);
      }
   }
   
   /**
    * Gets a specific <code>Method</code> from an internal cache, or creates it
    * using reflection if it does not exist.  The method is looked up via the
    * name "get&lt;Property&gt;", with the given property name, on the given
    * value object.  Invokes the <code>Method</code> and returns the value.
    * This is expected to be called in the <code>iterate</code> method, so that
    * it can access the object's property, although it can be called from any
    * <code>AggregateFunction</code> method.  This method is here to standardize
    * the "bean" method naming convention specified by <code>AggregateFunction</code>
    * and to cache <code>Method</code> objects for internal use.
    *
    * @param value The object on which to lookup a property value.
    * @param property The property to lookup.
    * @return The object's property value.
    * @throws PropertyAccessException If the desired <code>Method</code>
    *    does not exist, if the <code>Method</code> cannot be invoked because
    *    of Java language access control (e.g. private, etc.), or if  the
    *    invoked <code>Method</code> throws an <code>Exception</code>.
    * @see #iterate
    */
   public static Object getValueFromProperty(Object value, String property)
   {
      return myMethodCache.getValueFromProperty(value, property);
   }

   /**
    * Sets the property name.  Subclasses may override this method if they
    * want to extract more information from the property string, e.g.
    * "Name(property, addlInfo)".  The default implementation simply stores the
    * entire string to be made available via "getProperty".
    * 
    * @param property The property name.
    * @see #getProperty()
    */
   protected void setProperty(String property)
   {
      myProperty = property;
   }

   /**
    * Determines whether this <code>Aggregator</code> is in use.
    * @return A boolean indicating whether it's in use.
    */
   public final boolean isInUse()
   {
      return amIInUse;
   }

   /**
    * Sets whether this <code>Aggregator</code> is in use.
    * @param inUse A boolean indicating whether it's in use.
    */
   public final void setInUse(boolean inUse)
   {
      amIInUse = inUse;
   }

   /**
    * Returns an uninitialized copy of this <code>AggregateFunction</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>AggregateFunction</code> object.
    */
   public abstract AggregateFunction replicate();

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
    * aggregation will add this object's property value to a sum object.  An
    * implementation will likely want to call <code>getValueFromProperty</code>,
    * which accesses a cache of <code>Methods</code> to find the property's
    * value in the given object.
    *
    * @param value The value to aggregate.
    * @see #getValueFromProperty
    */
   public abstract void iterate(Object value);

   /**
    * Merges the state of the given <code>Aggregator</code> into this own
    * <code>Aggregator</code>'s state.  Called when parallel execution
    * yields more than one <code>Aggregator</code> to combine into one.
    *
    * @param agg The <code>AggregateFunction</code> whose state needs to be
    *    merged into this one.
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
    * Return the result as a <code>DoubleDouble</code>.  This is used mainly
    * when other <code>Aggregators</code> that use this result must maintain a
    * high precision.
    * @return A <code>DoubleDouble</code> representing the result of the
    *    aggregation.  The default implementation returns
    * <code>DoubleDouble.NaN</code>.
    * @see DoubleDouble
    * @since 0.4.0
    */
   public DoubleDouble terminateDoubleDouble()
   {
      return DoubleDouble.NaN;
   }

   /**
    * Determines whether the given <code>Aggregator</code> is equivalent to
    * this <code>Aggregator</code>.  This is necessary because
    * <code>Aggregator</code> objects will be stored in a <code>HashMap</code>.
    *
    * @param o Another <code>Aggregator</code>.
    * @return <code>true</code> if equivalent, <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      return (getClass().equals(o.getClass()) && toString().equals(o.toString()));
   }

   /**
    * Calculates a hash code for this <code>Aggregator</code>.  This is
    * necessary because <code>Aggregator</code> objects will be stored in a
    * <code>HashMap</code>.
    *
    * @return The hash code of this <code>Aggregator</code>.  It is computed by
    *    taking the hash of the result of the <code>toString</code> method.
    * @see #toString
    */
   public int hashCode()
   {
      return toString().hashCode();
   }

   /**
    * Retrieves the property that this <code>Aggregator</code> aggregates.
    *
    * @return A property name.
    */
   public String getProperty()
   {
      return myProperty;
   }

   /**
    * A String representation of this <code>Aggregator</code>, in the form
    * "className(property)".
    *
    * @return The String representation.
    */
   public String toString()
   {
      return getClass().getName() + "(" + getProperty() + ")";
   }
}
