package net.sf.jagg;

import net.sf.jagg.exception.ExpectedNumberException;
import net.sf.jagg.math.DoubleDouble;
import net.sf.jagg.model.WindowClause;

/**
 * This abstract class represents variance-like aggregator calculations over
 * numeric values.
 *
 * @author Randy Gettman
 * @since 0.3.0
 */
public abstract class AbstractVarianceAggregator extends Aggregator implements AnalyticFunction
{
   /**
    * A running count of items processed so far for the given property.
    */
   protected long   myCount;
   /**
    * A running total of items processed so far for the given property.
    */
   protected DoubleDouble mySum = new DoubleDouble();
   /**
    * A running total of the variance, before it is divided by
    * the denominator in the variance calculation.
    */
   protected DoubleDouble myVarNumerator = new DoubleDouble();

   /**
    * Constructs an <code>VarianceAggregator</code> that operates on the specified
    * property.
    * @param property Calculate the variance of this property's values.
    */
   public AbstractVarianceAggregator(String property)
   {
      setProperty(property);
   }

   /**
    * Initialize the sum and count to zero.
    */
   public void init()
   {
      myCount = 0;
      mySum.reset();
      myVarNumerator.reset();
   }

   /**
    * If the property is non-null, then count it and add the property value to
    * the sum.  Update the variance numerator.
    *
    * @param value The value to aggregate.
    */
   public void iterate(Object value)
   {
      if (value != null)
      {
         String property = getProperty();
         try
         {
            Number obj = (Number) getValueFromProperty(value, property);
            // Don't count nulls.
            if (obj != null)
            {
               long oldCount = myCount;
               myCount++;
               double dVal = obj.doubleValue();

               // Running algorithm adapted from "Updating Formulae and a
               // Pairwise Algorithm for Computing Sample Variances" by Chan,
               // Gloub, and LeVeque, November 1979, Stanford University.

               // Running sum.
               mySum.addToSelf(dVal);
               // Running variance numerator.
               if (myCount == 1)
                  myVarNumerator.reset();
               else
               {
                  // temp = myCount * dVal - mySum;
                  DoubleDouble temp = new DoubleDouble(dVal);
                  temp.multiplySelfBy(myCount);
                  temp.subtractFromSelf(mySum);
                  // temp *= temp;
                  temp.squareSelf();
                  // temp /= (myCount * oldCount);
                  temp.divideSelfBy(myCount);
                  temp.divideSelfBy(oldCount);
                  // myVarNumerator += temp;
                  myVarNumerator.addToSelf(temp);
               }
            }
         }
         catch (ClassCastException e)
         {
            throw new ExpectedNumberException("Property \"" + property +
               "\" must represent a Number.", e);
         }
      }
   }

   /**
    * If the property is non-null, then un-count it and subtract the property
    * value from the sum.  Un-update the variance numerator.
    *
    * @param value The value to delete.
    * @since 0.9.0
    */
   public void delete(Object value)
   {
      if (value != null)
      {
         String property = getProperty();
         try
         {
            Number obj = (Number) getValueFromProperty(value, property);
            // Don't count nulls.
            if (obj != null)
            {
               long newCount = myCount - 1;
               double dVal = obj.doubleValue();

               // Running variance numerator.
               // "Undo" the iteration.
               if (myCount <= 2)
               {
                  // Count will drop to 1, which means a variance of 0.
                  myVarNumerator.reset();
               }
               else
               {
                  // temp = myCount * dVal - mySum;
                  DoubleDouble temp = new DoubleDouble(dVal);
                  temp.multiplySelfBy(myCount);
                  temp.subtractFromSelf(mySum);
                  // temp *= temp;
                  temp.squareSelf();
                  // temp /= (myCount * newCount);
                  temp.divideSelfBy(myCount);
                  temp.divideSelfBy(newCount);
                  // Subtract what was added.
                  // myVarNumerator -= temp;
                  myVarNumerator.subtractFromSelf(temp);
               }

               // Running sum.
               mySum.subtractFromSelf(dVal);

               myCount--;
            }
         }
         catch (ClassCastException e)
         {
            throw new ExpectedNumberException("Property \"" + property +
                    "\" must represent a Number.", e);
         }
      }
   }

   /**
    * All variance functions can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * No variance functions supply their own window clause.
    * @return <code>null</code>
    * @since 0.9.0
    */
   public WindowClause getWindowClause()
   {
      return null;
   }

   /**
    * Merge the given <code>Aggregator</code> into this one.  Add the
    * respective sums and counts together.  Update the variance numerator.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof AbstractVarianceAggregator)
      {
         AbstractVarianceAggregator otherAgg = (AbstractVarianceAggregator) agg;
         if (myCount == 0)
         {
            // Nothing on this side yet.  Just copy the other one over.
            myCount = otherAgg.myCount;
            mySum.addToSelf(otherAgg.mySum);
            myVarNumerator.addToSelf(otherAgg.myVarNumerator);
         }
         else if (otherAgg.myCount > 0)
         {
            // We have something on this side, and there's something on the
            // other side.
            
            // Merging algorithm adapted from "Updating Formulae and a
            // Pairwise Algorithm for Computing Sample Variances" by Chan,
            // Gloub, and LeVeque, November 1979, Stanford University.
            // temp = ((double) otherAgg.myCount / myCount) * mySum - otherAgg.mySum;
            DoubleDouble temp = new DoubleDouble(otherAgg.myCount);
            temp.divideSelfBy(myCount);
            temp.multiplySelfBy(mySum);
            temp.subtractFromSelf(otherAgg.mySum);
            // temp *= temp;
            temp.squareSelf();
            // myVarNumerator += otherAgg.myVarNumerator +
            //    (double) myCount / (otherAgg.myCount * (myCount + otherAgg.myCount)) * temp;
            DoubleDouble temp3 = new DoubleDouble(myCount);
            temp3.divideSelfBy(otherAgg.myCount * (myCount + otherAgg.myCount));
            temp3.multiplySelfBy(temp);
            myVarNumerator.addToSelf(otherAgg.myVarNumerator);
            myVarNumerator.addToSelf(temp3);

            mySum.addToSelf(otherAgg.mySum);
            myCount += otherAgg.myCount;
         }
      }
   }

   /**
    * Return the result as a <code>DoubleDouble</code>.  This is used mainly
    * when other <code>Aggregators</code> that use this result must maintain a
    * high precision.
    * @return A <code>DoubleDouble</code> representing the result of the
    *    aggregation.
    */
   public abstract DoubleDouble terminateDoubleDouble();
}
