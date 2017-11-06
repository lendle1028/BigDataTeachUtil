package net.sf.jagg.util;

import net.sf.jagg.AnalyticAggregator;

/**
 * This subclasses <code>FunctionCache</code> with a type argument of
 * <code>AnalyticAggregator</code>.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class AnalyticCache extends FunctionCache<AnalyticAggregator>
{
   /**
    * Returns the archetype itself, if it's not already in use, or a copy.
    * @param archetype An archetype to either use or copy.
    * @return The archetype itself, if it's not already in use, or a copy.
    */
   protected AnalyticAggregator replicate(AnalyticAggregator archetype)
   {
      return archetype.replicate();
   }
}
