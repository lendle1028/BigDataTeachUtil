package net.sf.jagg.model;

import java.util.List;

/**
 * An <code>OrderByClause</code> represents how to order data for analytic
 * function processing within a partition.  It works like an SQL "order by"
 * clause.  This only affects the order of processing within a partition; it
 * does not affect the order of the results.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class OrderByClause
{
   private List<OrderByElement> myOrderByElements;

   /**
    * Constructs an <code>OrderByClause</code> that consists of a
    * <code>List</code> of <code>OrderByElements</code>.
    * @param elements A <code>List</code> of <code>OrderByElements</code>.
    */
   public OrderByClause(List<OrderByElement> elements)
   {
      myOrderByElements = elements;
   }

   /**
    * Returns the <code>List</code> of <code>OrderByElements</code>.
    * @return The <code>List</code> of <code>OrderByElements</code>.
    */
   public List<OrderByElement> getElements()
   {
      return myOrderByElements;
   }

   /**
    * For equality, all elements must match, including the count of
    * elements.
    * @param obj Another object, hopefully an <code>OrderByClause</code>.
    * @return <code>true</code> if the elements are equal in number and to
    *    themselves, in order, else <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      if (obj != null && obj instanceof OrderByClause)
      {
         OrderByClause pc = (OrderByClause) obj;
         if (myOrderByElements.size() != pc.myOrderByElements.size())
            return false;
         for (int i = 0; i < myOrderByElements.size(); i++)
         {
            if (!myOrderByElements.get(i).equals(pc.myOrderByElements.get(i)))
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
      int hc = 0;
      for (OrderByElement element : myOrderByElements)
      {
         hc = 31 * hc + element.hashCode();
      }
      return hc;
   }

   /**
    * Returns the string representation.
    * @return The string representation.
    */
   public String toString()
   {
      StringBuilder buf = new StringBuilder();
      buf.append("OrderByClause(");
      for (int i = 0; i < myOrderByElements.size(); i++)
      {
         OrderByElement element = myOrderByElements.get(i);
         if (i != 0)
            buf.append(", ");
         buf.append(element);
      }
      buf.append(")");
      return buf.toString();
   }
}
