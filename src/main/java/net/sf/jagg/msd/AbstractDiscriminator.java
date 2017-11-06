package net.sf.jagg.msd;

import java.util.List;

/**
 * An <code>AbstractDiscriminator</code> is an abstract
 * <code>Discriminator</code> that implements the <code>discriminate</code>
 * method that doesn't take an <code>Extractor</code> by calling the
 * <code>discriminate</code> method that does take an <code>Extractor</code>
 * and supplying a <code>SelfExtractor</code>.
 * The other <code>discriminate</code> method that does take an
 * <code>Extractor</code> is left unimplemented.
 *
 * @author Randy Gettman
 * @since 0.5.0
 * @see SelfExtractor
 */
public abstract class AbstractDiscriminator<T> implements Discriminator<T>
{
   /**
    * Defers to the <code>discriminate</code> method that takes an
    * <code>Extractor</code> by supplying it a <code>SelfExtractor</code>.
    * @param elements A <code>List</code> of elements.
    * @param workspace The <code>MsdWorkspace</code> used in the discrimination process.
    * @return A <code>List</code> of <code>Lists</code> containing all
    *    equivalence classes.  Each equivalence class list contains all
    *    elements that compare equal to each other.
    */
   public List<List<T>> discriminate(List<T> elements, MsdWorkspace workspace)
   {
      return discriminate(elements, new SelfExtractor<T>(), workspace);
   }
}
