package net.sf.jagg.msd;

import java.util.List;

/**
 * <p>A <code>Discriminator</code> is able to distinguish elements of a given
 * <code>List</code> of objects of a certain type.  It partitions the given
 * list into a <code>List</code> of <em>equivalence classes</em>.  An
 * <em>equivalence class</em> is represented by a <code>List</code> of objects
 * that compare equal, according to this <code>Discriminator</code>.</p>
 *
 * <p>For example, in an unsorted list, one may have different values scattered
 * randomly throughout the list:</p>
 * <code>{100, 23, 5, 23, 10, 10, 5, 23, 6}</code>
 * <p>The discrimination process returns a new <code>List</code> of
 * <code>Lists</code>, each list containing an equivalence class.  Each
 * equivalence class list contains all values that compare equal to each other.
 * They are not necessarily in sorted order.  For example, integer
 * discrimination of the above list yields the following result:</p>
 * <code>{{100}, {6}, {23, 23, 23}, {5, 5}, {10, 10}}</code>
 * <p>This algorithm is <em>stable</em>, meaning that values that do compare
 * equal to each other remain in the same order as before, i.e. the first 5
 * remains before the second 5 in the equivalence class list.</p>
 *
 * @param <T> The type being discriminated.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public interface Discriminator<T>
{
   /**
    * Partitions the given <code>List</code> of elements into another
    * <code>List</code>, in which all of the elements from the given list exist
    * in the new list, and all elements that compare equal are adjacent to each
    * other.
    * @param elements A <code>List</code> of elements.
    * @param workspace The <code>MsdWorkspace</code> used in the discrimination process.
    * @return A <code>List</code> of <code>Lists</code> containing all
    *    equivalence classes.  Each equivalence class list contains all values
    *    that compare equal to each other.
    */
   public List<List<T>> discriminate(List<T> elements, MsdWorkspace workspace);

   /**
    * Partitions the given <code>List</code> of elements into another
    * <code>List</code>, in which all of the elements from the given list exist
    * in the new list, and all elements that compare equal are adjacent to each
    * other, according to the given <code>Extractor</code>.
    * @param elements A <code>List</code> of elements.
    * @param extractor An <code>Extractor</code> that gives <em>labels</em> for
    *    each element.
    * @param workspace The <code>MsdWorkspace</code> used in the discrimination process.
    * @return A <code>List</code> of <code>Lists</code> containing all
    *    equivalence classes.  Each equivalence class list contains all
    *    elements that compare equal to each other.
    * @param <E> The type of element that is being discriminated by the type
    *    &lt;E&gt;
    */
   public <E> List<List<E>> discriminate(List<E> elements, Extractor<E, T> extractor, MsdWorkspace workspace);
}
