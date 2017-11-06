package net.sf.jagg.msd;

import java.math.BigInteger;

/**
 * A <code>BigIntegerDiscriminator</code> discriminates <code>Lists</code> of
 * <code>BigIntegers</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class BigIntegerDiscriminator extends NumberDiscriminator<BigInteger>
{
   /**
    * Returns a portion extractor appropriate for <code>BigIntegers</code>.
    * @param e An <code>Extractor</code> that returns appropriate labels.
    * @return An appropriate <code>PortionExtractor</code>.
    */
   protected <E> PortionExtractor<E, BigInteger> getPortionExtractor(Extractor<E, BigInteger> e)
   {
      return new PortionExtractor<E, BigInteger>(e) {
         /**
          * The label is the specific portion of the element, cast as an
          * integer, taken from the byte array of the <code>BigInteger</code>.
          * @param element The element.
          * @return The specific portion of the element, cast as an integer.
          */
         public int getLabel(E element)
         {
            return myExtractor.getLabel(element).toByteArray()[myIndex] & PORTION_MASK;
         }

         /**
          * Complete after the end of the byte array representation.
          * @param element The element.
          * @return <code>true</code> if we are off the end of the byte array,
          *    representation, <code>false</code> otherwise.
          */
         public boolean isComplete(E element)
         {
            return myExtractor.isComplete(element) || myIndex >= myExtractor.getLabel(element).toByteArray().length;
         }
      };
   }
}