package net.sf.jagg.msd;

import java.util.List;
import java.util.ArrayList;

/**
 * A <code>BooleanDiscriminator</code> discriminates <code>Lists</code> of
 * <code>Booleans</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class BooleanDiscriminator extends AbstractDiscriminator<Boolean>
{
   /**
    * Partitions the given <code>List</code> of values into another
    * <code>List</code>, in which all of the values from the given list exist
    * in the new list, and all values that compare equal are adjacent to each
    * other, according to the given <code>Extractor</code>.
    * @param elements A <code>List</code> of elements.
    * @param extractor An <code>Extractor</code> that gives <em>labels</em> for
    *    each element.
    * @param workspace The <code>MsdWorkspace</code> used in the discrimination process.
    * @return A <code>List</code> of <code>Lists</code> containing all
    *    equivalence classes.  Each equivalence class list contains all values
    *    that compare equal to each other.
    */
   public <E> List<List<E>> discriminate(List<E> elements, Extractor<E, Boolean> extractor, MsdWorkspace workspace)
   {
      List<E> trues = new ArrayList<E>();
      List<E> falses = new ArrayList<E>();
      int size = elements.size();
      for (int i = 0; i < size; i++)
      {
         E element = elements.get(i);
         if (extractor.getLabel(element))
            trues.add(element);
         else
            falses.add(element);
      }
      List<List<E>> equivClasses = new ArrayList<List<E>>();
      if (!trues.isEmpty())
         equivClasses.add(trues);
      if (!falses.isEmpty())
         equivClasses.add(falses);

      return equivClasses;
   }
}