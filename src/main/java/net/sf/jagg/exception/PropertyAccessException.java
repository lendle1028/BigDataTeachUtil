package net.sf.jagg.exception;

/**
 * A <code>PropertyAccessException</code> is a <code>JaggException</code> that
 * indicates that there was a problem accessing a property of a data value.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class PropertyAccessException extends JaggException
{
   /**
    * Create a <code>PropertyAccessException</code>.
    */
   public PropertyAccessException()
   {
      super();
   }

   /**
    * Create a <code>PropertyAccessException</code> with the given message.
    * @param message The message.
    */
   public PropertyAccessException(String message)
   {
      super(message);
   }

   /**
    * Create a <code>PropertyAccessException</code>.
    * @param cause The cause.
    */
   public PropertyAccessException(Throwable cause)
   {
      super(cause);
   }

   /**
    * Create a <code>PropertyAccessException</code> with the given message.
    * @param message The message.
    * @param cause The cause.
    */
   public PropertyAccessException(String message, Throwable cause)
   {
      super(message, cause);
   }
}