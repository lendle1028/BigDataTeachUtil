package net.sf.jagg.exception;

/**
 * An <code>AggregatorCreationException</code> is a <code>JaggException</code>
 * that indicates that there was a problem attempting to instantiate an
 * <code>AggregateFunction</code>.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class AggregatorCreationException extends JaggException
{
   /**
    * Create a <code>AggregatorCreationException</code>.
    */
   public AggregatorCreationException()
   {
      super();
   }

   /**
    * Create a <code>AggregatorCreationException</code> with the given message.
    * @param message The message.
    */
   public AggregatorCreationException(String message)
   {
      super(message);
   }

   /**
    * Create a <code>AggregatorCreationException</code>.
    * @param cause The cause.
    */
   public AggregatorCreationException(Throwable cause)
   {
      super(cause);
   }

   /**
    * Create a <code>AggregatorCreationException</code> with the given message.
    * @param message The message.
    * @param cause The cause.
    */
   public AggregatorCreationException(String message, Throwable cause)
   {
      super(message, cause);
   }
}