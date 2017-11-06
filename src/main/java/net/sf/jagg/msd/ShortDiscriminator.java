package net.sf.jagg.msd;

/**
 * A <code>ShortDiscriminator</code> discriminates <code>Lists</code> of
 * <code>Shorts</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class ShortDiscriminator extends NumberDiscriminator<Short>
{
   /**
    * Returns a portion extractor appropriate for <code>shorts</code>.
    * @param e An <code>Extractor</code> that returns appropriate labels.
    * @return An appropriate <code>PortionExtractor</code>.
    */
   protected <E> PortionExtractor<E, Short> getPortionExtractor(Extractor<E, Short> e)
   {
      return new PortionExtractor<E, Short>(e) {
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