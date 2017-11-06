package net.sf.jagg.exception;

/**
 * A <code>JaggException</code> represents an exceptional condition that
 * occurred during jAgg processing and it is the parent of all jAgg-related
 * exceptions.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class JaggException extends RuntimeException
{
   /**
    * Create a <code>JaggException</code>.
    */
   public JaggException()
   {
      super();
   }

   /**
    * Create a <code>JaggException</code> with the given message.
    * @param message The message.
    */
   public JaggException(String message)
   {
      super(message);
   }

   /**
    * Create a <code>JaggException</code>.
    * @param cause The cause.
    */
   public JaggException(Throwable cause)
   {
      super(cause);
   }

   /**
    * Create a <code>JaggException</code> with the given message.
    * @param message The message.
    * @param cause The cause.
    */
   public JaggException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
