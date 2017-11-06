package net.sf.jagg.exception;

/**
 * An <code>AnalyticCreationException</code> is a <code>JaggException</code>
 * that indicates that there was a problem attempting to instantiate an
 * <code>AnalyticFunction</code>.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class AnalyticCreationException extends JaggException
{
   /**
    * Create a <code>AggregatorCreationException</code>.
    */
   public AnalyticCreationException()
   {
      super();
   }

   /**
    * Create a <code>AggregatorCreationException</code> with the given message.
    * @param message The message.
    */
   public AnalyticCreationException(String message)
   {
      super(message);
   }

   /**
    * Create a <code>AggregatorCreationException</code>.
    * @param cause The cause.
    */
   public AnalyticCreationException(Throwable cause)
   {
      super(cause);
   }

   /**
    * Create a <code>AggregatorCreationException</code> with the given message.
    * @param message The message.
    * @param cause The cause.
    */
   public AnalyticCreationException(String message, Throwable cause)
   {
      super(message, cause);
   }
}