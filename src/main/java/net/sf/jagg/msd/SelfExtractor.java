package net.sf.jagg.msd;

/**
 * A <code>SelfExtractor</code> returns the element itself as the label.
 *
 * @param <E> The element type whose label is itself.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class SelfExtractor<E> implements Extractor<E, E>
{
   /**
    * Returns the element as its own label.
    * @param element The element.
    * @return The element as the label.
    */
   public E getLabel(E element)
   {
      return element;
   }

   /**
    * The <code>SelfExtractor</code> is never "complete", i.e., we would never
    * want to prevent the calling of <code>getLabel</code> because that method
    * will always succeed.
    * @param element The element.
    * @return <code>false</code>.
    */
   public boolean isComplete(E element)
   {
      return false;
   }

   /**
    * The <code>SelfExtractor</code> is never "all complete", i.e., there are
    * no <code>Discriminators</code> up the chain to notify that discrimination
    * is complete.
    * @param allComplete Whether all elements were complete.
    */
   public void setAllComplete(boolean allComplete)
   {
   }

   /**
    * The <code>SelfExtractor</code> is never "all complete", i.e., there are
    * no <code>Discriminators</code> up the chain to notify that discrimination
    * is complete.
    * @return <code>false</code>.
    */
   public boolean isAllComplete()
   {
      return false;
   }
}
