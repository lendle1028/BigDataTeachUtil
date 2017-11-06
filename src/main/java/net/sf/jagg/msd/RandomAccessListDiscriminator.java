package net.sf.jagg.msd;

import java.util.List;

/**
 * A <code>RandomAccessListDiscriminator</code> discriminates
 * <code>Lists</code> of <code>Lists</code>.  The <code>Lists</code> are the
 * items being discriminated and they should implement
 * <code>RandomAccess</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 * @see java.util.RandomAccess
 */
public class RandomAccessListDiscriminator<T> extends ChainedDiscriminator<List<T>>
{
   /**
    * Returns an appropriate <code>ChainedExtractor</code>.
    * @param elements The <code>List</code> of elements.
    * @param extractor A <code>ChainedExtractor</code> that returns appropriate
    *    labels.
    * @return An appropriate <code>ChainedExtractor</code>.
    */
   protected <E> ChainedExtractor<E, T, List<T>> getChainedExtractor(List<E> elements, Extractor<E, List<T>> extractor)
   {
      return new ListChainedExtractor<E, T>(extractor);
   }

   /**
    * A <code>ListChainedExtractor</code> extracts members of a random access
    * list as labels.
    * @param <E> The type of element.
    * @param <B> The base type of the list.
    */
   protected class ListChainedExtractor<E, B> extends ChainedExtractor<E, B, List<B>>
   {
      /**
       * Create an <code>ListChainedExtractor</code> that uses the given
       * <code>Extractor</code> to retrieve the list.
       * @param extractor An <code>Extractor</code> whose label is a list.
       */
      public ListChainedExtractor(Extractor<E, List<B>> extractor)
      {
         super(extractor);
      }

      /**
       * The label is the specific member of the list.
       * @param element The element.
       * @return A specific member of the list.
       */
      public B getLabel(E element)
      {
         return myExtractor.getLabel(element).get(myIndex);
      }

      /**
       * The discrimination is complete when the process runs off the end of
       * the list.
       * @param element The element.
       * @return <code>true</code> if off the end of the list,
       *    <code>false</code> otherwise.
       */
      public boolean isComplete(E element)
      {
         return myExtractor.isComplete(element) || myIndex >= myExtractor.getLabel(element).size();
      }
   }

   /**
    * Returns the <code>Discriminator</code> that discriminates on the list's
    * base type.
    * @param elements The list of elements.
    * @param extractor The <code>ChainedExtractor</code> that was obtained from
    *    <code>getChainedExtractor</code>.
    * @param index The index of the loop.
    * @return A <code>Discriminator</code> that discriminates on the list's
    *    base type.
    */
   @SuppressWarnings({"unchecked", "ForLoopReplaceableByForEach"})
   protected <E> Discriminator<T> getDiscriminator(List<E> elements, ChainedExtractor<E, ?, List<T>> extractor,
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