package net.sf.jagg;

import net.sf.jagg.exception.ExpectedNumberException;
import net.sf.jagg.math.DoubleDouble;
import net.sf.jagg.model.WindowClause;

/**
 * This abstract class represents covariance-like aggregator calculations over
 * numeric values.
 *
 * @author Randy Gettman
 * @since 0.3.0
 */
public abstract class AbstractCovarianceAggregator extends TwoPropAggregator implements AnalyticFunction
{
   /**
    * A running count of items processed so far where BOTH properties yield
    * non-null values.
    */
   protected long   myCount;
   /**
    * A running sum of items processed so far for the FIRST property.
    */
   protected DoubleDouble mySum1 = new DoubleDouble();
   /**
    * A running sum of items processed so far for the SECOND property.
    */
   protected DoubleDouble mySum2 = new DoubleDouble();
   /**
    * A running total of the variance, before it is divided by
    * the denominator in the variance calculation.
    */
   protected DoubleDouble myVarNumerator = new DoubleDouble();

   /**
    * Constructs a <code>CovarianceAggregator</code> on the specified
    * properties, in the format: <code>property, property2</code>.
    * @param properties A specification string in the format:
    *    <code>property, property2</code>.
    */
   public AbstractCovarianceAggregator(String properties)
   {
      setProperty(properties);
   }

   /**
    * Constructs a <code>CovarianceAggregator</code> that operates on the specified
    * properties.
    * @param property Determine the covariance of this property with the other.
    * @param property2 Determine the covariance of this property with the other.
    */
   public AbstractCovarianceAggregator(String property, String property2)
   {
      setProperty(property + "," + property2);
   }

   /**
    * Initialize the sums, count, and variance numerator to zero.
    */
   public void init()
   {
      myCount = 0;
      mySum1.reset();
      mySum2.reset();
      myVarNumerator.reset();
   }

   /**
    * Count only if both properties are non-null.  Sum both properties.
    * Update the variance numerator.
    *
    * @param value The value to aggregate.
    */
   public void iterate(Object value)
   {
      if (value != null)
      {
         String property1 = getProperty();
         String property2 = getProperty2();
         try
         {
            Number obj1 = (Number) getValueFromProperty(value, property1);
            Number obj2 = (Number) getValueFromProperty(value, property2);
            // Don't count nulls.
            if (obj1 != null && obj2 != null)
            {
               long oldCount = myCount;
               myCount++;
               double dVal1 = obj1.doubleValue();
               double dVal2 = obj2.doubleValue();

               // Running algorithm adapted from "Updating Formulae and a
               // Pairwise Algorithm for Computing Sample Variances" by Chan,
               // Gloub, and LeVeque, November 1979, Stanford University.

               // Running sums.
               mySum1.addToSelf(dVal1);
               mySum2.addToSelf(dVal2);
               // Running variance numerator.
               if (myCount == 1)
                  myVarNumerator.reset();
               else
               {
                  // temp = myCount * dVal1 - mySum1;
                  DoubleDouble temp = new DoubleDouble(dVal1);
                  temp.multiplySelfBy(myCount);
                  temp.subtractFromSelf(mySum1);
                  // temp2 = myCount * dVal2 - mySum2;
                  DoubleDouble temp2 = new DoubleDouble(dVal2);
                  temp2.multiplySelfBy(myCount);
                  temp2.subtractFromSelf(mySum2);
                  // temp *= temp2;
                  temp.multiplySelfBy(temp2);
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
            throw new ExpectedNumberException("Property(ies) \"" + property1 +
               " and " + property2 + "\" must represent Numbers.", e);
         }
      }
   }

   /**
    * Un-count only if both properties are non-null.  Un-sum both properties.
    * Un-update the variance numerator.
    *
    * @param value The value to delete.
    * @since 0.9.0
    */
   public void delete(Object value)
   {
      if (value != null)
      {
         String property1 = getProperty();
         String property2 = getProperty2();
         try
         {
            Number obj1 = (Number) getValueFromProperty(value, property1);
            Number obj2 = (Number) getValueFromProperty(value, property2);
            // Don't count nulls.
            if (obj1 != null && obj2 != null)
            {
               long newCount = myCount - 1;
               double dVal1 = obj1.doubleValue();
               double dVal2 = obj2.doubleValue();

               // Running variance numerator.
               // "Undo" the iteration.
               if (myCount <= 2)
               {
                  myVarNumerator.reset();
               }
               else
               {
                  // temp = myCount * dVal1 - mySum1;
                  DoubleDouble temp = new DoubleDouble(dVal1);
                  temp.multiplySelfBy(myCount);
                  temp.subtractFromSelf(mySum1);
                  // temp2 = myCount * dVal2 - mySum2;
                  DoubleDouble temp2 = new DoubleDouble(dVal2);
                  temp2.multiplySelfBy(myCount);
                  temp2.subtractFromSelf(mySum2);
                  // temp *= temp2;
                  temp.multiplySelfBy(temp2);
                  // temp /= (myCount * oldCount);
                  temp.divideSelfBy(myCount);
                  temp.divideSelfBy(newCount);
                  // Subtract what was added.
                  // myVarNumerator -= temp;
                  myVarNumerator.subtractFromSelf(temp);
               }

               // Running sums.
               mySum1.subtractFromSelf(dVal1);
               mySum2.subtractFromSelf(dVal2);

               myCount--;
            }
         }
         catch (ClassCastException e)
         {
            throw new ExpectedNumberException("Property(ies) \"" + property1 +
               " and " + property2 + "\" must represent Numbers.", e);
         }
      }
   }

   /**
    * All covariance functions can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * No covariance functions supply their own window clause.
    * @return <code>null</code>
    * @since 0.9.0
    */
   public WindowClause getWindowClause()
   {
      return null;
   }

   /**
    * Merge the given <code>Aggregator</code> into this one.  Add the sums
    * together.  Add in the count.  Update the variance numerator.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof AbstractCovarianceAggregator)
      {
         AbstractCovarianceAggregator otherAgg = (AbstractCovarianceAggregator) agg;
         if (myCount == 0)
         {
            // Nothing on this side yet.  Just copy the other one over.
            myCount = otherAgg.myCount;
            mySum1.addToSelf(otherAgg.mySum1);
            mySum2.addToSelf(otherAgg.mySum2);
            myVarNumerator.addToSelf(otherAgg.myVarNumerator);
         }
         else if (otherAgg.myCount > 0)
         {
            // We have something on this side, and there's something on the
            // other side.
            
            // Merging algorithm adapted from "Updating Formulae and a
            // Pairwise Algorithm for Computing Sample Variances" by Chan,
            // Gloub, and LeVeque, November 1979, Stanford University.
            // nOverM = (double) otherAgg.myCount / myCount;
            DoubleDouble nOverM = new DoubleDouble(otherAgg.myCount);
            nOverM.divideSelfBy(myCount);
            // double temp = nOverM * mySum1 - otherAgg.mySum1;
            DoubleDouble temp = new DoubleDouble(nOverM);
            temp.multiplySelfBy(mySum1);
            temp.subtractFromSelf(otherAgg.mySum1);
            // double temp2 = nOverM * mySum2 - otherAgg.mySum2;
            DoubleDouble temp2 = new DoubleDouble(nOverM);
            temp2.multiplySelfBy(mySum2);
            temp2.subtractFromSelf(otherAgg.mySum2);
            // temp *= temp2;
            temp.multiplySelfBy(temp2);
            // myVarNumerator += otherAgg.myVarNumerator +
            //    (double) myCount / (otherAgg.myCount * (myCount + otherAgg.myCount)) * temp;
            DoubleDouble temp3 = new DoubleDouble(myCount);
            temp3.divideSelfBy(otherAgg.myCount * (myCount + otherAgg.myCount));
            temp3.multiplySelfBy(temp);
            myVarNumerator.addToSelf(otherAgg.myVarNumerator);
            myVarNumerator.addToSelf(temp3);

            mySum1.addToSelf(otherAgg.mySum1);
            mySum2.addToSelf(otherAgg.mySum2);
            myCount += otherAgg.myCount;
         }
      }
   }
}
