package net.sf.jagg.util;

import java.util.Map;

import net.sf.jagg.math.DoubleDouble;

/**
 * This utility class contains common code to access a frequency map (a
 * <code>TreeMap&lt;T, Integer&gt;</code>) that jAgg commonly uses in its
 * <code>Aggregators</code>.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class FrequencyMapUtil
{
   private static final boolean DEBUG = false;

   /**
    * Places the given key into the given <code>Map</code> with a frequency of
    * <code>1</code>.  If the key already exists, then the existing frequency
    * value is retrieved and replaced with the incremented value.
    * 
    * @param map A <code>Map</code> of integer frequency values.
    * @param key The key.
    * @param <K> The type of the key.
    * @return <code>true</code> if the map didn't already contain the key.
    */
   public static <K> boolean add(Map<K, Integer> map, K key)
   {
      Integer frequency = map.get(key);
      if (frequency != null)
      {
         map.put(key, frequency + 1);
         return false;
      }
      else
      {
         map.put(key, 1);
         return true;
      }
   }

   /**
    * Removes the given key from the given <code>Map</code> with a frequency of
    * <code>1</code>.  If the key already exists and the frequency is greater
    * than <code>1</code>, then the existing frequency value is replaced with the
    * decremented value.
    *
    * @param map A <code>Map</code> of integer frequency values.
    * @param key The key.
    * @param <K> The type of the key.
    * @return <code>true</code> if the map still contains the key.
    */
   public static <K> boolean remove(Map<K, Integer> map, K key)
   {
      Integer frequency = map.get(key);
      if (frequency != null)
      {
         if (frequency > 1)
         {
            map.put(key, frequency - 1);
            return true;
         }
         else
         {
            map.remove(key);
            return false;
         }
      }
      return false;
   }

   /**
    * Combines the state of the <code>other</code> map into the state of the
    * <code>into</code> map.  The <code>other</code> map isn't changed, but the
    * <code>into</code> map is changed.
    *
    * @param other The "other" map whose state is combined into the "into" map.
    *    This map is not changed.
    * @param into The "into" map whose state is changed when the "other" map's
    *    state is combined into this map.
    * @param <K> The type of the key.
    */
   public static <K> void combine(Map<K, Integer> other, Map<K, Integer> into)
   {
      if (DEBUG)
      {
         System.err.println("other: " + other);
         System.err.println("into : " + into);
      }
      for (K c : other.keySet())
      {
         Integer freq = into.get(c);
         if (freq != null)
         {
            into.put(c, freq + other.get(c));
         }
         else
         {
            into.put(c, other.get(c));
         }
      }
   }

   /**
    * Treats the given map as a list that allows multiple equal elements.
    * Retrieves the key at the given logical "index".  E.g. if the map contains
    * 3 keys "key1" and 2 keys "key2", an index of <code>0-2</code> would
    * return "key1", an index of <code>3-4</code> would return "key2", and any
    * other index would yield an <code>IndexOutOfBoundsException</code>.  Note
    * that this method is <em>O(distinct K)</em>.
    *
    * @param map The frequency map to be treated as a list with multiple equal
    *    elements allowed.
    * @param index The logical "index".
    * @param <K> The type of the key, which must be a <code>Number</code>.
    * @return The key at the specified logical "index", as a
    *    <code>DoubleDouble</code>.
    * @throws IndexOutOfBoundsException If the given logical "index" is
    *    negative or greater than or equal to the number of elements in the
    *    logical list of values.
    */
   public static <K extends Number> DoubleDouble get(Map<K, Integer> map, int index)
   {
      if (index < 0) throw new IndexOutOfBoundsException("Invalid index: " + index);

      int i = 0;
      for (K key : map.keySet())
      {
         int frequency = map.get(key);
         i += frequency;
         if (i > index)
            return new DoubleDouble(key.doubleValue());
      }

      throw new IndexOutOfBoundsException("Index too high: " + index);
   }

   /**
    * Treats the given map as a list that allows multiple equal elements.
    * Retrieves the key at the given logical "index".  A decimal index is
    * handled by performing linear interpolation between the values before and
    * after the decimal index.  E.g. if index "3" is <code>18</code> and index
    * "4" is <code>22</code>, and the logical "index" passed in is
    * <code>3.4</code>, then the resultant value is <code>19.6</code> (40% of
    * the way from <code>18</code> to <code>22).</code>.  This delegates to
    * {@link #get(Map, int)} if <code>index</code> is a mathematical integer.
    *
    * @param map The frequency map to be treated as a list with multiple equal
    *    elements allowed.
    * @param index The logical "index".
    * @param <K> The type of the key, which must be a <code>Number</code>.
    * @return The interpolated value, which may not necessarily exist in the
    *    map, at the specified logical "index", as a <code>DoubleDouble</code>.
    * @throws IndexOutOfBoundsException If the given logical "index" is
    *    negative or greater than the number of elements in the
    *    logical list of values minus one.
    */
   public static <K extends Number> DoubleDouble get(Map<K, Integer> map, double index)
   {
      if (index < 0) throw new IndexOutOfBoundsException("Invalid index: " + index);

      int floor = (int) Math.floor(index);
      int ceiling = (int) Math.ceil(index);

      if (floor == ceiling)
         return get(map, floor);

      int i = 0;
      DoubleDouble low = null;
      DoubleDouble high = null;
      for (K key : map.keySet())
      {
         if (low != null)
         {
            // Case: Low and high are different elements.
            high = new DoubleDouble(key.doubleValue());
            break;
         }
         int frequency = map.get(key);
         i += frequency;
         if (i > index)
         {
            low = new DoubleDouble(key.doubleValue());
            if (i > index + 1)
            {
               // Case: Next is the same value; no interpolation necessary.
               return low;
            }
         }
      }

      if (low == null || high == null)
      {
         // The low may have been in range but the high wasn't.
         throw new IndexOutOfBoundsException("Index too high: " + index);
      }

      // Linear interpolation.
      DoubleDouble temp = new DoubleDouble(index);
      temp.subtractFromSelf(floor);
      temp.multiplySelfBy(high);
      DoubleDouble temp2 = new DoubleDouble(index);
      temp2.negateSelf();
      temp2.addToSelf(ceiling);
      temp2.multiplySelfBy(low);
      temp2.addToSelf(temp);
      return temp2;
   }
}
