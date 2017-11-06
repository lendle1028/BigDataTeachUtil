package net.sf.jagg.util;

import java.util.Comparator;
import java.util.List;

import net.sf.jagg.exception.ExpectedComparableException;
import net.sf.jagg.model.AggregateValue;

/**
 * This class represents a <code>Comparator</code> that is capable of comparing
 * two objects based on a dynamic list of properties of the objects
 * of type <code>T</code>.  These objects are wrapped in
 * <code>AggregateValues</code>.  This class is used internally by
 * <code>Aggregation</code>  objects to sort a
 * <code>List&lt;AggregateValue&lt;T&gt;&gt;</code> according to a specified
 * list of properties.
 *
 * @author Randy Gettman
 * @since 0.7.0
 */
public class AggregateValuePropertiesComparator<T> implements Comparator<AggregateValue<T>>
{
   private List<String> myProperties;
   private int mySize;

   /**
    * Construct an <code>AggregateValuePropertiesComparator</code> that pays
    * attention to the given <code>List</code> of properties in an
    * <code>AggregateValue&lt;T&gt;</code>. All properties must be
    * <code>Comparable</code>.
    *
    * @param properties A <code>List&lt;String&gt;</code> of properties.
    */
   public AggregateValuePropertiesComparator(List<String> properties)
   {
      myProperties = properties;
      mySize = properties.size();
   }

   /**
    * <p>Compares the given objects to determine order.  Fulfills the
    * <code>Comparator</code> contract by returning a negative integer, 0, or a
    * positive integer if <code>o1</code> is less than, equal to, or greater
    * than <code>o2</code>.</p>
    * <p>Null property values compare equal to each other, and a null property
    * compares greater than a non-null property</p>.
    *
    * @param av1 The left-hand-side object to compare.
    * @param av2 The right-hand-side object to compare.
    * @return A negative integer, 0, or a positive integer if <code>o1</code>
    *    is less than, equal to, or greater than <code>o2</code>.
    * @throws ExpectedComparableException If any property specified in the
    *    constructor doesn't correspond to a no-argument "get&lt;Property&gt;"
    *    getter method in <code>T</code>, or if the property's type is not
    *    <code>Comparable</code>.
    */
   @SuppressWarnings("unchecked")
   public int compare(AggregateValue<T> av1, AggregateValue<T> av2) throws ExpectedComparableException
   {
      int comp;
      for (int i = 0; i < mySize; i++)
      {
         String property = myProperties.get(i);
         Comparable value1 = (Comparable) av1.getPropertyValue(property);
         Comparable value2 = (Comparable) av2.getPropertyValue(property);
         try
         {
            if (value1 == null)
            {
               if (value2 == null)
                  comp = 0;
               else
                  comp = 1;
            }
            else
            {
               if (value2 == null)
                  comp = -1;
               else
                  comp = value1.compareTo(value2);
            }
            if (comp != 0) return comp;
         }
         catch (ClassCastException e)
         {
            throw new ExpectedComparableException("Property \"" + property + "\" needs to be Comparable.", e);
         }
      }
      return 0;
   }

   /**
    * Indicates whether the given
    * <code>AggregateValuePropertiesComparator</code> is equal to this
    * <code>AggregateValuePropertiesComparator</code>.  All property names must
    * match in order.
    *
    * @param obj The other <code>AggregateValuePropertiesComparator</code>.
    */
   public boolean equals(Object obj)
   {
      if (obj instanceof AggregateValuePropertiesComparator)
      {
         AggregateValuePropertiesComparator otherComp = (AggregateValuePropertiesComparator) obj;
         if (mySize != otherComp.mySize)
            return false;
         for (int i = 0; i < mySize; i++)
         {
            if (!myProperties.get(i).equals(otherComp.myProperties.get(i)))
               return false;
         }
         return true;
      }
      return false;
   }
}