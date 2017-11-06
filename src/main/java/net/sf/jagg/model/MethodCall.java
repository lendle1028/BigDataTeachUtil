package net.sf.jagg.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.jagg.model.ChainedMethodCall;

/**
 * A <code>MethodCall</code> bundles a <code>Method</code> object and an array
 * of parameter values together so they can go together into a <code>Map</code>
 * as the value.  This class existed as a private inner class of
 * <code>MethodCache</code> prior to version 0.5.0, but for 0.5.0, it was
 * pulled out and made public.  It was made to extend
 * <code>ChainedMethodCall</code> for version 0.8.0.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class MethodCall extends ChainedMethodCall
{
   private Method myMethod;
   private Object[] myParameters;

   /**
    * Constructs a <code>MethodCall</code>.
    * @param method The <code>Method</code>.
    * @param parameters The array of parameter values.
    */
   public MethodCall(Method method, Object[] parameters)
   {
      myMethod = method;
      myParameters = parameters;
   }

   /**
    * Returns the return type of the <code>MethodCall</code>.
    * @return A <code>Class</code> object representing the return type of the
    *    method.
    */
   public Class<?> getReturnType()
   {
      return myMethod.getReturnType();
   }

   /**
    * Invokes the internal <code>Method</code> using the internal parameters,
    * and returns the result.
    * @param object The object on which to invoke the <code>Method</code>.
    * @return The result of the invocation on the <code>Method</code>.
    * @throws IllegalAccessException If the <code>Method</code> is inaccessible
    *    (private, etc.)
    * @throws InvocationTargetException If the <code>Method</code> throws an
    *    <code>Exception</code> during execution.
    */
   protected Object invokeMethod(Object object) throws IllegalAccessException, InvocationTargetException
   {
      return myMethod.invoke(object, myParameters);
   }
}
