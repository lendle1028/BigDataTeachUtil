package net.sf.jagg.msd;

import java.util.List;

/**
 * A <code>CharacterDiscriminator</code> discriminates <code>Lists</code> of
 * <code>Characters</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class CharacterDiscriminator extends PortionDiscriminator<Character>
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
   public <E> List<List<E>> discriminate(List<E> elements, Extractor<E, Character> extractor, MsdWorkspace workspace)
   {
      CharExtractor<E> ce = getPortionExtractor(extractor);
      return discriminatePortion(elements, ce, workspace);
   }

   /**
    * Returns a portion extractor appropriate for <code>Characters</code>.
    * @param e An <code>Extractor</code> that returns appropriate labels.
    * @return An appropriate <code>PortionExtractor</code>.
    */
   protected <E> CharExtractor<E> getPortionExtractor(Extractor<E, Character> e)
   {
      return new CharExtractor<E>(e);
   }

   /**
    * A <code>CharExtractor</code> extracts integer labels by converting the
    * character into an integer.  It does not use the index.
    */
   protected class CharExtractor<E> extends PortionExtractor<E, Character>
   {
      /**
       * Create a <code>CharExtractor</code> that first uses the given
       * <code>Extractor</code> to get the value.
       * @param extractor Another <code>Extractor</code>.
       */
      public CharExtractor(Extractor<E, Character> extractor)
      {
         super(extractor);
      }

      /**
       * Create an integer label out of the extractor's label.
       * @param element The element.
       * @return An integer label.
       */
      public int getLabel(E element)
      {
         return (int) myExtractor.getLabel(element).charValue();
      }

      /**
       * Determines whether discrimination is complete for the given element, at
       * the given index.
       * @param element The element.
       * @return <code>true</code> if discrimination is complete or cannot
       *    continue, usually because the discrimination has run off the end of
       *    the label, <code>false</code> otherwise.
       */
      public boolean isComplete(E element)
      {
         // The index is not an issue here.
         return myExtractor.isComplete(element);
      }
   }
}
