package net.sf.jagg.util;

import java.util.Comparator;
import java.util.List;

import net.sf.jagg.model.AnalyticValue;
import net.sf.jagg.util.PropertiesComparator;

/**
 * This class represents a <code>Comparator</code> that is capable of
 * comparing two objects based on a dynamic list of properties of the objects
 * of type <code>T</code>.  These properties are on objects that are embedded
 * in <code>AnalyticValues</code>. This class is used internally by
 * <code>Analytic</code> to sort a <code>List&lt;AnalyticValue&lt;T&gt;&gt;</code>
 * according to a specified list of properties.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class AnalyticValuePropertiesComparator<T> implements Comparator<AnalyticValue<T>>
{
   private PropertiesComparator<T> myPropertiesComparator;

   /**
    * Construct a <code>AnalyticValuePropertiesComparator</code> that pays
    * attention to the given <code>List</code> of properties in the generic
    * type <code>T</code>.  All properties must be <code>Comparable</code>.
    *
    * @param properties A <code>List&lt;String&gt;</code> of properties.
    */
   public AnalyticValuePropertiesComparator(List<String> properties)
   {
      myPropertiesComparator = new PropertiesComparator<T>(properties);
   }

   /**
    * <p>Compares the given <code>AnalyticValues</code> to determine order.
    * Extracts the actual <code>T</code> objects from the <code>AnalyticValues</code>
    * and then compares them as a <code>PropertiesComparator</code> would.
    * Fulfills the <code>Comparator</code> contract by returning a negative
    * integer, 0, or a positive integer if <code>o1</code> is less than, equal
    * to, or greater than <code>o2</code>.</p>
    * <p>Nulls properties compare equal to each other, and a null property
    * compares greater than a non-null property</code>.</p>
    *
    * @param o1 The left-hand-side object to compare.
    * @param o2 The right-hand-side object to compare.
    * @return A negative integer, 0, or a positive integer if <code>o1</code>
    *    is less than, equal to, or greater than <code>o2</code>.
    * @throws net.sf.jagg.exception.ExpectedComparableException If the
    *    property's type is not <code>Comparable</code>.
    * @throws net.sf.jagg.exception.PropertyAccessException If there was a
    *    problem accessing the property value on the data.
    */
   @SuppressWarnings("unchecked")
   public int compare(AnalyticValue<T> o1, AnalyticValue<T> o2)
   {
      return myPropertiesComparator.compare(o1.getObject(), o2.getObject());
   }
}
