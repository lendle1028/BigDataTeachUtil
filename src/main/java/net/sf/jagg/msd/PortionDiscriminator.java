package net.sf.jagg.msd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A <code>PortionDiscriminator</code> is an abstract class that represents
 * all <code>Discriminators</code> that need to discriminate in steps, whether
 * it be characters in a string, elements of an array, or bit portions of a
 * number.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public abstract class PortionDiscriminator<T> extends AbstractDiscriminator<T>
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
    *    that compare equal to each other.
    */
   @SuppressWarnings("ForLoopReplaceableByForEach")
   public <E> List<List<E>> discriminate(List<E> elements, Extractor<E, T> extractor, MsdWorkspace workspace)
   {
      if (DEBUG)
         System.err.println("  " + getClass().getName() + " elements: " + Arrays.deepToString(elements.toArray()));

      if (elements.size() == 0)
         return new ArrayList<List<E>>(0);
      
      List<List<E>> curr = new ArrayList<List<E>>(1);
      curr.add(elements);
      List<List<E>> equivClasses;
      PortionExtractor<E, T> portionExtractor = getPortionExtractor(extractor);
      List<List<E>> results = new ArrayList<List<E>>();
      // Index loop.
      int index = 0;
      while (!curr.isEmpty())
      {
         List<List<E>> next = new ArrayList<List<E>>();
         portionExtractor.setIndex(index);
         if (DEBUG)
            System.err.println("    index: " + index);
         // Loop through each equivalence class to create sub-equivalence
         // classes.
         for (int i = 0; i < curr.size(); i++)
         {
            List<E> currElements = curr.get(i);
            equivClasses = discriminatePortion(currElements, portionExtractor, workspace);
            if (DEBUG)
            {
               System.err.println("      " + getClass().getName() + " equivClasses:");
               for (List<E> equivClass : equivClasses)
                  System.err.println("        " + Arrays.deepToString(equivClass.toArray()));
            }
            // If the condition says for all of the elements that they all
            // terminated, then they are all equivalent, in one class.
            if (equivClasses.size() == 1 && portionExtractor.isAllComplete())
            {
               results.add(equivClasses.get(0));
            }
            else
            {
               // Store the sub-equivalence classes.
               for (int j = 0; j < equivClasses.size(); j++)
               {
                  List<E> equivClass = equivClasses.get(j);
                  // Don't bother with classes of size 1, which won't change in
                  // subsequent loops.
                  if (equivClass.size() > 1)
                     next.add(equivClass);
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
         System.err.println("    " + getClass().getName() + " results: ");
         for (List<E> result : results)
            System.err.println("      " + Arrays.deepToString(result.toArray()));
      }

      return results;
   }

   /**
    * Perform multiset discrimination for a portion of the elements.  This is
    * meant as a step in the full discrimination.  For this reason, we must
    * return the elements, so subsequent steps may be performed.
    * @param elements A <code>List</code> of <code>Lists</code> of elements.
    * @param extractor A <code>PortionExtractor</code>.  It is capable of
    *    creating labels out of portions of an element.  It also specifies when
    *    certain elements are "complete", e.g. no more characters in a string.
    *    Such elements are separated into their own equivalence class apart
    *    from the rest of the process.
    * @param workspace A <code>MsdWorkspace</code>.
    * @return A <code>List</code> of <code>Lists</code> containing all
    *    (portion-defined) equivalence classes.  Each equivalence class list
    *    contains all values that compare equal to each other, as far as the
    *    portion is concerned.
    * @param <E> The type of elements to discriminate.
    */
   @SuppressWarnings("unchecked")
   protected <E> List<List<E>> discriminatePortion(List<E> elements, PortionExtractor<E, T> extractor,
      MsdWorkspace workspace)
   {
      int usedSize = 0;
      int size = elements.size();
      List<E> completed = new ArrayList<E>();
      for (int i = 0; i < size; i++)
      {
         E element = elements.get(i);
         if (extractor.isComplete(element))
         {
            completed.add(element);
         }
         else
         {
            int index = extractor.getLabel(element);
            List list = workspace.myLists[index];
            if (list == null)
            {
               workspace.myUsedIndexes[usedSize++] = index;
               list = new ArrayList<T>();
               workspace.myLists[index] = list;
            }
            list.add(element);
         }
      }
      List<List<E>> result = new ArrayList<List<E>>(usedSize);
      if (!completed.isEmpty())
         result.add(completed);
      // Notify the PortionExtractor if all labels were complete.  This is used
      // as a termination condition.
      extractor.setAllComplete(usedSize == 0);
      for (int i = 0; i < usedSize; i++)
      {
         int index = workspace.myUsedIndexes[i];
         result.add(workspace.myLists[index]);
         // Empty the workspace as we exit.
         workspace.myLists[index] = null;
      }
      return result;
   }

   /**
    * Returns an appropriate <code>PortionExtractor</code>.
    * @param extractor A <code>PortionExtractor</code> that returns appropriate
    *    labels.
    * @return An appropriate <code>PortionExtractor</code>.
    * @param <E> The type of the element.
    */
   protected abstract <E> PortionExtractor<E, T> getPortionExtractor(Extractor<E, T> extractor);
}
