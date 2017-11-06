package net.sf.jagg.msd;

import java.util.ArrayList;
import java.util.List;

import net.sf.jagg.model.ChainedMethodCall;
import net.sf.jagg.util.MethodCache;

/**
 * A <code>PropertiesDiscriminator</code> discriminates <code>Lists</code> of
 * <code>Objects</code> by their properties.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class PropertiesDiscriminator<T> extends ChainedDiscriminator<T>
{
   /**
    * A cache of <code>ChainedMethodCalls</code>.
    */
   private List<ChainedMethodCall> myMethodCalls;
   /**
    * An array of property strings.
    */
   private String[] myProperties;

   /**
    * Creates a <code>PropertiesDiscriminator</code> that discriminates on
    * the given properties of a list of elements.
    * @param properties An array of properties on which to discriminate.
    */
   public PropertiesDiscriminator(String... properties)
   {
      myProperties = properties;
      int numProperties = myProperties.length;
      myMethodCalls = new ArrayList<ChainedMethodCall>(numProperties);
   }

   /**
    * Creates a <code>PropertiesDiscriminator</code> that discriminates on
    * the given properties of a list of elements.
    * @param properties A <code>List</code> of properties on which to
    *    discriminate.
    */
   public PropertiesDiscriminator(List<String> properties)
   {
      myProperties = new String[properties.size()];
      if (!properties.isEmpty())
         properties.toArray(myProperties);
      int numProperties = myProperties.length;
      myMethodCalls = new ArrayList<ChainedMethodCall>(numProperties);
   }

   /**
    * Returns an appropriate <code>ChainedExtractor</code>.
    * @param extractor A <code>ChainedExtractor</code> that returns appropriate
    *    labels.
    * @return An appropriate <code>ChainedExtractor</code>.
    */
   @SuppressWarnings({"unchecked", "ForLoopReplaceableByForEach"})
   protected <E> ChainedExtractor<E, ?, T> getChainedExtractor(List<E> elements, Extractor<E, T> extractor)
   {
      // Get all MethodCalls here.
      T obj = extractor.getLabel(elements.get(0));
      MethodCache cache = MethodCache.getMethodCache();
      for (int i = 0; i < myProperties.length; i++)
      {
         String property = myProperties[i];
         myMethodCalls.add(cache.getMethodCallFromProperty(obj, property));
      }
      return new MethodCallChainedExtractor(extractor);
   }

   /**
    * Returns the <code>Discriminator</code> that discriminates on a specific
    * property, indexed by the given index.  If it is known that no more loops
    * are necessary, then the returned <code>Discriminator</code> may be
    * <code>null</code>.
    * @param elements The list of elements.
    * @param extractor The <code>ChainedExtractor</code> that was obtained from
    *    <code>getChainedExtractor</code>.
    * @param index The index of the loop.
    * @return A <code>Discriminator</code> that discriminates on a specific
    *    property's type.
    */
   protected <E> Discriminator<?> getDiscriminator(List<E> elements, ChainedExtractor<E, ?, T> extractor,
      int index)
   {
      if (index < myProperties.length)
      {
         ChainedMethodCall mc = myMethodCalls.get(index);
         Class<?> returnType = mc.getChainedReturnType();
         return Discriminators.getDiscriminator(returnType);
      }
      return new NullDiscriminator<T>(null);
   }

   /**
    * An <code>MethodCallChainedExtractor</code> extracts results of a method
    * call as labels.
    * @param <E> The type of element.
    * @param <L> The type of label.
    * @param <B> The base type of the object.
    */
   protected class MethodCallChainedExtractor<E, L, B> extends ChainedExtractor<E, L, B>
   {
      /**
       * Create an <code>MethodCallChainedExtractor</code> that uses the given
       * <code>Extractor</code> to retrieve the base item.
       * @param extractor An <code>Extractor</code>.
       */
      public MethodCallChainedExtractor(Extractor<E, B> extractor)
      {
         super(extractor);
      }

      /**
       * The label is the result of a <code>MethodCall</code> on the base
       * object type.
       * @param element The element.
       * @return The result of a <code>MethodCall</code> on the base object
       *    type.
       */
      @SuppressWarnings("unchecked")
      public L getLabel(E element)
      {
         B obj = myExtractor.getLabel(element);
         ChainedMethodCall mc = myMethodCalls.get(myIndex);
         return (L) mc.invoke(obj);
      }

      /**
       * The discrimination is complete after all properties have been used.
       * @param element The element.
       * @return <code>true</code> if complete,
       *    <code>false</code> otherwise.
       */
      public boolean isComplete(E element)
      {
         return myExtractor.isComplete(element) || myIndex >= myProperties.length;
      }
   }
}
