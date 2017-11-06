package net.sf.jagg.msd;

/**
 * An <code>Extractor</code> takes an element (or part of an element) from an
 * object and returns it as a <em>label</em>.  The <em>label</em> is the value
 * that is actually discriminated.  Examples of labels include one character in
 * a String, or the high-order 16 bits of an integer.
 *
 * @param <E> The element type that has labels that can be extracted.
 * @param <L> The type of the label that this <code>Extractor</code> extracts
 *    out of elements of type <code>E</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public interface Extractor<E, L>
{
   /**
    * Returns the <em>label</em> for a given element.
    * @param element The element.
    * @return The label.
    */
   public L getLabel(E element);

   /**
    * Determines whether discrimination is complete for the given element, at
    * the given index.
    * @param element The element.
    * @return <code>true</code> if discrimination is complete or cannot
    *    continue, usually because the discrimination has run off the end of
    *    the label, <code>false</code> otherwise.
    */
   public boolean isComplete(E element);

   /**
    * The <code>Discriminator</code> calls this method to indicate whether all
    * elements in its current equivalence class were complete.
    * @param allComplete Whether all elements were complete.
    */
   public void setAllComplete(boolean allComplete);

   /**
    * The specific <code>Discriminator</code> calls this method to determine
    * whether all elements in the current equivalence class were complete.
    * @return Whether all elements were complete.
    */
   public boolean isAllComplete();
}
