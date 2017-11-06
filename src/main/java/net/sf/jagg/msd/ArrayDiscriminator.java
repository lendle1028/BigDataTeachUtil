package net.sf.jagg.msd;

import java.util.List;

/**
 * An <code>ArrayDiscriminator</code> discriminates <code>Lists</code> of
 * arrays.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class ArrayDiscriminator<T> extends ChainedDiscriminator<T[]>
{
   /**
    * Returns an appropriate <code>ChainedExtractor</code>.
    * @param elements The <code>List</code> of elements.
    * @param extractor A <code>ChainedExtractor</code> that returns appropriate
    *    labels.
    * @return An appropriate <code>ChainedExtractor</code>.
    */
   protected <E> ChainedExtractor<E, T, T[]> getChainedExtractor(List<E> elements, Extractor<E, T[]> extractor)
   {
      return new ArrayChainedExtractor<E, T>(extractor);
   }

   /**
    * An <code>ArrayChainedExtractor</code> extracts members of an array as
    * labels.
    * @param <E> The type of element.
    * @param <B> The base type of the array.
    */
   protected class ArrayChainedExtractor<E, B> extends ChainedExtractor<E, B, B[]>
   {
      /**
       * Create an <code>ArrayChainednExtractor</code> that uses the given
       * <code>Extractor</code> to retrieve the array.
       * @param extractor An <code>Extractor</code> whose label is an array.
       */
      public ArrayChainedExtractor(Extractor<E, B[]> extractor)
      {
         super(extractor);
      }

      /**
       * The label is the specific member of the array.
       * @param element The element.
       * @return A specific member of the array.
       */
      public B getLabel(E element)
      {
         return myExtractor.getLabel(element)[myIndex];
      }

      /**
       * The discrimination is complete when the process runs off the end of
       * the array.
       * @param element The element.
       * @return <code>true</code> if off the end of the array,
       *    <code>false</code> otherwise.
       */
      public boolean isComplete(E element)
      {
         return myExtractor.isComplete(element) || myIndex >= myExtractor.getLabel(element).length;
      }
   }

   /**
    * Returns the <code>Discriminator</code> that discriminates on the array's
    * base type.
    * @param elements The list of elements.
    * @param extractor The <code>ChainedExtractor</code> that was obtained from
    *    <code>getChainedExtractor</code>.
    * @param index The index of the loop.
    * @return A <code>Discriminator</code> that discriminates on the array's
    *    base type.
    */
   @SuppressWarnings({"unchecked", "ForLoopReplaceableByForEach"})
   protected <E> Discriminator<T> getDiscriminator(List<E> elements, ChainedExtractor<E, ?, T[]> extractor,
      int index)
   {
      for (int i = 0; i < elements.size(); i++)
      {
         E element = elements.get(i);
         if (!extractor.isComplete(element))
         {
            T member = (T) extractor.getLabel(element);
            if (member != null)
            {
               return (Discriminator<T>) Discriminators.getDiscriminator(member.getClass());
            }
         }
      }
      return new NullDiscriminator<T>(null);
   }
}
