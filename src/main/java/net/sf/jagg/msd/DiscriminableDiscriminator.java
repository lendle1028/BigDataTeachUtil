package net.sf.jagg.msd;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>DiscriminableDiscriminator</code> assumes that the type of elements
 * is <code>Discriminable</code>.  It extracts the properties, then delegates
 * to a <code>PropertiesDiscriminator</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 * @see PropertiesDiscriminator
 */
public class DiscriminableDiscriminator<T> extends AbstractDiscriminator<T>
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
    * @throws ClassCastException If an encountered object was not
    *    <code>Discriminable</code>.
    * @see Discriminable
    */
   public <E> List<List<E>> discriminate(List<E> elements, Extractor<E, T> extractor, MsdWorkspace workspace)
   {
      int size = elements.size();
      if (size == 0)
         return new ArrayList<List<E>>(0);

      T label = null;
      for (int i = 0; i < size; i++)
      {
         E element = elements.get(i);
         label = extractor.getLabel(element);
         if (label != null)
            break;
      }

      Discriminator<T> pd;
      if (label == null)
      {
         // All nulls
         pd = new NullDiscriminator<T>(null);
      }
      else
      {
         Discriminable d = (Discriminable) label;
         List<String> properties = d.getDiscriminableProperties();
         pd = new PropertiesDiscriminator<T>(properties);
      }
      return pd.discriminate(elements, extractor, workspace);
   }
}
