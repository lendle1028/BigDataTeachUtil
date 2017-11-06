package net.sf.jagg.msd;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.RandomAccess;

/**
 * The <code>Discriminators</code> utility class supplies utility functionality
 * for <code>Discriminators</code>.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class Discriminators
{
   /**
    * Do not instantiate the <code>Discriminators</code> class.
    */
   private Discriminators() {}

   /**
    * Gets the proper <code>Discriminator</code> for a particular object.
    * @param object The object representing the type to be discriminated.
    * @param <T> The type of the object to be discriminated.
    * @return A <code>Discriminator</code> capable of discriminating objects of
    *    type <code>T</code>, or <code>null</code> if one couldn't be found.
    */
   @SuppressWarnings("unchecked")
   public static <T> Discriminator<T> getDiscriminator(T object)
   {
      Discriminator<T> discriminator;
      // Try for decreasing order of popularity.
      if (object instanceof Integer)
         discriminator = (Discriminator<T>) new IntegerDiscriminator();
      else if (object instanceof Double)
         discriminator = (Discriminator<T>) new DoubleDiscriminator();
      else if (object instanceof CharSequence)
         discriminator = (Discriminator<T>) new CharSequenceDiscriminator();
      else if (object instanceof Enum)
         discriminator = (Discriminator<T>) new EnumDiscriminator();
      else if (object instanceof Long)
         discriminator = (Discriminator<T>) new LongDiscriminator();
      else if (object instanceof Date)
         discriminator = (Discriminator<T>) new DateDiscriminator();
      else if (object instanceof Calendar)
         discriminator = (Discriminator<T>) new CalendarDiscriminator();
      else if (object instanceof Float)
         discriminator = (Discriminator<T>) new FloatDiscriminator();
      else if (object instanceof Short)
         discriminator = (Discriminator<T>) new ShortDiscriminator();
      else if (object instanceof Byte)
         discriminator = (Discriminator<T>) new ByteDiscriminator();
      else if (object instanceof Boolean)
         discriminator = (Discriminator<T>) new BooleanDiscriminator();
      else if (object instanceof Character)
         discriminator = (Discriminator<T>) new CharacterDiscriminator();
      else if (object instanceof BigInteger)
         discriminator = (Discriminator<T>) new BigIntegerDiscriminator();
      else if (object instanceof BigDecimal)
         discriminator = (Discriminator<T>) new BigDecimalDiscriminator();
      else if (object instanceof RandomAccess && object instanceof List)
         discriminator = (Discriminator<T>) new RandomAccessListDiscriminator();
      else if (object instanceof Collection)
         discriminator = (Discriminator<T>) new CollectionDiscriminator();
      else if (object instanceof Class)
         discriminator = new ClassDiscriminator<T>();
      else if (object instanceof Object[])
         discriminator = (Discriminator<T>) new ArrayDiscriminator<T>();
      else if (object instanceof Discriminable)
         discriminator = new DiscriminableDiscriminator<T>();
      else
         return null;

      return new NullDiscriminator<T>(discriminator);
   }

   /**
    * Gets the proper <code>Discriminator</code> for a particular object's
    * class.
    * @param c The <code>Class</code> object representing the type to be
    *    discriminated.
    * @param <T> The type of the object to be discriminated.
    * @return A <code>Discriminator</code> capable of discriminating objects of
    *    type <code>T</code>, or <code>null</code> if one couldn't be found.
    */
   @SuppressWarnings("unchecked")
   public static <T> Discriminator<T> getDiscriminator(Class<T> c)
   {
      Discriminator<T> discriminator;
      // Try for decreasing order of popularity.
      if (Integer.class == c || Integer.TYPE == c)
         discriminator = (Discriminator<T>) new IntegerDiscriminator();
      else if (Double.class == c || Double.TYPE == c)
         discriminator = (Discriminator<T>) new DoubleDiscriminator();
      else if (CharSequence.class.isAssignableFrom(c))
         discriminator = (Discriminator<T>) new CharSequenceDiscriminator();
      else if (Enum.class.isAssignableFrom(c))
         discriminator = (Discriminator<T>) new EnumDiscriminator();
      else if (Long.class == c || Long.TYPE == c)
         discriminator = (Discriminator<T>) new LongDiscriminator();
      else if (Date.class.isAssignableFrom(c))
         discriminator = (Discriminator<T>) new DateDiscriminator();
      else if (Calendar.class.isAssignableFrom(c))
         discriminator = (Discriminator<T>) new CalendarDiscriminator();
      else if (Float.class == c || Float.TYPE == c)
         discriminator = (Discriminator<T>) new FloatDiscriminator();
      else if (Short.class == c || Short.TYPE == c)
         discriminator = (Discriminator<T>) new ShortDiscriminator();
      else if (Byte.class == c || Byte.TYPE == c)
         discriminator = (Discriminator<T>) new ByteDiscriminator();
      else if (Boolean.class == c || Boolean.TYPE == c)
         discriminator = (Discriminator<T>) new BooleanDiscriminator();
      else if (Character.class == c || Character.TYPE == c)
         discriminator = (Discriminator<T>) new CharacterDiscriminator();
      else if (BigInteger.class == c)
         discriminator = (Discriminator<T>) new BigIntegerDiscriminator();
      else if (BigDecimal.class == c)
         discriminator = (Discriminator<T>) new BigDecimalDiscriminator();
      else if (RandomAccess.class.isAssignableFrom(c) && List.class.isAssignableFrom(c))
         discriminator = (Discriminator<T>) new RandomAccessListDiscriminator();
      else if (Collection.class.isAssignableFrom(c))
         discriminator = (Discriminator<T>) new CollectionDiscriminator();
      else if (Class.class.isAssignableFrom(c))
         discriminator = new ClassDiscriminator<T>();
      else if (Object[].class.isAssignableFrom(c))
         discriminator = (Discriminator<T>) new ArrayDiscriminator<T>();
      else if (Discriminable.class.isAssignableFrom(c))
         discriminator = new DiscriminableDiscriminator<T>();
      else
         return null;

      return new NullDiscriminator<T>(discriminator);
   }

   /**
    * "Flattens" a <code>List</code> of <code>Lists</code> into a new
    * <code>List</code> of T objects.
    * @param listOfLists A <code>List</code> of <code>Lists</code>.
    * @param <T> The type of object.
    * @return A flattened <code>List</code> that contains only the elements
    *    of each of the internal lists, in the order found.
    */
   @SuppressWarnings("ForLoopReplaceableByForEach")
   public static <T> List<T> getFlattenedList(List<List<T>> listOfLists)
   {
      // Get size first to avoid ArrayList re-allocation of the internal array.
      int size = 0;
      int numLists = listOfLists.size();
      for (int i = 0; i < numLists; i++)
         size += listOfLists.get(i).size();
      List<T> flatList = new ArrayList<T>(size);
      // Avoid Iterator creation overhead. 
      for (int i = 0; i < numLists; i++)
      {
         List<T> internalList = listOfLists.get(i);
         for (int j = 0; j < internalList.size(); j++)
            flatList.add(internalList.get(j));
      }
      return flatList;
   }
}
