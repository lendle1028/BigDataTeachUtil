package net.sf.jagg.msd;

/**
 * A <code>ChainedExtractor</code> extracts a part of an element and returns it
 * as the label.  It relies on labels from a chained <code>Extractor</code> so
 * it can produce its own labels based on that chained <code>Extractor's</code>
 * labels.  At the end of the chain is an <code>Extractor</code> that returns
 * the element itself -- a <code>SelfExtractor</code>.
 *
 * @param <E> The element type, matching <code>Extractor</code>.
 * @param <L> The label type, matching <code>Extractor</code>.
 * @param <T> The label type from a <em>chained</em> <code>Extractor</code>,
 *    from which this can generate a new label of type <code>L</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 * @see SelfExtractor
 */
public abstract class ChainedExtractor<E, L, T> extends AbstractExtractor<E, T> implements Extractor<E, L>
{
   /**
    * Creates an <code>ChainedExtractor</code> that uses the given
    * <code>Extractor</code> in a chain for its labels.
    *
    * @param extractor The chained <code>Extractor</code>.
    */
   public ChainedExtractor(Extractor<E, T> extractor)
   {
      super(extractor);
   }

   /**
    * Returns the <em>label</em> for a given element.
    * @param element The element.
    * @return The label.
    */
   public abstract L getLabel(E element);
}