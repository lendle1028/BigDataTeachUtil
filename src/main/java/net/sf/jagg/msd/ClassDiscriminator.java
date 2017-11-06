package net.sf.jagg.msd;

import java.util.List;

/**
 * A <code>ClassDiscriminator</code> discriminates <code>Lists</code> of
 * <code>Objects</code> by their class names.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class ClassDiscriminator<T> extends ChainedDiscriminator<T>
{
   /**
    * Returns an appropriate <code>ChainedExtractor</code>.
    * @param elements The <code>List</code> of elements.
    * @param extractor A <code>ChainedExtractor</code> that returns appropriate
    *    labels.
    * @return An appropriate <code>ChainedExtractor</code>.
    */
   protected <E> ChainedExtractor<E, String, T> getChainedExtractor(List<E> elements, Extractor<E, T> extractor)
   {
      return new ClassChainedExtractor<E, T>(extractor);
   }

   /**
    * A <code>ClassChainedExtractor</code> extracts the class name.
    * @param <E> The type of element.
    * @param <B> The type of object.
    */
   protected class ClassChainedExtractor<E, B> extends ChainedExtractor<E, String, B>
   {
      /**
       * Create a <code>ClassChainedExtractor</code> that uses the given
       * <code>Extractor</code> to retrieve the <code>Class</code> name.
       * @param extractor An <code>Extractor</code> whose label is a class
       *    name.
       */
      public ClassChainedExtractor(Extractor<E, B> extractor)
      {
         super(extractor);
      }

      /**
       * The label is the specific member of the array.
       * @param element The element.
       * @return A specific member of the array.
       */
      public String getLabel(E element)
      {
         return myExtractor.getLabel(element).getClass().getName();
      }

      /**
       * The discrimination is complete when the chained <code>Extractor</code>
       * is complete.
       * @param element The element.
       * @return <code>true</code> if complete, <code>false</code> otherwise.
       */
      public boolean isComplete(E element)
      {
         // Only check the class names once!
         return myExtractor.isComplete(element) || myIndex >= 1;
      }
   }

   /**
    * Returns the <code>Discriminator</code> that discriminates on the class
    * name of the objects.
    * @param elements The list of elements.
    * @param extractor The <code>ChainedExtractor</code> that was obtained from
    *    <code>getChainedExtractor</code>.
    * @param index The index of the loop.
    * @return A <code>Discriminator</code> that discriminates on the class name
    *    of the objects.
    */
   protected <E> Discriminator<String> getDiscriminator(List<E> elements, ChainedExtractor<E, ?, T> extractor,
      int index)
   {
      return new CharSequenceDiscriminator<String>();
   }
}