package net.sf.jagg.exception;

/**
 * An <code>ExpectedNumberException</code> is a <code>JaggException</code> that
 * indicates that a data value was expected to be a <code>Number</code>, but it
 * wasn't.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class ExpectedNumberException extends JaggException
{
   /**
    * Create a <code>ExpectedNumberException</code>.
    */
   public ExpectedNumberException()
   {
      super();
   }

   /**
    * Create a <code>ExpectedNumberException</code> with the given message.
    * @param message The message.
    */
   public ExpectedNumberException(String message)
   {
      super(message);
   }

   /**
    * Create a <code>ExpectedNumberException</code>.
    * @param cause The cause.
    */
   public ExpectedNumberException(Throwable cause)
   {
      super(cause);
   }

   /**
    * Create a <code>ExpectedNumberException</code> with the given message.
    * @param message The message.
    * @param cause The cause.
    */
   public ExpectedNumberException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
