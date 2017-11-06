package net.sf.jagg.model;

import java.lang.reflect.InvocationTargetException;

import net.sf.jagg.exception.PropertyAccessException;

/**
 * A <code>ChainedMethodCall</code> represents a chain of method calls given by
 * a single property string.
 *
 * @author Randy Gettman
 * @since 0.8.0
 */
public abstract class ChainedMethodCall
{
   private ChainedMethodCall myNext;

   /**
    * Sets the next <code>ChainedMethodCall</code> in the chain, if any.
    * @param next The next <code>ChainedMethodCall</code> in the chain, if any.
    */
   public void setNext(ChainedMethodCall next)
   {
      myNext = next;
   }

   /**
    * Invokes this <code>ChainedMethodCall</code>.  If there is a next
    * <code>ChainedMethodCall</code>, then invoke it also.
    * @param object The object on which to invoke this method.
    * @return The return of all calls in the chain.
    * @throws PropertyAccessException If there was a problem invoking the
    *    method.
    */
   public Object invoke(Object object) throws PropertyAccessException
   {
      try
      {
         Object result = invokeMethod(object);
         if (myNext != null)
         {
            result = myNext.invoke(result);
         }
         return result;
      }
      catch (IllegalAccessException e)
      {
         throw new PropertyAccessException("IllegalAccessException caught!", e);
      }
      catch (InvocationTargetException e)
      {
         throw new PropertyAccessException("InvocationTargetException caught!", e);
      }
   }

   /**
    * Invokes only this <code>ChainedMethodCall</code>.  Invoking other methods
    * in the chain is handled by the <code>invoke</code> method.
    * @param object The object on which to invoke this method.
    * @return The result of invoking this method.
    * @see #invoke
    * @throws IllegalAccessException If the <code>Method</code> is inaccessible
    *    (private, etc.)
    * @throws InvocationTargetException If the <code>Method</code> throws an
    *    <code>Exception</code> during execution.
    */
   protected abstract Object invokeMethod(Object object) throws IllegalAccessException, InvocationTargetException;

   /**
    * Returns the return type of the <code>ChainedMethodCall</code>.
    * @return A <code>Class</code> object representing the return type of the
    *    whole chained call.
    */
   public Class<?> getChainedReturnType()
   {
      if (myNext != null)
         return myNext.getChainedReturnType();
      return getReturnType();
   }

   /**
    * Returns the return type of this particular<code>ChainedMethodCall</code>.
    * The return type of the entire chained call is handled by the
    * <code>getChainedReturnType</code> method.
    * @return A <code>Class</code> object representing the return type of this
    *    particular method, not the entire chained call.
    */
   public abstract Class<?> getReturnType();
}
