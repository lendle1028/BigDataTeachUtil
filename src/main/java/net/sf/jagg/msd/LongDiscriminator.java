package net.sf.jagg.msd;

/**
 * A <code>LongDiscriminator</code> discriminates <code>Lists</code> of
 * <code>Longs</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class LongDiscriminator extends NumberDiscriminator<Long>
{
   /**
    * Returns a portion extractor appropriate for <code>longs</code>.
    * @param e An <code>Extractor</code> that returns appropriate labels.
    * @return An appropriate <code>PortionExtractor</code>.
    */
   protected <E> PortionExtractor<E, Long> getPortionExtractor(Extractor<E, Long> e)
   {
      return new PortionExtractor<E, Long>(e) {
         /**
          * The label is the specific portion of the element, cast as an
          * integer.
          * @param element The element.
          * @return The specific portion of the element, cast as an integer.
          */
         public int getLabel(E element)
         {
            return (int) ((myExtractor.getLabel(element) >> (myIndex * PORTION_BITS)) & PORTION_MASK);
         }

         /**
          * Complete after 4 portions.
          * @param element The element.
          * @return <code>true</code> if the index is at least 4,
          *    <code>false</code> otherwise.
          */
         public boolean isComplete(E element)
         {
            return myExtractor.isComplete(element) || myIndex >= 4;
         }
      };
   }
}