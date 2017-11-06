package net.sf.jagg.util;

import net.sf.jagg.AggregateFunction;

/**
 * This now subclasses <code>FunctionCache</code> with a type argument of
 * <code>AggregateFunction</code>.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class AggregatorCache extends FunctionCache<AggregateFunction>
{
   /**
    * Returns the archetype itself, if it's not already in use, or a copy.
    * @param archetype An archetype to either use or copy.
    * @return The archetype itself, if it's not already in use, or a copy.
    */
   protected AggregateFunction replicate(AggregateFunction archetype)
   {
      return archetype.replicate();
   }
}
