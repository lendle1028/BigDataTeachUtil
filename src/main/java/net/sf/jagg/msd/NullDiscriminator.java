package net.sf.jagg.msd;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>NullDiscriminator</code> extracts <code>nulls</code> into a separate
 * equivalence class, then calls another discriminator.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class NullDiscriminator<T> extends AbstractDiscriminator<T>
{
   /**
    * The decorated <code>Discriminator</code>.
    */
   private Discriminator<T> myDiscriminator;

   /**
    * Create a <code>NullDiscriminator</code> that decorates another
    * <code>Discriminator</code>, to allow <code>null</code> labels.
    * @param discriminator Another <code>Discriminator</code>.
    */
   public NullDiscriminator(Discriminator<T> discriminator)
   {
      myDiscriminator = discriminator;
   }

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
   public <E> List<List<E>> discriminate(List<E> elements, Extractor<E, T> extractor, MsdWorkspace workspace)
   {
      List<E> nulls = new ArrayList<E>();
      List<E> nonNulls = new ArrayList<E>();
      int size = elements.size();
      for (int i = 0; i < size; i++)
      {
         E element = elements.get(i);
         if (extractor.getLabel(element) == null)
            nulls.add(element);
         else
            nonNulls.add(element);
      }
      // Note: if all nulls, then the nested Discriminator is not necessary.
      List<List<E>> equivClasses = (nonNulls.isEmpty()) ?
         new ArrayList<List<E>>(1) :
         myDiscriminator.discriminate(nonNulls, extractor, workspace);
      if (!nulls.isEmpty())
         equivClasses.add(nulls);
      
      return equivClasses;
   }
}
