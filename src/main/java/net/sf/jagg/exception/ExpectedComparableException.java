package net.sf.jagg.exception;

/**
 * An <code>ExpectedComparableException</code> is a <code>JaggException</code>
 * that indicates that a data value was expected to be a
 * <code>Comparable</code>, but it wasn't.
 *
 * @author Randy Gettman
 * @since 0.9.0
 */
public class ExpectedComparableException extends JaggException
{
   /**
    * Create a <code>ExpectedComparableException</code>.
    */
   public ExpectedComparableException()
   {
      super();
   }

   /**
    * Create a <code>ExpectedComparableException</code> with the given message.
    * @param message The message.
    */
   public ExpectedComparableException(String message)
   {
      super(message);
   }

   /**
    * Create a <code>ExpectedComparableException</code>.
    * @param cause The cause.
    */
   public ExpectedComparableException(Throwable cause)
   {
      super(cause);
   }

   /**
    * Create a <code>ExpectedComparableException</code> with the given message.
    * @param message The message.
    * @param cause The cause.
    */
   public ExpectedComparableException(String message, Throwable cause)
   {
      super(message, cause);
   }
}