package net.sf.jagg.msd;

/**
 * A <code>CharSequenceDiscriminator</code> discriminates <code>Lists</code> of
 * <code>CharSequences</code>, such as <code>Strings</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class CharSequenceDiscriminator<T extends CharSequence> extends PortionDiscriminator<T>
{
   /**
    * Returns an appropriate <code>PortionExtractor</code>.
    * @param extractor A <code>PortionExtractor</code> that returns appropriate
    *    labels.
    * @return An appropriate <code>PortionExtractor</code>.
    */
   protected <E> PortionExtractor<E, T> getPortionExtractor(Extractor<E, T> extractor)
   {
      return new CharPortionExtractor<E>(extractor);
   }

   /**
    * A <code>CharPortionExtractor</code> is a <code>PortionExtractor</code>
    * that extracts portions of <code>CharSequences</code> for their labels.
    */
   protected class CharPortionExtractor<E> extends PortionExtractor<E, T>
   {
      /**
       * Create a <code>CharPortionExtractor</code> that first uses the given
       * <code>Extractor</code> to get the value.
       * @param extractor Another <code>Extractor</code>.
       */
      public CharPortionExtractor(Extractor<E, T> extractor)
      {
         super(extractor);
      }

      /**
       * Returns the label, which is the specific character at the index,
       * converted to an integer.
       * @param element The element.
       * @return The the specific character at the index, converted to an
       *    integer.
       */
      public int getLabel(E element)
      {
         return (int) myExtractor.getLabel(element).charAt(myIndex);
      }

      /**
       * Discrimination is complete if the index has reached the length of the
       * <code>CharSequence</code>.
       * @param element An element.
       * @return <code>true</code> if off the end of the
       *    <code>CharSequence</code>, <code>false</code> otherwise.
       */
      public boolean isComplete(E element)
      {
         if (!myExtractor.isComplete(element))
         {
            int length = myExtractor.getLabel(element).length();
            return myIndex >= length;
         }
         return true;
      }
   }
}
