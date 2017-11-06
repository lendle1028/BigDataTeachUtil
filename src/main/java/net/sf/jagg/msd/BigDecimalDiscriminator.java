package net.sf.jagg.msd;

import java.math.BigDecimal;

/**
 * A <code>BigDecimalDiscriminator</code> discriminates <code>Lists</code> of
 * <code>BigDecimals</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class BigDecimalDiscriminator extends NumberDiscriminator<BigDecimal>
{
   /**
    * Returns a portion extractor appropriate for <code>BigDecimals</code>.
    * @param e An <code>Extractor</code> that returns appropriate labels.
    * @return An appropriate <code>PortionExtractor</code>.
    */
   protected <E> PortionExtractor<E, BigDecimal> getPortionExtractor(Extractor<E, BigDecimal> e)
   {
      return new PortionExtractor<E, BigDecimal>(e) {
         /**
          * If index is 0 or 1, then the label will be the high-order or low-
          * order 16 bits of the scale, respectively.  All other indexes will
          * have 2 subtracted, then be treated as an index into the byte array
          * of the significand, which is a <code>BigInteger</code>.
          * @param element The element.
          * @return The specific portion of the element, cast as an integer.
          */
         public int getLabel(E element)
         {
            // Stripping trailing zeroes makes BDs with same mathematical value
            // have the same representation.  I.e. [6.00] => [6]
            BigDecimal bd = myExtractor.getLabel(element).stripTrailingZeros();
            if (myIndex < 2)
            {
               int scalePortion = bd.scale();
               scalePortion >>= (1 - myIndex) * PORTION_BITS;
               return scalePortion & PORTION_MASK;
            }
            // Treat "our index minus two" as the index into the byte array.
            return bd.unscaledValue().toByteArray()[myIndex - 2] & PORTION_MASK;
         }

         /**
          * Complete after the end of the byte array representation.
          * @param element The element.
          * @return <code>true</code> if we are off the end of the byte array,
          *    representation, <code>false</code> otherwise.
          */
         public boolean isComplete(E element)
         {
            // Take into the account that we are "cramming" two index positions
            // for the scale first, before the actual byte array.
            return myExtractor.isComplete(element) ||
               myIndex >= myExtractor.getLabel(element).stripTrailingZeros().unscaledValue().toByteArray().length + 2;
         }
      };
   }
}