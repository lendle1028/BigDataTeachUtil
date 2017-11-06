package net.sf.jagg;

import net.sf.jagg.exception.ExpectedNumberException;
import net.sf.jagg.math.DoubleDouble;
import net.sf.jagg.model.WindowClause;

/**
 * This class allows represents the "product" aggregator over numeric values.
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class ProductAggregator extends Aggregator implements AnalyticFunction
{
   private DoubleDouble myProduct = new DoubleDouble();
   private int myNumZeroes = 0;

   /**
    * Constructs an <code>ProductAggregator</code> that operates on the specified
    * property.
    * @param property Multiply all this property's values.
    */
   public ProductAggregator(String property)
   {
      setProperty(property);
   }

   /**
    * Returns an uninitialized copy of this <code>Aggregator</code> object,
    * with the same property(ies) to analyze.
    * @return An uninitialized copy of this <code>Aggregator</code> object.
    */
   public ProductAggregator replicate()
   {
      return new ProductAggregator(getProperty());
   }

   /**
    * Initialize the product to one.
    */
   public void init()
   {
      myProduct.reset();
      myProduct.addToSelf(1.0);
      myNumZeroes = 0;
   }

   /**
    * Multiply-in the factor given by this value, ignorning nulls.
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
               double d = obj.doubleValue();
               if (d == 0.0)
               {
                  myNumZeroes++;
               }
               else
               {
                  myProduct.multiplySelfBy(d);
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
    * Divide-out the factor given by this value, ignorning nulls.
    *
    * @param value The value to remove.
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
               if (obj.doubleValue() == 0.0)
               {
                  myNumZeroes--;
               }
               else
               {
                  myProduct.divideSelfBy(obj.doubleValue());
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
    * The product function can take a window clause.
    * @return <code>true</code>.
    * @since 0.9.0
    */
   public boolean takesWindowClause()
   {
      return true;
   }

   /**
    * The product function doesn't supply its own window clause.
    * @return <code>null</code>
    * @since 0.9.0
    */
   public WindowClause getWindowClause()
   {
      return null;
   }

   /**
    * Merge the given <code>Aggregator</code> into this one by multiplying the
    * respective products.
    *
    * @param agg The <code>Aggregator</code> to merge into this one.
    */
   public void merge(AggregateFunction agg)
   {
      if (agg != null && agg instanceof ProductAggregator)
      {
         ProductAggregator otherAgg = (ProductAggregator) agg;
         myProduct.multiplySelfBy(otherAgg.myProduct);
         myNumZeroes += otherAgg.myNumZeroes;
      }
   }

   /**
    * Return the product of all values.
    *
    * @return The product, as a <code>Double</code>, or <code>1</code> if no
    *    values have been processed.
    */
   public Double terminate()
   {
      return terminateDoubleDouble().doubleValue();
   }

   /**
    * Return the result as a <code>DoubleDouble</code>.  This is used mainly
    * when other <code>Aggregators</code> that use this result must maintain a
    * high precision.
    * @return The product, as a <code>DoubleDouble</code>, or <code>1</code>
    *    if no values have been accumulated.
    * @since 0.4.0
    */
   public DoubleDouble terminateDoubleDouble()
   {
      if (myNumZeroes > 0)
      {
         return new DoubleDouble(0.0);
      }
      return new DoubleDouble(myProduct);
   }
}
