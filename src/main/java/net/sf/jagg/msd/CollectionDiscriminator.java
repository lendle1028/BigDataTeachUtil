package net.sf.jagg.msd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A <code>CollectionDiscriminator</code> discriminates
 * <code>Lists</code> of <code>Collections</code>.  The <code>Collections</code>
 * are the items being discriminated.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class CollectionDiscriminator<T> extends ChainedDiscriminator<Collection<T>>
{
   /**
    * Partitions the given <code>List</code> of elements into another
    * <code>List</code>, in which all of the elements from the given list exist
    * in the new list, and all elements that compare equal are adjacent to each
    * other, according to the given <code>Extractor</code>.
    * @param elements A <code>List</code> of elements.
    * @param extractor An <code>Extractor</code> that gives <em>labels</em> for
    *    each element.
    * @param workspace The <code>MsdWorkspace</code> used in the discrimination
    *    process.
    * @return A <code>List</code> of <code>Lists</code> containing all
    *    equivalence classes.  Each equivalence class list contains all elements
    *    that compare equal to each other.
    */
   public <E> List<List<E>> discriminate(List<E> elements, Extractor<E, Collection<T>> extractor, MsdWorkspace workspace)
   {
      RandomAccessListDiscriminator<T> listDiscr = new RandomAccessListDiscriminator<T>();
      ChainedExtractor<E, List<T>, Collection<T>> ce = getChainedExtractor(elements, extractor);
      return listDiscr.discriminate(elements, ce, workspace);
   }

   /**
    * Returns an appropriate <code>ChainedExtractor</code>.
    * @param elements The <code>List</code> of elements.
    * @param extractor A <code>ChainedExtractor</code> that returns appropriate
    *    labels.
    * @return An appropriate <code>ChainedExtractor</code>.
    */
   protected <E> ChainedExtractor<E, List<T>, Collection<T>> getChainedExtractor(List<E> elements, Extractor<E, Collection<T>> extractor)
   {
      return new CollectionChainedExtractor<E, T>(extractor);
   }

   /**
    * A <code>CollectionChainedExtractor</code> turns <code>Collections</code>
    * into <code>RandomAccess</code> <code>Lists</code>.
    * @param <E> The type of element.
    * @param <B> The base type of the collection.
    */
   protected class CollectionChainedExtractor<E, B> extends ChainedExtractor<E, List<B>, Collection<B>>
   {
      /**
       * Create a <code>CollectionChainedExtractor</code> that uses the given
       * <code>Extractor</code> to retrieve the collection.
       * @param extractor An <code>Extractor</code> whose label is a collection.
       */
      public CollectionChainedExtractor(Extractor<E, Collection<B>> extractor)
      {
         super(extractor);
      }

      /**
       * The label is a random access list containing all members of the
       * collection.
       * @param element The element.
       * @return A random access list containing all members of the
       *    collection.
       */
      public List<B> getLabel(E element)
      {
         Collection<B> collection = myExtractor.getLabel(element);
         List<B> list = new ArrayList<B>();
         list.addAll(collection);
         return list;
      }

      /**
       * Completeness doesn't matter for this extractor.
       * @param element The element.
       * @return <code>false</code>.
       */
      public boolean isComplete(E element)
      {
         return myExtractor.isComplete(element);
      }
   }

   /**
    * Returns the <code>Discriminator</code> that discriminates on the
    * collection's base type.
    * @param elements The list of elements.
    * @param extractor The <code>ChainedExtractor</code> that was obtained from
    *    <code>getChainedExtractor</code>.
    * @param index The index of the loop.
    * @return A <code>Discriminator</code> that discriminates on the
    *    collection's base type.
    */
   @SuppressWarnings({"unchecked", "ForLoopReplaceableByForEach"})
   protected <E> Discriminator<T> getDiscriminator(List<E> elements, ChainedExtractor<E, ?, Collection<T>> extractor,
      int index)
   {
      for (int i = 0; i < elements.size(); i++)
      {
         E element = elements.get(i);
         if (!extractor.isComplete(element))
         {
            List<T> member = (List<T>) extractor.getLabel(element);
            if (member != null)
            {
               return (Discriminator<T>) Discriminators.getDiscriminator(member.getClass());
            }
         }
      }
      return new NullDiscriminator<T>(null);
   }
}