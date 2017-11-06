package net.sf.jagg.exception;

/**
 * An <code>ParseException</code> is a <code>JaggException</code> that
 * indicates that a specification that needed to be parsed was invalid.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class ParseException extends JaggException
{
   /**
    * Create a <code>ParseException</code>.
    */
   public ParseException()
   {
      super();
   }

   /**
    * Create a <code>ParseException</code> with the given message.
    * @param message The message.
    */
   public ParseException(String message)
   {
      super(message);
   }

   /**
    * Create a <code>ParseException</code>.
    * @param cause The cause.
    */
   public ParseException(Throwable cause)
   {
      super(cause);
   }

   /**
    * Create a <code>ParseException</code> with the given message.
    * @param message The message.
    * @param cause The cause.
    */
   public ParseException(String message, Throwable cause)
   {
      super(message, cause);
   }
}