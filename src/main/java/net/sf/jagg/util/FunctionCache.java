package net.sf.jagg.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jagg.AggregateFunction;

/**
 * Created as a wrapper around a <code>Map</code> that maps specification
 * strings to <code>Lists</code> of an implementation of
 * <code>AggregateFunctions</code>.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public abstract class FunctionCache<F extends AggregateFunction>
{
   private final Map<String, List<F>> myAggregators;

   /**
    * Constructs a <code>FunctionCache</code>.
    */
   public FunctionCache()
   {
      myAggregators = new HashMap<String, List<F>>();
   }

   /**
    * Adds the given <code>AggregateFunction</code> to an internal cache.  If
    * it's not in use, then it marks it as "in use" and returns it.  Else, it
    * searches the cache for an <code>AggregateFunction</code> that matches the
    * given <code>AggregateFunction</code> and is not already in use.  If none
    * exist in the cache, then it replicates the given <code>AggregateFunction</code>,
    * adds it to the cache, and returns it.
    *
    * @param archetype The <code>AggregateFunction</code> whose properties (and
    *    type) need to be matched.
    * @return A matching <code>AggregateFunction</code> object.  It could be
    *    <code>archetype</code> itself if it's not already in use, or it could
    *    be <code>null</code> if <code>archetype</code> was null.
    */
   public F getFunction(F archetype)
   {
      if (archetype == null)
         return null;

      List<F> aggsList;
      F agg = null;

//      long time1 = System.nanoTime();

      // Synchronize access to the cache so multiple Threads don't "find"
      // the same AggregateFunction.
      synchronized (myAggregators)
      {
         // Use the given AggregateFunction if it was not already in use.
         // This must be within a synchronized block so that the same archetype
         // is not chosen by multiple threads.  It is in THIS synchronized
         // block to avoid having to get multiple locks.
         if (!archetype.isInUse())
         {
            archetype.setInUse(true);
            agg = archetype;
         }

         // If we can't use the archetype itself...
         if (agg == null)
         {
            aggsList = myAggregators.get(archetype.toString());
            // If we have a list of aggregators matching the name and property.
            if (aggsList != null)
            {
               // Look for one that's not in use.
               int size = aggsList.size();
               for (int a = 0; a < size; a++)
               {
                  F candidate = aggsList.get(a);
                  if (!candidate.isInUse())
                  {
                     agg = candidate;
                     // Set as "in use" before coming out of synchronization.
                     agg.setInUse(true);
                     break;
                  }
               }

            }
            if (aggsList == null)
            {
               aggsList = new ArrayList<F>();
               myAggregators.put(archetype.toString(), aggsList);
            }

            // We must create another AggregateFunction and add it to the cache.
            // Only replicated AggregateFunction are added to the cache; archetypes
            // are not added to the cache.
            if (agg == null)
            {
               //agg = archetype.replicate();
               agg = replicate(archetype);

               // Set as in use before adding to the cache, so another Thread
               // won't pick up this one.
               agg.setInUse(true);
               aggsList.add(agg);
            }
         }
      }
//      long time10 = System.nanoTime();
//      System.out.println("AC timings: All of gA: " + (time10 - time1));
      return agg;
   }

   /**
    * Replicates the given archetype, preserving the same type.
    * @param archetype An archetype to either use or copy.
    * @return The archetype itself, if it's not already in use, or a copy.
    */
   protected abstract F replicate(F archetype);
}