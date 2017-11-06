package net.sf.jagg.msd;

/**
 * A <code>DoubleDiscriminator</code> discriminates <code>Lists</code> of
 * <code>Doubles</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class DoubleDiscriminator extends NumberDiscriminator<Double>
{
   /**
    * Returns a portion extractor appropriate for <code>doubles</code>.
    * @param e An <code>Extractor</code> that returns appropriate labels.
    * @return An appropriate <code>PortionExtractor</code>.
    */
   protected <E> PortionExtractor<E, Double> getPortionExtractor(Extractor<E, Double> e)
   {
      return new PortionExtractor<E, Double>(e) {
         /**
          * The label is the specific portion of the element, cast as an
          * integer.
          * @param element The element.
          * @return The specific portion of the element, cast as an integer.
          */
         public int getLabel(E element)
         {
            return (int) (Double.doubleToLongBits(myExtractor.getLabel(element)) >> (myIndex * PORTION_BITS)) & PORTION_MASK;
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