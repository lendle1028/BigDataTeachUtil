package net.sf.jagg.model;

/**
 * An <code>OrderByElement</code> is one element of an <code>OrderByClause</code>,
 * containing a property string, the sort direction, and the nulls first or last
 * part.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class OrderByElement
{
   /**
    * Controls the direction of the sort for this property.
    */
   public enum SortDir
   {
      /**
       * Sort the property ascending.
       */
      ASC,
      /**
       * Sort the property descending.
       */
      DESC
   }

   /**
    * Controls whether to sort nulls first or last.
    */
   public enum NullSort
   {
      /**
       * Sort nulls first.
       */
      FIRST,
      /**
       * Sort nulls last.
       */
      LAST
   }

   private String myProperty;
   private SortDir mySortDir;
   private NullSort myNullSort;

   /**
    * Order by the given property, defaulting to ascending and nulls last.
    * @param property The property.
    */
   public OrderByElement(String property)
   {
      this(property, SortDir.ASC, NullSort.LAST);
   }

   /**
    * Order by the given property and the given sort order.  Ascending means
    * nulls last, descending means nulls first.
    * @param property The given property.
    * @param sortDir A <code>SortDir</code> value.
    */
   public OrderByElement(String property, SortDir sortDir)
   {
      this(property, sortDir, (sortDir == SortDir.ASC) ? NullSort.LAST : NullSort.FIRST);
   }

   /**
    * Order by the given property and the given nulls sort order preference,
    * defaulting to an overall ascending sort.
    * @param property The given property.
    * @param nullSort A <code>NullSort</code> value.
    */
   public OrderByElement(String property, NullSort nullSort)
   {
      this(property, SortDir.ASC, nullSort);
   }

   /**
    * Order by the given property, in the given sort direction, with the given
    * nulls sort order preference.
    * @param property The given property.
    * @param sortDir A <code>SortDir</code> value.
    * @param nullSort A <code>NullSort</code> value.
    */
   public OrderByElement(String property, SortDir sortDir, NullSort nullSort)
   {
      myProperty = property;
      mySortDir = sortDir;
      myNullSort = nullSort;
   }

   /**
    * Returns the property string.
    * @return The property string.
    */
   public String getProperty()
   {
      return myProperty;
   }

   /**
    * Returns the sort direction as a <code>SortDir</code>.
    * @return The sort direction as a <code>SortDir</code>.
    */
   public SortDir getSortDir()
   {
      return mySortDir;
   }

   /**
    * Returns the null sort order preference as a <code>NullSort</code>.
    * @return The null sort order preference as a <code>NullSort</code>.
    */
   public NullSort getNullSort()
   {
      return myNullSort;
   }

   /**
    * Determines whether the given object is an <code>OrderByElement</code>
    * and if the property string, sort direction, and null sort preference
    * match.
    * @param obj The other object, hopefully another <code>OrderByElement</code>.
    * @return <code>true</code> if the property strings, the sort direction,
    *    and the null sort preference match, else <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      if (obj != null && obj instanceof OrderByElement)
      {
         OrderByElement obe = (OrderByElement) obj;
         return (myProperty.equals(obe.myProperty) &&
             mySortDir == obe.mySortDir &&
             myNullSort == obe.myNullSort);
      }
      return false;
   }

   /**
    * Returns the hash code.
    * @return The hash code.
    */
   public int hashCode()
   {
      int hc = myProperty.hashCode();
      hc += 31 * hc + mySortDir.hashCode();
      hc += 31 * hc + myNullSort.hashCode();
      return hc;
   }

   /**
    * Returns the string representation.
    * @return The string representation.
    */
   public String toString()
   {
      StringBuilder buf = new StringBuilder();
      buf.append(myProperty);
      buf.append(" ");
      buf.append(mySortDir);
      buf.append(" ");
      buf.append(myNullSort);
      return buf.toString();
   }
}
