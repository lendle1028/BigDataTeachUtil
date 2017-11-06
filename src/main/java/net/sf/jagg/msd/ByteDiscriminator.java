package net.sf.jagg.msd;

/**
 * A <code>ByteDiscriminator</code> discriminates <code>Lists</code> of
 * <code>Bytes</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class ByteDiscriminator extends NumberDiscriminator<Byte>
{
   /**
    * Returns a portion extractor appropriate for <code>bytes</code>.
    * @param e An <code>Extractor</code> that returns appropriate labels.
    * @return An appropriate <code>PortionExtractor</code>.
    */
   protected <E> PortionExtractor<E, Byte> getPortionExtractor(Extractor<E, Byte> e)
   {
      return new PortionExtractor<E, Byte>(e) {
         /**
          * The label is the element itself, cast as an integer.
          * @param element The element.
          * @return The element itself, cast as an integer.
          */
         public int getLabel(E element)
         {
            return myExtractor.getLabel(element) & PORTION_MASK;
         }

         /**
          * Complete after 1 portion.
          * @param element The element.
          * @return <code>true</code> if the index is at least 1,
          *    <code>false</code> otherwise.
          */
         public boolean isComplete(E element)
         {
            return myExtractor.isComplete(element) || myIndex >= 1;
         }
      };
   }
}
