package net.sf.jagg;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.jagg.exception.ParseException;
import net.sf.jagg.exception.AnalyticCreationException;
import net.sf.jagg.util.AnalyticCache;
import net.sf.jagg.model.AnalyticValue;
import net.sf.jagg.model.AnalyticContext;
import net.sf.jagg.model.OrderByClause;
import net.sf.jagg.model.OrderByElement;
import net.sf.jagg.model.PartitionClause;
import net.sf.jagg.model.WindowClause;

/**
 * An <code>AnalyticAggregator</code> is not used directly.  jAgg creates an
 * <code>AnalyticAggregator</code> for every <code>AnalyticFunction</code>.  In
 * addition to delegating analytic operations to an <code>AnalyticFunction</code>,
 * this object also carries partition data, order-by data, and windowing data.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class AnalyticAggregator implements AnalyticFunction
{
   private static final boolean DEBUG = false;

   // Cache AnalyticAggregator objects to save on instantiation/garbage collection
   // costs.  The key is the analytic specification string.
   private static final AnalyticCache myAnalyticCache = new AnalyticCache();

   /**
    * The part of the analytic specification string that indicates the
    * partition-by clause.
    */
   public static final String PARTITION_CLAUSE_IND = "partitionBy";
   /**
    * The part of the analytic specification string that indicates the
    * partition-by clause.
    */
   public static final String ORDER_BY_CLAUSE_IND = "orderBy";
   /**
    * The part of the analytic specification string that indicates the
    * window clause by rows.
    */
   public static final String WINDOW_CLAUSE_IND_ROWS = "rows";
   /**
    * The part of the analytic specification string that indicates the
    * window clause by a range of values.
    */
   public static final String WINDOW_CLAUSE_IND_RANGE = "range";

   /**
    * Sort ascending (default).
    */
   public static final String ASC = "ASC";
   /**
    * Sort descending.
    */
   public static final String DESC = "DESC";
   /**
    * Use this to indicate which sequence nulls should be ordered.
    */
   public static final String NULLS = "NULLS";
   /**
    * Sort nulls first (default if descending order).
    */
   public static final String FIRST = "FIRST";
   /**
    * Sort nulls last (default if ascending order).
    */
   public static final String LAST = "LAST";
   /**
    * The window clause's keyword for indicating a range of values or rows.
    */
   public static final String WINDOW_THROUGH = ",";

   /**
    * Legal expected suffixes:
    * <ul>
    * <li>AnalyticAggregator - So that a subclass of an existing
    * <code>Aggregator</code> can supply the needed analytic functionality.</li>
    * <li>Aggregator - So that existing <code>Aggregators</code> can also
    * exist as <code>AnalyticFunctions</code>.</li>
    * <li>Analytic - So that an <code>AnalyticFunction</code> that isn't
    * valid as an <code>Aggregtor</code> can be created.</li>
    * </ul>
    *
    * Examples:
    * <ul>
    * <li>MaxAnalyticAggregator - It's a subclass of <code>MaxAggregator</code>
    * that contains additional logic to support analytic functionality as an
    * <code>AnalyticFunction</code>.</li>
    * <li>SumAggregator - It's an <code>AggregateFunction</code> that doubles as
    * an <code>AnalyticFunction</code>.</li>
    * <li>LagAnalytic - It's an <code>AnalyticFunction</code> that is not
    * intended for use as an <code>AggregateFunction</code>.</li>
    * </ul>
    */
   private static final List<String> ANALYTIC_SUFFIXES = Arrays.asList("AnalyticAggregator", "Aggregator", "Analytic");

   private AnalyticFunction myAnalyticFunction;
   private PartitionClause myPartition;
   private OrderByClause myOrderBy;
   private WindowClause myWindow;

   /**
    * Private constructor to ensure that the "Builder" pattern is used.
    * @param builder A <code>Builder</code>.
    */
   private AnalyticAggregator(Builder builder)
   {
      if (builder.myAnalyticFunction == null)
         throw new IllegalArgumentException("Analytic function not supplied!");
      myAnalyticFunction = builder.myAnalyticFunction;
      myPartition = builder.myPartition;
      myOrderBy = builder.myOrderBy;
      myWindow = builder.myWindow;
   }

   /**
    * Private copy constructor.  Replicates the internal
    * <code>AnalyticFunction</code>, but copies the references to everything
    * else.
    * @param agg The <code>AnalyticAggregator</code> to copy.
    */
   private AnalyticAggregator(AnalyticAggregator agg)
   {
      myAnalyticFunction = agg.replicate();
      myPartition = agg.myPartition;
      myOrderBy = agg.myOrderBy;
      myWindow = agg.myWindow;
   }

   /**
    * Creates an <code>AnalyticFunction</code> based on an <em>analytic
    * specification string</em>.  Does not mark it as in use.  Does not add it
    * to the internal cache.  This is meant to aid the caller in creating an
    * <code>AnalyticFunction</code> based on the following analytic
    * specification string format:
    * <code>anaName(property/-ies) [partitionBy(property/-ies)]
    * [orderBy([prop [ASC|DESC] [NULLS [FIRST|LAST]] ]*)]
    * [rows|range([start] through [end])]</code>.
    * This assumes that the desired <code>AnalyticFunction</code> has a
    * one-argument constructor with a <code>String</code> argument for its
    * property or properties.
    *
    * @param anaSpec The String specification of an <code>AnalyticFunction</code>.
    * @return An <code>AnalyticAggregator</code> object that encapsulates the
    *    <code>AnalyticFunction</code> and any <code>PartitionClause</code>,
    *    <code>OrderByClause</code>, and <code>WindowClause</code>.
    * @throws ParseException If the analytic specification was mal-
    *    formed.
    */
   public static AnalyticAggregator getAnalytic(String anaSpec)
   {
      int partitionIdx = anaSpec.indexOf(PARTITION_CLAUSE_IND);
      int orderByIdx = anaSpec.indexOf(ORDER_BY_CLAUSE_IND);
      int windowRowsIdx = anaSpec.indexOf(WINDOW_CLAUSE_IND_ROWS);
      int windowRangeIdx = anaSpec.indexOf(WINDOW_CLAUSE_IND_RANGE);

      if (windowRowsIdx != -1 && windowRangeIdx != -1)
      {
         throw new ParseException("Can't specify both " + WINDOW_CLAUSE_IND_ROWS + " and " +
            WINDOW_CLAUSE_IND_RANGE + " in an analytic function.");
      }

      int minAnalyticIdx = -1;
      if (partitionIdx != -1)
      {
         minAnalyticIdx = partitionIdx;
      }
      if (orderByIdx != -1 && (minAnalyticIdx == -1 || orderByIdx < minAnalyticIdx))
      {
         minAnalyticIdx = orderByIdx;
      }
      if (windowRowsIdx != -1 && (minAnalyticIdx == -1 || windowRowsIdx < minAnalyticIdx))
      {
         minAnalyticIdx = windowRowsIdx;
      }
      if (windowRangeIdx != -1 && (minAnalyticIdx == -1 || windowRangeIdx < minAnalyticIdx))
      {
         minAnalyticIdx = windowRangeIdx;
      }

      String aggSpecPart;
      if (minAnalyticIdx != -1)
      {
         aggSpecPart = anaSpec.substring(0, minAnalyticIdx).trim();
      }
      else
      {
         aggSpecPart = anaSpec;
      }

      int leftParenIdx = aggSpecPart.indexOf("(");
      int rightParenIdx = aggSpecPart.lastIndexOf(")");
      if (leftParenIdx == -1 || rightParenIdx == -1 || leftParenIdx > rightParenIdx)
         throw new ParseException("Malformed Analytic specification: " + anaSpec);
      String aggName = aggSpecPart.substring(0, leftParenIdx);

      int dotIndex = aggSpecPart.indexOf(".");
      // Any dot past a "(" isn't a package specifier; it could be part of an argument.
      if (dotIndex == -1 || dotIndex > leftParenIdx)
      {
         aggName = AnalyticFunction.class.getPackage().getName() + "." + aggName;
      }

      if (DEBUG)
      {
         System.out.println("AnaAgg.getAna: aggName = \"" + aggName + "\".");
      }

      AnalyticFunction anaFunc = null;
      // Loop through legal expected suffixes for analytic function classes.
      for (String suffix : ANALYTIC_SUFFIXES)
      {
         String aggNameWithSuffix = aggName;
         if (!aggName.endsWith(suffix))
            aggNameWithSuffix = aggName + suffix;
         String property = aggSpecPart.substring(leftParenIdx + 1, rightParenIdx);

         try
         {
            Class aggClass = Class.forName(aggNameWithSuffix);
            Constructor ctor = aggClass.getConstructor(String.class);
            anaFunc = (AnalyticFunction) ctor.newInstance(property);
            break;
         }
         catch (ClassNotFoundException ignored)
         {
            // Continue on through list of suffixes
         }
         catch (NoSuchMethodException e)
         {
            throw new AnalyticCreationException("Can't find constructor for AnalyticFunction class \"" +
                    aggName + "\" that contains exactly one String parameter.", e);
         }
         catch (InstantiationException e)
         {
            throw new AnalyticCreationException("AnalyticFunction specified is not a concrete class: \"" +
                    aggName + "\".", e);
         }
         catch (IllegalAccessException e)
         {
            throw new AnalyticCreationException("Unable to construct AnalyticFunction \"" +
                    aggName + "\".", e);
         }
         catch (InvocationTargetException e)
         {
            throw new AnalyticCreationException("Exception caught instantiating AnalyticFunction \"" +
                    aggName + "\": " + e.getCause().getClass().getName(), e);
         }
         catch (ClassCastException e)
         {
            throw new AnalyticCreationException("Class found is not an AnalyticFunction: \"" +
                    aggName + "\".", e);
         }
      }

      if (anaFunc == null)
         throw new AnalyticCreationException("Unknown AnalyticFunction class \"" + aggName + "\".");

      Builder builder = new Builder();
      builder.setAnalyticFunction(anaFunc)
          .setPartition(getPartitionClause(anaSpec, partitionIdx, orderByIdx, windowRowsIdx, windowRangeIdx))
          .setOrderBy(getOrderByClause(anaSpec, partitionIdx, orderByIdx, windowRowsIdx, windowRangeIdx))
          .setWindow(getWindowClause(anaSpec, partitionIdx, orderByIdx, windowRowsIdx, windowRangeIdx));

      return builder.build();
   }

   /**
    * Adds the given <code>AnalyticAggregator</code> to an internal cache.  If
    * it's not in use, then it marks it as "in use" and returns it.  Else, it
    * searches the cache for an <code>AnalyticAggregator</code> that matches the
    * given <code>AnalyticAggregator</code> and is not already in use.  If none
    * exist in the cache, then it replicates the given
    * <code>AnalyticAggregator</code>, adds it to the cache, and returns it.
    *
    * @param archetype The <code>AnalyticAggregator</code> whose properties (and
    *    type) need to be matched.
    * @return A matching <code>AnalyticAggregator</code> object.  It could be
    *    <code>archetype</code> itself if it's not already in use, or it could
    *    be <code>null</code> if <code>archetype</code> was null.
    */
   public static AnalyticAggregator getAnalyticAggregator(AnalyticAggregator archetype)
   {
      return myAnalyticCache.getFunction(archetype);
   }

   /**
    * Creates a <code>PartitionClause</code> out of any part of the given
    * analytic specification string that may define a partition-by clause.
    * @param anaSpec The analytic specification string.
    * @param partitionIdx The already calculated index of PARTITION_CLAUSE_IND.
    * @param orderByIdx The already calculated index of ORDER_BY_CLAUSE_IND.
    * @param windowRowsIdx The already calculated index of WINDOW_CLAUSE_IND_ROWS.
    * @param windowRangeIdx The already calculated index of WINDOW_CLAUSE_IND_RANGE.
    * @return A <code>PartitionClause</code>, or <code>null</code> if the
    *    clause is absent.
    * @throws ParseException If the partition-by clause is malformed.
    */
   private static PartitionClause getPartitionClause(String anaSpec, int partitionIdx,
      int orderByIdx, int windowRowsIdx, int windowRangeIdx)
   {
      if (partitionIdx == -1)
         return null;

      int endOfPartitionByClause = -1;
      if (orderByIdx > partitionIdx)
      {
         endOfPartitionByClause = orderByIdx;
      }
      if (windowRowsIdx > partitionIdx && windowRowsIdx < endOfPartitionByClause)
      {
         endOfPartitionByClause = windowRowsIdx;
      }
      if (windowRangeIdx > partitionIdx && windowRangeIdx < endOfPartitionByClause)
      {
         endOfPartitionByClause = windowRangeIdx;
      }

      String partitionByClause;
      if (endOfPartitionByClause != -1)
      {
         partitionByClause = anaSpec.substring(partitionIdx, endOfPartitionByClause).trim();
      }
      else
      {
         partitionByClause = anaSpec.substring(partitionIdx).trim();
      }

      // partitionBy(prop[, prop]*)
      int leftParenIdx = partitionByClause.indexOf("(");
      int rightParenIdx = partitionByClause.lastIndexOf(")");
      if (leftParenIdx == -1 || rightParenIdx == -1 || leftParenIdx > rightParenIdx)
         throw new ParseException("Malformed Analytic " + PARTITION_CLAUSE_IND + " clause: " + anaSpec);
      String inParens = partitionByClause.substring(leftParenIdx + 1, rightParenIdx);
      List<String> propsList = new ArrayList<String>();
      if (inParens.trim().length() > 0)
      {
         String[] properties = inParens.split(",");
         for (String prop : properties)
         {
            propsList.add(prop.trim());
         }
      }
      return new PartitionClause(propsList);
   }

   /**
    * Creates an <code>OrderByClause</code> out of any part of the given
    * analytic specification string that may define an order-by clause.
    * @param anaSpec The analytic specification string.
    * @param partitionIdx The already calculated index of PARTITION_CLAUSE_IND.
    * @param orderByIdx The already calculated index of ORDER_BY_CLAUSE_IND.
    * @param windowRowsIdx The already calculated index of WINDOW_CLAUSE_IND_ROWS.
    * @param windowRangeIdx The already calculated index of WINDOW_CLAUSE_IND_RANGE.
    * @return An <code>OrderByClause</code>, or <code>null</code> if the
    *    clause is absent.
    * @throws ParseException If the order-by clause is malformed.
    */
   private static OrderByClause getOrderByClause(String anaSpec, int partitionIdx,
      int orderByIdx, int windowRowsIdx, int windowRangeIdx)
   {
      if (orderByIdx == -1)
         return null;

      int endOfOrderByClause = -1;
      if (partitionIdx > orderByIdx)
      {
         endOfOrderByClause = partitionIdx;
      }
      if (windowRowsIdx > orderByIdx && (windowRowsIdx < endOfOrderByClause || endOfOrderByClause == -1))
      {
         endOfOrderByClause = windowRowsIdx;
      }
      if (windowRangeIdx > orderByIdx && (windowRangeIdx < endOfOrderByClause || endOfOrderByClause == -1))
      {
         endOfOrderByClause = windowRangeIdx;
      }

      String orderByClause;
      if (endOfOrderByClause != -1)
      {
         orderByClause = anaSpec.substring(orderByIdx, endOfOrderByClause).trim();
      }
      else
      {
         orderByClause = anaSpec.substring(orderByIdx).trim();
      }

      // orderBy(orderByProp[, orderByProp]*)
      // orderByProp: prop [ASC|DESC] [NULLS [FIRST|LAST]]
      int leftParenIdx = orderByClause.indexOf("(");
      int rightParenIdx = orderByClause.lastIndexOf(")");
      if (leftParenIdx == -1 || rightParenIdx == -1 || leftParenIdx > rightParenIdx)
         throw new ParseException("Malformed Analytic " + ORDER_BY_CLAUSE_IND + " clause: " + anaSpec);
      String inParens = orderByClause.substring(leftParenIdx + 1, rightParenIdx);
      List<OrderByElement> elementsList = new ArrayList<OrderByElement>();
      if (inParens.trim().length() > 0)
      {
         String[] orderBySpecs = inParens.split(",");
         for (String orderBySpec : orderBySpecs)
         {
            String[] parts = orderBySpec.trim().split("\\s+");
            String property;
            OrderByElement.SortDir ordering;
            OrderByElement.NullSort nullOrdering;
            if (parts.length > 0 && parts.length < 5)
            {
               property = parts[0];
               ordering = OrderByElement.SortDir.ASC;
               nullOrdering = OrderByElement.NullSort.LAST;

               if (parts.length == 2 || parts.length == 4)
               {
                  // ordering is next.
                  if (ASC.equalsIgnoreCase(parts[1]))
                  {
                     ordering = OrderByElement.SortDir.ASC;
                     nullOrdering = OrderByElement.NullSort.LAST;
                  }
                  else if (DESC.equalsIgnoreCase(parts[1]))
                  {
                     ordering = OrderByElement.SortDir.DESC;
                     nullOrdering = OrderByElement.NullSort.FIRST;
                  }
                  else if (NULLS.equalsIgnoreCase(parts[1]))
                     throw new ParseException("OrderBy: Expected \"" + FIRST + "\" or \"" + LAST +
                         " after " + NULLS + ": " + orderBySpec);
                  else
                     throw new ParseException("OrderBy: Expected \"" + ASC + "\" or \"" + DESC + ": " + orderBySpec);
               }
               if (parts.length == 3 || parts.length == 4)
               {
                  if (!NULLS.equalsIgnoreCase(parts[parts.length - 2]))
                     throw new ParseException("OrderBy: Expected \"" + NULLS + " " + FIRST + "|" + LAST + ": " + orderBySpec);
                  if (LAST.equalsIgnoreCase(parts[parts.length - 1]))
                     nullOrdering = OrderByElement.NullSort.LAST;
                  else if (FIRST.equalsIgnoreCase(parts[parts.length - 1]))
                     nullOrdering = OrderByElement.NullSort.FIRST;
                  else
                     throw new ParseException("OrderBy: Expected \"" + FIRST + "\" or \"" + LAST + ": " + orderBySpec);
               }
            }
            else
            {
               throw new ParseException("OrderBy: Expected \"property\" [" + ASC + "|" + DESC + "] [" + NULLS + " ]" +
                       FIRST + "|" + LAST + ": " + orderBySpec);
            }

            elementsList.add(new OrderByElement(property, ordering, nullOrdering));
         }
      }
      return new OrderByClause(elementsList);
   }

   /**
    * Creates a <code>WindowClause</code> out of any part of the given
    * analytic specification string that may define a window clause.
    * @param anaSpec The analytic specification string.
    * @param partitionIdx The already calculated index of PARTITION_CLAUSE_IND.
    * @param orderByIdx The already calculated index of ORDER_BY_CLAUSE_IND.
    * @param windowRowsIdx The already calculated index of WINDOW_CLAUSE_IND_ROWS.
    * @param windowRangeIdx The already calculated index of WINDOW_CLAUSE_IND_RANGE.
    * @return A <code>WindowClause</code>, or <code>null</code> if the
    *    clause is absent.
    * @throws ParseException If the window clause is malformed.
    */
   private static WindowClause getWindowClause(String anaSpec, int partitionIdx,
      int orderByIdx, int windowRowsIdx, int windowRangeIdx)
   {
      if (windowRowsIdx == -1 && windowRangeIdx == -1)
         return null;
      if (windowRowsIdx != -1 && windowRangeIdx != -1)
         throw new ParseException("Can't have " + WINDOW_CLAUSE_IND_RANGE + " and " +
            WINDOW_CLAUSE_IND_ROWS + " at the same time: " + anaSpec);

      WindowClause.Type windowType;
      int startOfWindowClause;
      int endOfWindowClause;
      if (windowRowsIdx != -1)
      {
         windowType = WindowClause.Type.ROWS;
         startOfWindowClause = windowRowsIdx;
         endOfWindowClause = -1;
         if (orderByIdx > windowRowsIdx)
         {
            endOfWindowClause = orderByIdx;
         }
         if (partitionIdx > windowRowsIdx && partitionIdx < endOfWindowClause)
         {
            endOfWindowClause = partitionIdx;
         }
      }
      else //if (windowRangeIdx != -1)
      {
         windowType = WindowClause.Type.RANGE;
         startOfWindowClause = windowRangeIdx;
         endOfWindowClause = -1;
         if (orderByIdx > windowRangeIdx)
         {
            endOfWindowClause = orderByIdx;
         }
         if (partitionIdx > windowRangeIdx && partitionIdx < endOfWindowClause)
         {
            endOfWindowClause = partitionIdx;
         }
      }

      String windowClause;
      if (endOfWindowClause != -1)
      {
         windowClause = anaSpec.substring(startOfWindowClause, endOfWindowClause).trim();
      }
      else
      {
         windowClause = anaSpec.substring(startOfWindowClause).trim();
      }

      // rows|range([value] through [value])
      int leftParenIdx = windowClause.indexOf("(");
      int rightParenIdx = windowClause.lastIndexOf(")");
      if (leftParenIdx == -1 || rightParenIdx == -1 || leftParenIdx > rightParenIdx)
         throw new ParseException("Malformed Analytic window clause: " + anaSpec);
      String inParens = windowClause.substring(leftParenIdx + 1, rightParenIdx).trim();
      if (inParens.length() == 0)
      {
         return new WindowClause(windowType, null, null);
      }
      else
      {
         try
         {
            int index = inParens.indexOf(WINDOW_THROUGH);
            if (index != -1)
            {
               String before = inParens.substring(0, index).trim();
               String after = inParens.substring(index + 1).trim();
               Double dBefore = (before.equals("") ? null : Double.valueOf(before));
               Double dAfter = (after.equals("") ? null : Double.valueOf(after));
               return new WindowClause(windowType, dBefore, dAfter);
            }
            else if (inParens.trim().equals(""))
            {
               return new WindowClause(windowType, null, null);
            }
         }
         catch (NumberFormatException e)
         {
            throw new ParseException("Window clause arguments must be numbers: " + anaSpec, e);
         }
      }
      // If we got here, then the window clause values were illegal.
      throw new ParseException("Window clause arguments must be ([value] " + WINDOW_THROUGH +
              " [value]) or ():" + anaSpec);
   }

   /**
    * Delegate to the analytic function.
    * @return The property's value.
    */
   public String getProperty()
   {
      return myAnalyticFunction.getProperty();
   }

   /**
    * Delegate to the analytic function.
    */
   public void init()
   {
      myAnalyticFunction.init();
   }

   /**
    * Delegate to the analytic function.
    * @value The value to iterate.
    */
   public void iterate(Object value)
   {
      myAnalyticFunction.iterate(value);
   }

   /**
    * Delegate to the analytic function.
    * @param func The <code>AggregateFunction</code> to merge into this one.
    */
   public void merge(AggregateFunction func)
   {
      myAnalyticFunction.merge(func);
   }

   /**
    * Delegate to the analytic function.
    * @value The value to delete.
    */
   public void delete(Object value)
   {
      myAnalyticFunction.delete(value);
   }

   /**
    * Delegate to the analytic function.
    * @return Whether this <code>AnalyticFunction</code> can operate with a
    *    window clause.
    */
   public boolean takesWindowClause()
   {
      return myAnalyticFunction.takesWindowClause();
   }

   /**
    * Delegate to the analytic function.
    * @return The supplied <code>WindowClause</code>, if any.
    */
   public WindowClause getWindowClause()
   {
      return myAnalyticFunction.getWindowClause();
   }

   /**
    * Delegate to the analytic function.
    * @return A value representing the result of the analytic calculation.
    */
   public Object terminate()
   {
      return myAnalyticFunction.terminate();
   }

   /**
    * Delegate to the analytic function.
    * @param inUse Whether it's in use.
    */
   public void setInUse(boolean inUse)
   {
      myAnalyticFunction.setInUse(inUse);
   }

   /**
    * Delegate to the analytic function.
    * @return Whether it's in use.
    */
   public boolean isInUse()
   {
      return myAnalyticFunction.isInUse();
   }

   /**
    * Returns a copy of this <code>AnalyticAggregator</code>.  The internal
    * <code>AnalyticFunction</code> is replicated, but any
    * <code>PartitionClause</code>, <code>OrderByClause</code>, or
    * <code>WindowClause</code> remains the same object(s).
    * @return A replica of this <code>AnalyticAggregator</code>.
    */
   public AnalyticAggregator replicate()
   {
      return new AnalyticAggregator(this);
   }

   /**
    * Returns the <code>PartitionClause</code>, if any.
    * @return The <code>PartitionClause</code>, if any.
    */
   public PartitionClause getPartition()
   {
      return myPartition;
   }

   /**
    * Returns the <code>OrderByClause</code>, if any.
    * @return The <code>OrderByClause</code>, if any.
    */
   public OrderByClause getOrderBy()
   {
      return myOrderBy;
   }

   /**
    * Returns the <code>WindowClause</code>, if any.
    * @return The <code>WindowClause</code>, if any.
    */
   public WindowClause getWindow()
   {
      return myWindow;
   }

   /**
    * Returns a <code>List</code> of <code>AnalyticAggregators</code> on which
    * this <code>AnalyticAggregator</code> depends, or an empty <code>List</code>
    * if this doesn't depend on any <code>AnalyticFunctions</code>.
    * @return A <code>List</code> of <code>AnalyticAggregators</code>.
    */
   public List<AnalyticAggregator> getDependentAnalyticAggregators()
   {
      List<AnalyticAggregator> dependents = new ArrayList<AnalyticAggregator>();
      if (myAnalyticFunction instanceof DependentAnalyticFunction)
      {
         DependentAnalyticFunction depFunc = (DependentAnalyticFunction) myAnalyticFunction;
         int numFuncs = depFunc.getNumDependentFunctions();
         for (int i = 0; i < numFuncs; i++)
         {
            // Same partition clause and order by clause, but different
            // window clauses.
            AnalyticAggregator depAna = new AnalyticAggregator.Builder()
                    .setAnalyticFunction(depFunc.getAnalyticFunction(i))
                    .setPartition(getPartition())
                    .setOrderBy(getOrderBy())
                    .setWindow(depFunc.getWindowClause(i))
                    .build();
            if (depAna.myAnalyticFunction.getClass().equals(myAnalyticFunction.getClass()))
            {
               throw new ParseException("A DependentAnalyticFunction can't depend on itself: " + myAnalyticFunction);
            }
            dependents.add(depAna);
         }
      }
      return dependents;
   }

   /**
    * Sets the dependency values in the analytic function, if it's a
    * <code>DependentAnalyticFunction</code>.
    * @param anaValue The <code>AnalyticValue</code> containing at least the
    *    values which the analytic function depends, if any.
    * @param context The <code>AnalyticContext</code>, which knows the analytic
    *    indexes on which this depends on, if any.
    */
   public void setValuesForDependentAnalytics(AnalyticValue<?> anaValue, AnalyticContext<?> context)
   {
      if (myAnalyticFunction instanceof DependentAnalyticFunction)
      {
         DependentAnalyticFunction depFunc = (DependentAnalyticFunction) myAnalyticFunction;
         List<Integer> dependencies = context.getDependencies();
         if (dependencies != null)
         {
            for (int i = 0; i < dependencies.size(); i++)
            {
               // Translate from Analytic index (dependencies.get(i))
               // to dependent function index (i).
               depFunc.setValue(i, anaValue.getAnalyzedValue(dependencies.get(i)));
            }
         }
      }
   }

   /**
    * Determines whether the given <code>AnalyticAggregator</code> is
    * equivalent to this <code>AnalyticAggregator</code>.  This is necessary
    * because <code>AnalyticAggregator</code> objects will be stored in a
    * <code>HashMap</code>.
    *
    * @param o Another <code>AnalyticAggregator</code>.
    * @return <code>true</code> if equivalent, <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      return (getClass().equals(o.getClass()) && toString().equals(o.toString()));
   }

   /**
    * Calculates a hash code for this <code>AnalyticAggregator</code>.  This is
    * necessary because <code>AnalyticAggregator</code> objects will be stored
    * in a <code>HashMap</code>.
    *
    * @return The hash code of this <code>AnalyticAggregator</code>.  It is
    *    computed by taking the hash of the result of the <code>toString</code>
    *    method.
    * @see #toString
    */
   public int hashCode()
   {
      return toString().hashCode();
   }

   /**
    * Returns the name of the <code>AnalyticFunction</code> class that this
    * <code>AnalyticAggregator</code> wraps.
    * @return The name of the <code>AnalyticFunction</code> class that this
    * <code>AnalyticAggregator</code> wraps.
    */
   public String getAnalyticFunctionClassName()
   {
      return myAnalyticFunction.getClass().getName();
   }

   /**
    * A String representation of this <code>AnalyticAggregator</code>, in the form
    * "className(property)".
    *
    * @return The String representation.
    */
   public String toString()
   {
      StringBuilder buf = new StringBuilder(myAnalyticFunction.toString());
      if (myPartition != null)
      {
         buf.append(" ");
         buf.append(myPartition.toString());
      }
      if (myOrderBy != null)
      {
         buf.append(" ");
         buf.append(myOrderBy.toString());
      }
      if (myWindow != null)
      {
         buf.append(" ");
         buf.append(myWindow.toString());
      }
      return buf.toString();
   }

   /**
    * This <code>Builder</code> class follows the "Builder" pattern to create
    * an <code>AnalyticAggregator</code> object.
    */
   public static class Builder
   {
      private AnalyticFunction myAnalyticFunction;
      private PartitionClause myPartition;
      private OrderByClause myOrderBy;
      private WindowClause myWindow;

      /**
       * Constructs a <code>Builder</code> with no analytic function, no
       * partition, no order by clause, and no window clause.
       */
      public Builder()
      {
         myAnalyticFunction = null;
         myPartition = null;
         myOrderBy = null;
         myWindow = null;
      }

      /**
       * Sets the <code>AnalyticFunction</code>.  Required.
       * @param func The <code>AnalyticFunction</code>, which must not be
       *    <code>null</code>.
       * @return This <code>Builder</code>.
       * @throws IllegalArgumentException If <code>func</code> is
       *    <code>null</code>.
       */
      public Builder setAnalyticFunction(AnalyticFunction func)
      {
         if (func == null)
            throw new IllegalArgumentException("func must not be null!");
         myAnalyticFunction = func;
         return this;
      }

      /**
       * Sets the <code>PartitionClause</code>.  Optional.
       * @param partition The <code>PartitionClause</code>.
       * @return This <code>Builder</code>.
       */
      public Builder setPartition(PartitionClause partition)
      {
         myPartition = partition;
         return this;
      }

      /**
       * Sets the <code>OrderByClause</code>.  Optional.
       * @param orderBy The <code>OrderByClause</code>.
       * @return This <code>Builder</code>.
       */
      public Builder setOrderBy(OrderByClause orderBy)
      {
         myOrderBy = orderBy;
         return this;
      }

      /**
       * Sets the <code>WindowClause</code>.  Optional.
       * @param window The <code>WindowClause</code>.
       * @return This <code>Builder</code>.
       */
      public Builder setWindow(WindowClause window)
      {
         myWindow = window;
         return this;
      }

      /**
       * Build the <code>AnalyticAggregator</code> object.
       *
       * @return An <code>AnalyticAggregator</code>.
       * @throws ParseException If there is a problem with the analytic
       *    specification.
       * @see #setAnalyticFunction(AnalyticFunction)
       */
      public AnalyticAggregator build()
      {

         if (myWindow == null)
         {
            // Default window clause.
            myWindow = WindowClause.DEFAULT;
         }
         validate();
         if (!myAnalyticFunction.takesWindowClause())
         {
            // If it doesn't take a specified window, then it will supply its
            // own window.
            myWindow = myAnalyticFunction.getWindowClause();
         }
         return new AnalyticAggregator(this);
      }

      /**
       * Validates that the given clauses all work well together.  Some
       * <code>AnalyticFunctions</code> may require that the <code>WindowClause</code>
       * not be specified.
       * @throws ParseException If there is a problem with the analytic
       *    specification.
       */
      private void validate()
      {
         if (myAnalyticFunction == null)
         {
            throw new IllegalStateException("Must set an AnalyticFunction!");
         }
         // If window is range with at least one value that is not null or 0,
         // then the order by clause is required and it is restricted to
         // exactly one order-by element.
         Number startValue = myWindow.getStartValue();
         Number endValue = myWindow.getEndValue();
         if (myWindow.getWindowType() == WindowClause.Type.RANGE &&
             ((startValue != null && startValue.doubleValue() != 0) ||
              (endValue   != null && endValue.doubleValue()   != 0)))
         {
            if (myOrderBy == null || myOrderBy.getElements().size() != 1)
               throw new ParseException(
                  "Range window with bounds that exist and are not 0 must have exactly one element in the order by clause: " +
                  myAnalyticFunction + " " + myPartition + " " + myOrderBy + " " + myWindow);

         }

         // Some analytic functions require there not to be a window clause.
         if (!myAnalyticFunction.takesWindowClause() && myWindow != WindowClause.DEFAULT)
            throw new ParseException("Function " + myAnalyticFunction + " does not take a window clause.");
      }
   }
}
