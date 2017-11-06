package net.sf.jagg.msd;

import java.util.List;

/**
 * An <code>EnumDiscriminator</code> discriminates <code>Lists</code> of
 * <code>Enums</code> by their ordinals.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class EnumDiscriminator<T extends Enum<T>> extends ChainedDiscriminator<T>
{
   /**
    * Returns an appropriate <code>ChainedExtractor</code>.
    * @param elements The <code>List</code> of elements.
    * @param extractor A <code>ChainedExtractor</code> that returns appropriate
    *    labels.
    * @return An appropriate <code>ChainedExtractor</code>.
    */
   protected <E> ChainedExtractor<E, Integer, T> getChainedExtractor(List<E> elements, Extractor<E, T> extractor)
   {
      return new EnumChainedExtractor<E>(extractor);
   }

   /**
    * An <code>EnumChainedExtractor</code> extracts the enum ordinal.
    * @param <E> The type of element.
    */
   protected class EnumChainedExtractor<E> extends ChainedExtractor<E, Integer, T>
   {
      /**
       * Create an <code>EnumChainedExtractor</code> that uses the given
       * <code>Extractor</code> to retrieve the array.
       * @param extractor An <code>Extractor</code> whose label is an integer.
       */
      public EnumChainedExtractor(Extractor<E, T> extractor)
      {
         super(extractor);
      }

      /**
       * The label is the ordinal of the <code>Enum</code>.
       * @param element The element.
       * @return The ordinal of the <code>Enum</code>.
       */
      public Integer getLabel(E element)
      {
         return myExtractor.getLabel(element).ordinal();
      }

      /**
       * The discrimination is complete when the chained <code>Extractor</code>
       * is complete.
       * @param element The element.
       * @return <code>true</code> if complete, <code>false</code> otherwise.
       */
      public boolean isComplete(E element)
      {
         // Only check the ordinal once!
         return myExtractor.isComplete(element) || myIndex >= 1;
      }
   }

   /**
    * Returns the <code>Discriminator</code> that discriminates on the enum
    * ordinals.
    * @param elements The list of elements.
    * @param extractor The <code>ChainedExtractor</code> that was obtained from
    *    <code>getChainedExtractor</code>.
    * @param index The index of the loop.
    * @return A <code>Discriminator</code> that discriminates on the enum
    *    ordinals.
    */
   protected <E> Discriminator<Integer> getDiscriminator(List<E> elements, ChainedExtractor<E, ?, T> extractor,
      int index)
   {
      return new IntegerDiscriminator();
   }
}