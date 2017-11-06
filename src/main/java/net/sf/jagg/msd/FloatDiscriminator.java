package net.sf.jagg.msd;

/**
 * A <code>FloatDiscriminator</code> discriminates <code>Lists</code> of
 * <code>Floats</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class FloatDiscriminator extends NumberDiscriminator<Float>
{
   /**
    * Returns a portion extractor appropriate for <code>floats</code>.
    * @param e An <code>Extractor</code> that returns appropriate labels.
    * @return An appropriate <code>PortionExtractor</code>.
    */
   protected <E> PortionExtractor<E, Float> getPortionExtractor(Extractor<E, Float> e)
   {
      return new PortionExtractor<E, Float>(e) {
         /**
          * The label is the specific portion of the element, cast as an
          * integer.
          * @param element The element.
          * @return The specific portion of the element, cast as an integer.
          */
         public int getLabel(E element)
         {
            return (Float.floatToIntBits(myExtractor.getLabel(element)) >> (myIndex * PORTION_BITS)) & PORTION_MASK;
         }

         /**
          * Complete after 2 portions.
          * @param element The element.
          * @return <code>true</code> if the index is at least 2,
          *    <code>false</code> otherwise.
          */
         public boolean isComplete(E element)
         {
            return myExtractor.isComplete(element) || myIndex >= 2;
         }
      };
   }
}