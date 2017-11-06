package net.sf.jagg.msd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A <code>ChainedDiscriminator</code> relies on another
 * <code>Discriminator</code> to perform its work in a separate step, after
 * this discriminator's work is done.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public abstract class ChainedDiscriminator<T> extends AbstractDiscriminator<T>
{
   private static final boolean DEBUG = false;

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
    *    that compare equal to each other.  If for any reason, this is unable
    *    to discriminate the <code>List</code>, e.g. the elements are non-
    *    <code>Discriminable</code> objects, then this returns
    *    <code>null</code>.
    */
   @SuppressWarnings({"unchecked","ForLoopReplaceableByForEach"})
   public <E> List<List<E>> discriminate(List<E> elements, Extractor<E, T> extractor, MsdWorkspace workspace)
   {
      if (DEBUG)
         System.err.println(getClass().getName() + " elements: " + Arrays.deepToString(elements.toArray()));

      if (elements.size() == 0)
         return new ArrayList<List<E>>(0);

      List<List<E>> curr = new ArrayList<List<E>>(1);
      curr.add(elements);
      List<List<E>> equivClasses;
      List<List<E>> results = new ArrayList<List<E>>();
      ChainedExtractor/*<E, ?, T>*/ chainedExtractor = getChainedExtractor(elements, extractor);

      // Index loop.
      int index = 0;
      // Cannot get a wildcard for the extractor -- which could possibly
      // be any type -- to line up with the wildcard for the
      // discriminator.  That is, one <capture ?> cannot equal another
      // <capture ?>.  That's okay, the type might vary from one loop
      // iteration to the next.
      while (!curr.isEmpty())
      {
         Discriminator/*<?>*/ discr = getDiscriminator(elements, chainedExtractor, index);
         if (discr == null)
         {
            // Failed to get a Discriminator; can't discriminate.
            return null;
         }

         List<List<E>> next = new ArrayList<List<E>>();
         if (DEBUG)
            System.err.println("  index: " + index);

         chainedExtractor.setIndex(index);

         // Loop through each equivalence class to create sub-equivalence
         // classes.
         for (int i = 0; i < curr.size(); i++)
         {
            List<E> currElements = curr.get(i);
            List<E> finished = new ArrayList<E>();
            List<E> remaining = new ArrayList<E>();
            // Take out those elements that are complete according to the given extractor.
            for (int j = 0; j < currElements.size(); j++)
            {
               E element = currElements.get(j);
               if (chainedExtractor.isComplete(element))
                  finished.add(element);
               else
                  remaining.add(element);
            }
            // The discriminator will mark the "complete" property in portionExtractor.
            equivClasses = (List<List<E>>) discr.discriminate(remaining, chainedExtractor, workspace);
            if (DEBUG)
            {
               System.err.println("    " + getClass().getName() + " equivClasses:");
               for (List<E> equivClass : equivClasses)
                  System.err.println("      " + Arrays.deepToString(equivClass.toArray()));
               if (!finished.isEmpty())
               {
                  System.err.println("      finished:" + Arrays.deepToString(finished.toArray()));
               }
            }

            // Add in finished results in their own equivalence class, if any.
            if (!finished.isEmpty())
               results.add(finished);
            // If the condition says for all of the elements that they all
            // terminated, then they are all equivalent, in one class.
            if (equivClasses.size() == 1 && chainedExtractor.isAllComplete())
            {
               results.add(equivClasses.get(0));
            }
            else
            {
               // Store the sub-equivalence classes.
               for ( int j = 0; j < equivClasses.size(); j++)
               {
                  List<E> equivClass = equivClasses.get(j);
                  // Don't bother with classes of size 1, which won't change in
                  // subsequent loops.
                  if (equivClass.size() > 1)
                  {
                     next.add(equivClass);
                     if (DEBUG)
                        System.err.println("    Sending equivClass to next loop: " + Arrays.deepToString(equivClass.toArray()));
                  }
                  else
                     results.add(equivClass);
               }
            }
         }

         // Prepare for next loop.
         curr = next;
         index++;
      }

      // Add the last iteration in.
      int size = curr.size();
      // Avoid creating an Iterator in a call to "addAll".
      for (int i = 0; i < size; i++)
      {
         results.add(curr.get(i));
      }

      if (DEBUG)
      {
         System.err.println("  " + getClass().getName() + " results: ");
         for (List<E> result : results)
            System.err.println("    " + Arrays.deepToString(result.toArray()));
      }

      return results;
   }

   /**
    * Returns an <code>Extractor</code> that extracts a label of unknown type
    * from a label of type <code>T</code>, using the given
    * <code>Extractor</code>, which supplies labels of type <code>T</code>.
    * For the same index, the returned <code>Extractor</code> must return
    * labels of the same type as the type discriminated by the
    * <code>Discriminator</code> returned by <code>getDiscriminator</code>.
    * @param elements The <code>List</code> of elements.
    * @param extractor The <code>Extractor</code> that extracts a label of type
    *     <code>T</code> from the element.
    * @param <E> The type of element.
    * @return A <code>ChainedExtractor</code> that extracts a label of unknown
    *    type from a label of type <code>T</code>.
    * @see #getDiscriminator
    */
   protected abstract <E> ChainedExtractor<E, ?, T> getChainedExtractor(List<E> elements, Extractor<E, T> extractor);

   /**
    * Returns a <code>Discriminator</code> that discriminates on an unknown
    * type.  For the same index, the returned <code>Discriminator</code> must
    * discriminate on the same type as the labels that are returned by the
    * <code>ChainedExtractor</code> that is returned by
    * <code>getChainedExtractor</code>.  If it is known that no more loops are
    * necessary, then the returned <code>Discriminator</code> may be
    * <code>null</code>.
    * @param elements The list of elements.
    * @param extractor The <code>ChainedExtractor</code> that was obtained from
    *    <code>getChainedExtractor</code>.
    * @param index The index of the loop.
    * @return A <code>Discriminator</code>, or <code>null</code> if it is known
    *    for sure that no more loops are necessary.
    * @see #getChainedExtractor
    */
   protected abstract <E> Discriminator<?> getDiscriminator(List<E> elements, ChainedExtractor<E, ?, T> extractor,
      int index);

}
