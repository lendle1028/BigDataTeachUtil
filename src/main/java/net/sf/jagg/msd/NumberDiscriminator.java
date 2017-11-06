package net.sf.jagg.msd;

/**
 * A <code>NumberDiscriminator</code> discriminates <code>Lists</code> of
 * <code>Numbers</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public abstract class NumberDiscriminator<T extends Number> extends PortionDiscriminator<T>
{
   /**
    * Numbers will be discriminated this number of bits at a time.
    */
   public static final int PORTION_BITS = 16;
   /**
    * This is the mask used to extract a portion of a number.
    */
   public static final int PORTION_MASK = (1 << PORTION_BITS) - 1;

   /**
    * Returns an appropriate <code>Extractor</code>.
    * @param extractor An <code>Extractor</code> that returns appropriate
    *    labels.
    * @return An appropriate <code>Extractor</code>.
    */
   protected abstract <E> PortionExtractor<E, T> getPortionExtractor(Extractor<E, T> extractor);
}
