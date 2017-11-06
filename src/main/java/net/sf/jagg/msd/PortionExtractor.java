package net.sf.jagg.msd;

/**
 * A <code>PortionExtractor</code> extracts a <em>portion</em> of an
 * element and returns it as the label.  It relies on labels from a chained
 * <code>Extractor</code> so it can produce its own labels based on the
 * <code>Extractor's</code> labels.  It does NOT implement the
 * <code>Extractor</code> interface, so that it can produce <code>int</code>
 * labels (The primitive type <code>int</code> cannot be a type parameter.)
 * Many subclasses of <code>PortionExtractor</code> are anonymous subclasses
 * defined within Discriminators.
 *
 * @param <E> The element type, matching <code>Extractor</code>.
 * @param <T> The label type from a <em>chained</em> <code>Extractor</code>,
 *    from which this can generate a new label of type <code>int</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public abstract class PortionExtractor<E, T> extends AbstractExtractor<E, T>
{
   /**
    * Creates an <code>PortionExtractor</code> that uses the given
    * <code>Extractor</code> in a chain for its labels.
    *
    * @param extractor The chained <code>Extractor</code>.
    */
   public PortionExtractor(Extractor<E, T> extractor)
   {
      super(extractor);
   }

   /**
    * Returns the <em>label</em> for a given element.
    * @param element The element.
    * @return The label, as an <code>int</code>.
    */
   public abstract int getLabel(E element);
}