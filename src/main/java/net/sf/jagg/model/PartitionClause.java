package net.sf.jagg.model;

import java.util.List;

/**
 * A <code>PartitionClause</code> represents how to partition data for
 * analytic function processing.  It works like an SQL "group by" clause.  No
 * data in one partition will affect the processing of another partition.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class PartitionClause
{
   private List<String> myProperties;

   /**
    * Constructs a <code>PartitionClause</code> that consists of a
    * <code>List</code> of string properties.
    * @param properties A <code>List</code> of string properties.
    */
   public PartitionClause(List<String> properties)
   {
      myProperties = properties;
   }

   /**
    * Returns the <code>List</code> of string properties.
    * @return The <code>List</code> of string properties.
    */
   public List<String> getProperties()
   {
      return myProperties;
   }

   /**
    * For equality, all properties must match, including the count of
    * properties.
    * @param obj Another object, hopefully a <code>PartitionClause</code>.
    * @return <code>true</code> if the properties are equal in number and to
    *    themselves, in order, else <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      if (obj != null && obj instanceof PartitionClause)
      {
         PartitionClause pc = (PartitionClause) obj;
         if (myProperties.size() != pc.myProperties.size())
            return false;
         for (int i = 0; i < myProperties.size(); i++)
         {
            if (!myProperties.get(i).equals(pc.myProperties.get(i)))
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
      for (String prop : myProperties)
      {
         hc = 31 * hc + prop.hashCode();
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
      buf.append("partitionBy(");
      for (int i = 0; i < myProperties.size(); i++)
      {
         if (i != 0)
            buf.append(", ");
         buf.append(myProperties.get(i));
      }
      buf.append(")");
      return buf.toString();
   }
}
