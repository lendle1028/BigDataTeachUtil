package net.sf.jagg.util;

import java.util.Comparator;

/**
 * This adapter class compares <code>Comparables</code>.  It compares its
 * objects exactly like <code>T</code>'s <code>compareTo</code> method (which
 * exists because <code>T</code> is <code>Comparable</code>).
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class ComparableComparator<T extends Comparable<? super T>> implements Comparator<T>
{
   /**
    * <p>Compares the given objects to determine order.  Fulfills the
    * <code>Comparator</code> contract by returning a negative integer, 0, or a
    * positive integer if <code>o1</code> is less than, equal to, or greater
    * than <code>o2</code>.</p>
    * <p>Nulls compare equal to each other, and a null compares greater than
    * non-nulls</code>.
    *
    * @param o1 The left-hand-side object to compare.
    * @param o2 The right-hand-side object to compare.
    * @return A negative integer, 0, or a positive integer if <code>o1</code>
    *    is less than, equal to, or greater than <code>o2</code>.
    */
   public int compare(T o1, T o2)
   {
      if (o1 == null)
      {
         if (o2 == null)
            return 0;
         else
            return 1;
      }
      else if (o2 == null)
         return -1;
      return o1.compareTo(o2);
   }
}
