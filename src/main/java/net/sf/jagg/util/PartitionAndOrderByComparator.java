package net.sf.jagg.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.sf.jagg.exception.ExpectedComparableException;
import net.sf.jagg.model.AnalyticValue;
import net.sf.jagg.model.OrderByClause;
import net.sf.jagg.model.OrderByElement;
import net.sf.jagg.util.AnalyticValuePropertiesComparator;
import net.sf.jagg.util.PropertiesComparator;
import net.sf.jagg.model.PartitionClause;
import net.sf.jagg.Aggregator;

/**
 * A <code>PartitionAndOrderByComparator</code> is a <code>Comparator</code>
 * that uses a <code>PartitionClause</code> and an <code>OrderByClause</code>
 * to order values.  This class is used internally by <code>Analytic</code>
 * to sort a <code>List&lt;T&gt;</code> according to the list of properties
 * specified by a <code>PartitionClause</code> and the list of
 * <code>OrderByElements</code> specified by a <code>OrderByClause</code>.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class PartitionAndOrderByComparator<T> implements Comparator<AnalyticValue<T>>
{
   private PropertiesComparator<T> myPropertiesComparator;
   private AnalyticValuePropertiesComparator<T> myAnalyticValuePropertiesComparator;
   private List<OrderByElement> myOrderByElements;
   private int myOrderBySize;

   /**
    * Constructs a <code>PartitionAndOrderByComparator</code> with no
    * <code>PartitionClause</code> and no <code>OrderByClause</code>.
    */
   public PartitionAndOrderByComparator()
   {
      this(null, null);
   }

   /**
    * Constructs a <code>PartitionAndOrderByComparator</code> with the given
    * <code>PartitionClause</code> and <code>OrderByClause</code>.
    * @param part A <code>PartitionClause</code>.  A <code>null</code> is
    *    interpreted as an empty clause.
    * @param orderBy An <code>OrderByClause</code>.  A <code>null</code> is
    *    interpreted as an empty clause.
    */
   public PartitionAndOrderByComparator(PartitionClause part, OrderByClause orderBy)
   {
      List<String> properties = (part != null) ? part.getProperties() : new ArrayList<String>(0);
      myPropertiesComparator = new PropertiesComparator<T>(properties);
      myAnalyticValuePropertiesComparator = new AnalyticValuePropertiesComparator<T>(properties);
      if (orderBy != null)
      {
         myOrderByElements = orderBy.getElements();
         myOrderBySize = myOrderByElements.size();
      }
      else
      {
         myOrderByElements = new ArrayList<OrderByElement>(0);
         myOrderBySize = 0;
      }
   }

   /**
    * Returns the <code>AnalyticValuePropertiesComparator</code> that is used
    * to compare <code>AnalyticValues</code> for the purposes of partitioning
    * the list of values according to their objects' properties.
    * @return An <code>AnalyticValuePropertiesComparator</code>.
    */
   public AnalyticValuePropertiesComparator<T> getPartitionComparator()
   {
      return myAnalyticValuePropertiesComparator;
   }

   /**
    * Returns the <code>OrderByElements</code> that this
    * <code>PartitionAndOrderByComparator</code> compares.
    * @return A <code>List</code> of <code>OrderByElements</code>.
    */
   public List<OrderByElement> getOrderByElements()
   {
      return myOrderByElements;
   }

   /**
    * <p>Compares the given objects to determine order.  Fulfills the
    * <code>Comparator</code> contract by returning a negative integer, 0, or a
    * positive integer if <code>o1</code> is less than, equal to, or greater
    * than <code>o2</code>.</p>
    * <p>First, the result of the overridden <code>compare</code> method in
    * <code>PropertiesComparator</code> is determined, using the properties of
    * the <code>PartitionClause</code>.  If it compares equal, then a
    * comparison using the <code>OrderByClause</code> is used.</p>
    *
    * @param o1 The left-hand-side object to compare.
    * @param o2 The right-hand-side object to compare.
    * @return A negative integer, 0, or a positive integer if <code>o1</code>
    *    is less than, equal to, or greater than <code>o2</code>.
    * @throws ExpectedComparableException If any property specified in the
    *    constructor doesn't correspond to a no-argument "get&lt;Property&gt;"
    *    getter method in <code>T</code>, or if the property's type is not
    *    <code>Comparable</code>.
    */
   @SuppressWarnings("unchecked")
   public int compare(AnalyticValue<T> o1, AnalyticValue<T> o2) throws UnsupportedOperationException
   {
      T t1 = o1.getObject();
      T t2 = o2.getObject();
      int comp = myPropertiesComparator.compare(t1, t2);
      if (comp != 0) return comp;

      for (int i = 0; i < myOrderBySize; i++)
      {
         OrderByElement element = myOrderByElements.get(i);
         String property = element.getProperty();
         Comparable value1 = (Comparable)
            Aggregator.getValueFromProperty(t1, property);
         Comparable value2 = (Comparable)
            Aggregator.getValueFromProperty(t2, property);
         try
         {
            if (value1 == null)
            {
               if (value2 == null)
                  comp = 0;
               else
                  comp = element.getNullSort() == OrderByElement.NullSort.FIRST ? -1 : 1;
            }
            else
            {
               if (value2 == null)
                  comp = element.getNullSort() == OrderByElement.NullSort.FIRST ? 1 : -1;
               else
                  comp = element.getSortDir() == OrderByElement.SortDir.ASC ? value1.compareTo(value2) : value2.compareTo(value1);
            }
            if (comp != 0) return comp;
         }
         catch (ClassCastException e)
         {
            throw new ExpectedComparableException("Property \"" + property + "\" needs to be Comparable.");
         }
      }
      return 0;
   }

   /**
    * Indicates whether the given <code>PartitionAndOrderByComparator</code> is
    * equal to this <code>PartitionAndOrderByComparator</code>.  All property
    * names from the <code>PartitionClause</code> must match in order, and all
    * <code>OrderByElements</code> from the <code>OrderByClause</code> must
    * match in order.
    *
    * @param obj The other <code>PartitionAndOrderByComparator</code>.
    */
   public boolean equals(Object obj)
   {
      if (obj instanceof PartitionAndOrderByComparator)
      {
         PartitionAndOrderByComparator otherComp = (PartitionAndOrderByComparator) obj;
         if (!myPropertiesComparator.equals(otherComp.myPropertiesComparator))
            return false;
         if (myOrderBySize != otherComp.myOrderBySize)
            return false;
         for (int i = 0; i < myOrderBySize; i++)
         {
            if (!myOrderByElements.get(i).equals(otherComp.myOrderByElements.get(i)))
               return false;
         }
         return true;
      }
      return false;
   }

   /**
    * Returns a hash code.
    * @return A hash code.
    */
   public int hashCode()
   {
      int hc = myPropertiesComparator.hashCode();
      for (OrderByElement element : myOrderByElements)
      {
         hc = 31 * hc + element.hashCode();
      }
      return hc;
   }

   /**
    * <p>Determines whether this <code>PartitionAndOrderByComparator</code>
    * "covers" another one.  This "covers" another if this can be substituted
    * for the other one and the other one's sort order is still intact.  That
    * means that one of the following is true:</p>
    * <ul>
    * <li>This <code>PartitionClause</code> has at least as many properties as
    * the other <code>PartitionClause</code>, the properties in common match in
    * the same order, and the other doesn't have an <code>OrderByClause</code>.</li>
    * <li>This <code>PartitionClause</code> matches the other
    * <code>PartitionClause</code>, and this <code>OrderByClause</code> has at
    * least as many elements as the other <code>OrderByClause</code>, and the
    * elements in common match in the same order.</li>
    * </ul>
    * <p>Note that two <code>PartitionAndOrderByComparators</code> <code>a</code>
    * and <code>b</code> are equal according to {@link #equals}
    * (<code>a.equals(b)</code>) if and only if <code>a.covers(b)</code> is
    * <code>true</code> and <code>b.covers(a)</code> is <code>true</code>.</p>
    *
    * @param other The other <code>PartitionAndOrderByComparator</code>.
    * @return <code>true</code> if this <code>PartitionAndOrderByComparator</code>
    *    "covers" the other <code>PartitionAndOrderByComparator</code>.
    */
   public boolean covers(PartitionAndOrderByComparator<T> other)
   {
      List<String> properties = myPropertiesComparator.getProperties();
      List<String> otherProperties = other.myPropertiesComparator.getProperties();
      List<OrderByElement> elements = getOrderByElements();
      List<OrderByElement> otherElements = other.getOrderByElements();

      if (properties.size() < otherProperties.size())
      {
         return false;
      }
      else //if (properties.size() >= otherProperties.size())
      {
         for (int i = 0; i < otherProperties.size(); i++)
         {
            // This must match all of the other's properties, in order.
            if (!properties.get(i).equals(otherProperties.get(i)))
               return false;
         }

         if (properties.size() > otherProperties.size())
         {
            // The other must not have order by elements, which would necessarily
            // conflict with this due to its extra partitioning.
            if (other.getOrderByElements().size() > 0)
               return false;
         }
         else
         {
            // This match all of the other's order-by elements, in order.
            for (int i = 0; i < otherElements.size(); i++)
            {
               if (!elements.get(i).equals(otherElements.get(i)))
                  return false;
            }
         }
      }
      // Passed all covers tests.
      return true;
   }

   /**
    * Returns the string representation.
    * @return The string representation.
    */
   public String toString()
   {
      StringBuilder buf = new StringBuilder();
      buf.append("PAOBC(");
      buf.append(myPropertiesComparator);
      buf.append(" orderBy(");
      for (int i = 0; i < myOrderByElements.size(); i++)
      {
         if (i != 0)
            buf.append(", ");
         buf.append(myOrderByElements.get(i));
      }
      buf.append("))");
      return buf.toString();
   }
}
