package net.sf.jagg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jagg.model.AnalyticValue;
import net.sf.jagg.model.AnalyticContext;
import net.sf.jagg.model.OrderByElement;
import net.sf.jagg.model.WindowClause;
import net.sf.jagg.util.PartitionAndOrderByComparator;

/**
 * The <code>Analytic</code> class performs the actual analytic
 * operations.  It contains a <code>Builder</code> class that, following the
 * Builder Pattern, builds a <code>Analytic</code> object that can be used
 * for the actual analytic calculations.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class Analytic
{
   private static final boolean DEBUG = false;

   private List<AnalyticAggregator> myAnalytics;

   /**
    * Private constructor to ensure that the "Builder" pattern is used.
    * @param builder A <code>Builder</code>.
    */
   private Analytic(Builder builder)
   {
      myAnalytics = new ArrayList<AnalyticAggregator>(builder.myAnalytics);
   }

   /**
    * Perform one or more analytic operations on a <code>List&lt;T&gt;</code>.
    * <code>T</code> does not need to be <code>Comparable</code>.  This
    * operates on copies of the list of values, each sorted based on the
    * individual partitionBy clauses and/or orderBy clauses.
    * @param <T> The object type to aggregate.
    * @param values The <code>List&lt;T&gt;</code> of objects to aggregate.
    * @return A <code>List&lt;AggregateValue&lt;T&gt;&gt;</code>.
    */
   @SuppressWarnings("ForLoopReplaceableByForEach")
   public <T> List<AnalyticValue<T>> analyze(List<T> values)
   {
      // If no values, must return an empty list of AnalyticValues.
      if (values.size() == 0)
      {
         return new ArrayList<AnalyticValue<T>>();
      }
      List<AnalyticValue<T>> analyticValues = new ArrayList<AnalyticValue<T>>(values.size());
      for (int i = 0; i < values.size(); i++)
      {
         analyticValues.add(new AnalyticValue<T>(values.get(i)));
      }

      List<PartitionAndOrderByComparator<T>> sortComparators =
         new ArrayList<PartitionAndOrderByComparator<T>>(myAnalytics.size());
      List<PartitionAndOrderByComparator<T>> comparators =
         new ArrayList<PartitionAndOrderByComparator<T>>(myAnalytics.size());
      for (int i = 0; i < myAnalytics.size(); i++)
      {
         AnalyticAggregator ana = myAnalytics.get(i);
         PartitionAndOrderByComparator<T> comparator =
            new PartitionAndOrderByComparator<T>(ana.getPartition(), ana.getOrderBy());
         // Need original Comparator in the analysis process later.
         comparators.add(comparator);
         for (int j = 0; j < sortComparators.size(); j++)
         {
            PartitionAndOrderByComparator<T> existing = sortComparators.get(j);
            if (existing.covers(comparator))
            {
               if (DEBUG)
               {
                  System.out.println(existing + " covers " + comparator + "; choosing " + existing);
               }
               // Choose the existing comparator again.
               comparator = existing;
            }
         }

         for (int j = 0; j < sortComparators.size(); j++)
         {
            PartitionAndOrderByComparator<T> existing = sortComparators.get(j);
            if (comparator.covers(existing))
            {
               if (DEBUG)
               {
                  System.out.println(comparator + " covers " + existing + "; replacing " + existing + " with " + comparator);
               }
               // Overwrite.
               sortComparators.set(j, comparator);
            }
         }

         // Add in position.
         sortComparators.add(comparator);
      }

      if (DEBUG)
      {
         for (PartitionAndOrderByComparator<T> comparator : sortComparators)
         {
            System.out.println("Sort Comparator: " + comparator);
         }
      }

      if (myAnalytics.isEmpty())
      {
         // Nothing to process!
         return analyticValues;
      }

      // Create analytic contexts along with the individually sorted lists.
      List<AnalyticContext<T>> contexts = new ArrayList<AnalyticContext<T>>(sortComparators.size());
      for (int i = 0; i < sortComparators.size(); i++)
      {
         PartitionAndOrderByComparator<T> comparator = comparators.get(i);
         PartitionAndOrderByComparator<T> sortComparator = sortComparators.get(i);
         List<AnalyticValue<T>> sortedValues = null;

         // Try to find previously created equivalent comparator, so we don't
         // have to sort the list the same way multiple times.
         for (int j = 0; j < contexts.size(); j++)
         {
            AnalyticContext<T> context = contexts.get(j);
            PartitionAndOrderByComparator<T> previous = context.getComparator();
            if (previous.equals(sortComparator))
            {
               sortComparator = previous;
               sortedValues = context.getListOfValues();
               break;
            }
         }

         if (sortedValues == null)
         {
            // Not found previously.  Do a new sort of the values.
            // Copy the list of the analytic values.
            sortedValues = new ArrayList<AnalyticValue<T>>(analyticValues);
            Collections.sort(sortedValues, sortComparator);
            if (DEBUG)
            {
               System.out.println("Sorted values for " + myAnalytics.get(i) + ": ");
               for (AnalyticValue<T> anaValue : sortedValues)
               {
                  System.out.println(anaValue.getObject());
               }
            }
         }

         // Original PAOBC needed here; it'll be used in the analysis process.
         AnalyticContext<T> newContext = new AnalyticContext<T>(sortedValues, comparator);
         contexts.add(newContext);
      }

      doAnalysis(contexts);

      return analyticValues;
   }

   /**
    * Perform the actual analytic operations.
    *
    * TODO: Implement parallelism like Aggregation.doAggregation and the
    * methods it calls does.
    *
    * This restricts the parallelism based on
    * the size of the list of values to analyze, e.g. don't want to have a
    * parallelism of 8 when the list size is 6.  Then it delegates to either
    * the single-threaded or multi-threaded version of
    * <code>getAnalyzedValues</code>.
    * @param contexts The <code>Lists</code> of <code>AnalyticContexts</code>.
    *    Each <code>AnalyticContext</code> contains the <code>List</code> of
    *    values to analyze, plus the <code>PartitionAndOrderByComparator</code>
    *    that was used to sort them prior to analysis.
    * @return A <code>List</code> of <code>AnalyticValues</code>.
    */
   private <T> List<AnalyticValue<T>> doAnalysis(List<AnalyticContext<T>> contexts)
   {
      List<AnalyticValue<T>> analyzedList;
      // Begin commented code for parallelism, originally copied from
      // Aggregation.doAggregation.
//      int size = listCopy.size();
//      int minParallelism = (myParallelism > size) ? size : myParallelism;
//      if (minParallelism > 1)
//         analyzedList = getAnalyzedValues(listCopy, comparator, myParallelism);
//      else
         analyzedList = getAnalyzedValues(contexts);
      // There is no concept of super-analytics like there is super-aggregation
      // for aggregation.
      return analyzedList;
   }

   /**
    * Get all analyzed values for all analytics.  This is the single-
    * threaded version.
    * @param contexts The <code>List</code> of <code>AnalyticContexts</code>.
    * @return A <code>List</code> of <code>AnalyticValues</code>.
    */
   private <T> List<AnalyticValue<T>> getAnalyzedValues(List<AnalyticContext<T>> contexts)
   {
      List<AnalyticValue<T>> aggValues = new ArrayList<AnalyticValue<T>>();
      List<AnalyticAggregator> anaList = getAnalyticAggregatorsList();
      int anaSize = myAnalytics.size();
      int index = 0;
      int listsize = contexts.get(0).getListOfValues().size();

      for (int a = 0; a < anaSize; a++)
      {
         AnalyticContext<T> context = contexts.get(a);
         context.setEndOfPartitionIndex(Aggregations.indexOfLastMatching(
                 context.getListOfValues(), context.getComparator().getPartitionComparator(), index));
         AnalyticAggregator ana = anaList.get(a);

         anaList.addAll(ana.getDependentAnalyticAggregators());
         if (anaSize != anaList.size())
         {
            // Dependents were added.
            List<Integer> dependencies = new ArrayList<Integer>();
            for (int i = anaSize; i < anaList.size(); i++)
            {
               dependencies.add(i);
               // Add new contexts for these new analytics.
               AnalyticContext<T> newContext = new AnalyticContext<T>(context.getListOfValues(), context.getComparator());
               contexts.add(newContext);
            }
            context.setDependencies(dependencies);
         }

         anaSize = anaList.size();
         
         ana.init();
         if (DEBUG)
         {
            System.out.println("Beginning analysis: a = " + a + ", context's end of partition index = " +
                context.getEndOfPartitionIndex());
         }
      }

      while (index < listsize)
      {
         // Iterate backwards through the analytics, so that dependent
         // analytics are processed after the analytics on which they depend.
         for (int a = anaSize - 1; a >= 0; a--)
         {
            if (DEBUG)
            {
               System.out.println("index = " + index + ", a = " + a);
            }
            AnalyticContext<T> context = contexts.get(a);
            AnalyticAggregator ana = anaList.get(a);
            if (index > context.getEndOfPartitionIndex())
            {
               betweenPartitionsProcessing(a, ana, context);
               // Started a new partition for this AnalyticAggregator.
               context.setEndOfPartitionIndex(Aggregations.indexOfLastMatching(
                       context.getListOfValues(), context.getComparator().getPartitionComparator(), index));
               ana.init();
               if (DEBUG)
               {
                  System.out.println("  Partition ended; context's new end of partition index = " +
                     context.getEndOfPartitionIndex());
               }
            }

            processItem(a, ana, index, context);
         }
         index++;
      }

      // Finish up processing of the last partition.
      // Iterate backwards through the analytics, so that dependent
      // analytics are prcoessed after the analytics on which they depend.
      for (int a = anaSize - 1; a >= 0; a--)
      {
         AnalyticContext<T> context = contexts.get(a);
         AnalyticAggregator ana = anaList.get(a);
         if (index > context.getEndOfPartitionIndex())
         {
            betweenPartitionsProcessing(a, ana, context);
         }
      }

      return aggValues;
   }

   /**
    * This processing occurs before, between, and after the processing of
    * partitions.  It terminates all unterminated <code>AnalyticAggregators</code>
    * from the previous partition (if any).
    * @param anaIndex The 0-based analytic aggregator index, only used to store
    *    terminated values by index.
    * @param ana The <code>AnalyticAggregator</code>.
    * @param context The <code>AnalyticContext</code>, which knows the current
    *    window and through which index the <code>AnalyticValues</code> have
    *    been terminated.
    */
   private void betweenPartitionsProcessing(int anaIndex, AnalyticAggregator ana, AnalyticContext<?> context)
   {
      if (DEBUG)
      {
         System.out.println("Between partitions: " + anaIndex);
      }
      int endOfPartitionIndex = context.getEndOfPartitionIndex();
      while (context.getTerminatedThroughIndex() < endOfPartitionIndex)
      {
         if (context.getWindowStartIndex() > context.getEndOfPartitionIndex() ||
             isInWindow(context, ana, context.getWindowStartIndex()))
         {
            terminate(anaIndex, ana, context);
         }
         else
         {
            delete(ana, context);
         }
      }
      context.advanceWindowStartPastLastTerminated();
   }

   /**
    * Processes the item at the given start index.  This may include
    * terminating analytic values whose windows are complete.  This may also
    * include deleting values that are no longer supposed to be in the current
    * window.  Iterates the item at the given start index and updates the
    * window.
    * @param anaIndex The 0-based analytic aggregator index, only used to store
    *    terminated values by index.
    * @param ana The <code>AnalyticAggregator</code>.
    * @param index The current 0-based index into the list of values to
    *    iterate.
    * @param context The <code>AnalyticContext</code>, which knows the current
    *    window and through which index the <code>AnalyticValues</code> have
    *    been terminated.
    */
   private void processItem(int anaIndex, AnalyticAggregator ana, int index, AnalyticContext<?> context)
   {
      // Determine whether to terminate.
      while (context.getTerminatedThroughIndex() < context.getEndOfPartitionIndex() &&
             !isInWindow(context, ana, index, false))
      {
         terminate(anaIndex, ana, context);
         
         // Determine whether to delete a value.  Any delete values slide the
         // beginning of the window forward.
         while (context.getWindowEndIndex() >= context.getWindowStartIndex() &&
                !isInWindow(context, ana, context.getWindowStartIndex()))
         {
            delete(ana, context);
         }
      }
      // Iterate.  This slides the end of the window forward.
      AnalyticValue<?> iterValue = context.getListOfValues().get(index);
      Object valueToIterate = iterValue.getObject();
      ana.iterate(valueToIterate);
      if (DEBUG)
      {
         System.out.println("  Iterating " + valueToIterate);
      }
      context.incrementWindowEndIndex();
   }

   /**
    * Returns whether the current value to be analyzed at the given index is
    * within the window described by the next indexed value to be terminated.
    * @param context The <code>AnalyticContext</code>.
    * @param ana The <code>AnalyticAggregator</code>.
    * @param index The index that will be iterated next.
    * @return Whether what is about to be iterated is within the current window.
    */
   private <T> boolean isInWindow(AnalyticContext<T> context, AnalyticAggregator ana, int index)
   {
      return isInWindow(context, ana, index, true);
   }

   /**
    * Returns whether the current value to be analyzed at the given index is
    * within the window described by the next indexed value to be terminated.
    * @param context The <code>AnalyticContext</code>.
    * @param ana The <code>AnalyticAggregator</code>.
    * @param index The index that will be iterated next.
    * @param considerStart This will consider the start of the window only if
    *    this is set to <code>true</code>.  <code>false</code> will allow it to
    *    be "in the window" even if the beginning of the window hasn't been
    *    iterated yet.
    * @return Whether what is about to be iterated is within the current window.
    */
   private <T> boolean isInWindow(AnalyticContext<T> context, AnalyticAggregator ana, int index, boolean considerStart)
   {
      WindowClause window = ana.getWindow();
      int nextTerminationIndex = context.getTerminatedThroughIndex() + 1;
      switch(window.getWindowType())
      {
         case ROWS:
         {
            if (considerStart && window.getStartValue() != null)
            {
               int startWindow = nextTerminationIndex - window.getStartValue().intValue();
               if (startWindow > index)
                  return false;
            }
            if (window.getEndValue() != null)
            {
               int endWindow = nextTerminationIndex + window.getEndValue().intValue();
               if (index > endWindow)
                  return false;
            }
            return true;
         }
         case RANGE:
         {
            // Get the property that the first order-by element uses.
            // If actual values are involved, then the Order By Elements
            // should have been restricted already to exactly one property.
            PartitionAndOrderByComparator<T> comparator = context.getComparator();
            AnalyticValue<T> nextTerminatedValue = context.getListOfValues().get(nextTerminationIndex);
            AnalyticValue<T> indexValue = context.getListOfValues().get(index);
            Object objNextTerminatedValue = nextTerminatedValue.getObject();
            Object objIndexPropValue = indexValue.getObject();
            if (considerStart && window.getStartValue() != null)
            {
               if (window.getStartValue().doubleValue() == 0)
               {
                  if (index < nextTerminationIndex && comparator.compare(nextTerminatedValue, indexValue) != 0)
                     return false;
               }
               else
               {
                  OrderByElement only = comparator.getOrderByElements().get(0);
                  String property = only.getProperty();
                  Number nextTerminatedPropValue = (Number) Aggregator.getValueFromProperty(objNextTerminatedValue, property);
                  Number indexPropValue = (Number) Aggregator.getValueFromProperty(objIndexPropValue, property);
                  if (nextTerminatedPropValue == null || indexPropValue == null) return false;  // TODO: Is this correct?
                  double dNextTerminatedValue = nextTerminatedPropValue.doubleValue();
                  double dIndexValue = indexPropValue.doubleValue();

                  switch(only.getSortDir())
                  {
                     case ASC:
                     {
                        double startWindow = dNextTerminatedValue - window.getStartValue().doubleValue();
                        if (DEBUG)
                        {
                           System.out.println("    +startWindow: " + startWindow + ", dNextTerminatedValue: " + dNextTerminatedValue);
                        }
                        if (startWindow > dIndexValue)
                           return false;
                        break;
                     }
                     case DESC:
                     {
                        double startWindow = dNextTerminatedValue + window.getStartValue().doubleValue();
                        if (DEBUG)
                        {
                           System.out.println("    -startWindow: " + startWindow + ", dNextTerminatedValue: " + dNextTerminatedValue);
                        }
                        if (startWindow < dIndexValue)
                           return false;
                        break;
                     }
                  }
               }
            }
            if (window.getEndValue() != null)
            {
               if (window.getEndValue().doubleValue() == 0)
               {
                  if (index > nextTerminationIndex && comparator.compare(nextTerminatedValue, indexValue) != 0)
                     return false;
               }
               else
               {
                  OrderByElement only = comparator.getOrderByElements().get(0);
                  String property = only.getProperty();
                  Number nextTerminatedPropValue = (Number) Aggregator.getValueFromProperty(objNextTerminatedValue, property);
                  Number indexPropValue = (Number) Aggregator.getValueFromProperty(objIndexPropValue, property);
                  if (nextTerminatedPropValue == null || indexPropValue == null) return false;  // TODO: Is this correct?
                  double dNextTerminatedValue = nextTerminatedPropValue.doubleValue();
                  double dIndexValue = indexPropValue.doubleValue();

                  switch(only.getSortDir())
                  {
                     case ASC:
                     {
                        double endWindow = dNextTerminatedValue + window.getEndValue().doubleValue();
                        if (DEBUG)
                        {
                           System.out.println("    +endWindow: " + endWindow + ", dNextTerminatedValue: " + dNextTerminatedValue);
                        }
                        if (dIndexValue > endWindow)
                           return false;
                        break;
                     }
                     case DESC:
                     {
                        double endWindow = dNextTerminatedValue - window.getEndValue().doubleValue();
                        if (DEBUG)
                        {
                           System.out.println("    -endWindow: " + endWindow + ", dNextTerminatedValue: " + dNextTerminatedValue);
                        }
                        if (dIndexValue < endWindow)
                           return false;
                        break;
                     }
                  }
               }
            }
            return true;
         }
         default:
            return false;
      }
   }

   /**
    * Terminates the next item due to be terminated.
    * @param anaIndex The 0-based analytic aggregator index, only used to store
    *    terminated values by index.
    * @param ana The <code>AnalyticAggregator</code>.
    * @param context The <code>AnalyticContext</code>, which knows the current
    *    window and through which index the <code>AnalyticValues</code> have
    *    been terminated.
    */
   private void terminate(int anaIndex, AnalyticAggregator ana, AnalyticContext<?> context)
   {
      int nextTerminationIndex = context.getTerminatedThroughIndex() + 1;
      AnalyticValue<?> valueToTerminate = context.getListOfValues().get(nextTerminationIndex);
      ana.setValuesForDependentAnalytics(valueToTerminate, context);
      valueToTerminate.setAnalyzedValue(anaIndex, ana, ana.terminate());
      if (DEBUG)
      {
         System.out.println("  Terminating analytic value at " + nextTerminationIndex);
      }
      context.incrementTerminatedThroughIndex();
   }

   /**
    * Deletes the item at the start of the window.
    * @param ana The <code>AnalyticAggregator</code>.
    * @param context The <code>AnalyticContext</code>, which knows the current
    *    window and through which index the <code>AnalyticValues</code> have
    *    been terminated.
    */
   private void delete(AnalyticAggregator ana, AnalyticContext<?> context)
   {
      AnalyticValue<?> deleteValue = context.getListOfValues().get(context.getWindowStartIndex());
      Object valueToDelete = deleteValue.getObject();
      ana.delete(valueToDelete);
      if (DEBUG)
      {
         System.out.println("  Deleting " + valueToDelete);
      }
      context.incrementWindowStartIndex();
   }

   /**
    * Helper function to create a new <code>List</code> of
    * <code>AnalyticAggregators</code>.
    * @return A <code>List</code> of <code>AnalyticAggregators</code>.
    */
   private List<AnalyticAggregator> getAnalyticAggregatorsList()
   {
      int anaSize = myAnalytics.size();
      List<AnalyticAggregator> aggList = new ArrayList<AnalyticAggregator>(anaSize);
      for (int a = 0; a < anaSize; a++)
      {
         AnalyticAggregator archetype = myAnalytics.get(a);
         aggList.add((AnalyticAggregator) Aggregator.getAggregator(archetype));
      }
      return aggList;
   }

   /**
    * This <code>Builder</code> class follows the "Builder" pattern to create
    * an <code>Analytic</code> object.
    */
   public static class Builder
   {
      private List<AnalyticAggregator> myAnalytics;

      /**
       * Constructs a <code>Builder</code> with no analytics.
       */
      public Builder()
      {
         myAnalytics = null;
      }

      /**
       * Sets the <code>List</code> of <code>AnalyticAggregators</code> to use.  The
       * <code>AnalyticAggregators</code> define which analytic operatons to perform.
       * @param analytics A <code>List</code> of <code>AnalyticAggregators</code>.
       *    AnalyticAggregators can be created in two ways: direct instantiation, or by
       *    using the factory method.
       *    <ul>
       *        <li><code>AnalyticAggregator ana = new AnalyticAggregator.Builder()
       * .setAnalyticFunction(new SumAggregator("value")
       * .setPartition(new PartitionClause(Arrays.asList("value")))
       * .setOrderBy(new OrderByClause(Arrays.asList(
       *     new OrderByElement("category", OrderByElement.SortDir.DESC, OrderByElement.NullSort.LAST))))
       * .setWindow(new WindowClause(WindowClause.Type.ROWS, 3, 0))
       * .build();</code>
       *        <li><code>AnalyticAggregator ana =
       * AnalyticAggregator.getAnalytic("Sum(value) partitionBy(value) orderBy(category desc nulls last) rows(3, 0)");</code>
       *    </ul>
       * @return This <code>Builder</code>.
       * @see AnalyticAggregator
       * @see AnalyticFunction
       */
      public Builder setAnalytics(List<AnalyticAggregator> analytics)
      {
         myAnalytics = analytics;
         return this;
      }

      /**
       * Build the <code>Analytic</code> object.
       * @return An <code>Analytic</code> object that can be used to perform
       *    the actual analytic calculations.
       * @throws IllegalArgumentException If at least one
       *    <code>AnalyticFunction</code> was not supplied with the
       *    <code>setAnalytics</code> method.
       * @see #setAnalytics
       */
      public Analytic build()
      {
         if (myAnalytics == null || myAnalytics.isEmpty())
            throw new IllegalArgumentException("Analytic.Builder: Must supply at least one AnalyticFunction.");
         return new Analytic(this);
      }
   }
}
