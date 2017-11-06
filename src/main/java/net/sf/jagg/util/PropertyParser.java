package net.sf.jagg.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import net.sf.jagg.exception.ParseException;
import net.sf.jagg.util.PropertyScanner;

/**
 * This class knows how to parse a property specification:
 * <code>property[([param[, param]*])]</code>.
 * 
 * @author Randy Gettman
 * @since 0.1.0
 */
public class PropertyParser
{
   private static final boolean DEBUG = false;

   private String myPropertyText;
   private PropertyScanner myScanner;

   private String myPropertyName;
   private boolean amIMethod;
   private List<Object> myParameters = new ArrayList<Object>();
   private boolean amIFinished;

   private PropertyScanner.Token myNextToken;
   private String myNextLexeme;

   /**
    * Create a <code>PropertyParser</code>.
    */
   public PropertyParser()
   {
      setPropertyText("");
   }

   /**
    * Create a <code>PropertyParser</code> object that will parse the given property text.
    * @param propertyText The text of the property.
    */
   public PropertyParser(String propertyText)
   {
      setPropertyText(propertyText);
   }

   /**
    * Sets the property to the given property text and resets the parser.
    * @param propertyText The new property text.
    */
   public void setPropertyText(String propertyText)
   {
      myPropertyText = propertyText;
      reset();
   }

  /**
    * Resets this <code>PropertyParser</code>, usually at creation time and when new
    * input arrives.
    */
   private void reset()
   {
      myPropertyName = null;
      amIMethod = false;
      myParameters.clear();
      amIFinished = false;
      myNextToken = null;
      myNextLexeme = null;
      myScanner = new PropertyScanner(myPropertyText);
   }

   /**
    * Parses the property text.
    */
   public void parse()
   {
      amIFinished = false;
      myParameters.clear();

      PropertyScanner.Token token;
      if (myNextToken != null)
      {
         token = myNextToken;
         myNextToken = null;
         myNextLexeme = null;
      }
      else
         token = myScanner.getNextToken();

      if (token == PropertyScanner.Token.TOKEN_WHITESPACE)
         token = myScanner.getNextToken();
      if (token == PropertyScanner.Token.TOKEN_STRING)
      {
         token = parsePropertyOrMethod(myScanner);
      }
      else
      {
         throw new ParseException("Illegal token " + token + " start to property or method name: \"" + myPropertyText + "\".");
      }

      // Expect nothing to be beyond.
      if (token == PropertyScanner.Token.TOKEN_WHITESPACE)
         token = myScanner.getNextToken();

      // Allow a "." or a "[" which would begin another property.
      if (token == PropertyScanner.Token.TOKEN_PERIOD || token == PropertyScanner.Token.TOKEN_LEFT_BRACKET)
      {
         amIFinished = false;
         if (token == PropertyScanner.Token.TOKEN_PERIOD)
         {
            // Pass up ".".
            token = myScanner.getNextToken();
         }
      }
      else if (token != PropertyScanner.Token.TOKEN_EOI)
      {
         throw new ParseException("Extra token " + token + " found after ')': \"" + myPropertyText + "\".");
      }
      else
      {
         // EOI
         amIFinished = true;
      }

      myNextToken = token;
      myNextLexeme = myScanner.getCurrLexeme();
   }

   /**
    * Parses a property or a method using the given <code>PropertyScanner</code>.
    * This method used to be part of <code>parse</code>.
    * @param scanner A <code>PropertyScanner</code>.
    * @return The next <code>Token</code>.
    * @since 0.8.0
    */
   private PropertyScanner.Token parsePropertyOrMethod(PropertyScanner scanner)
   {
      String lexeme = "";
      boolean wasQuoted = false;

      if (myNextLexeme != null)
      {
         myPropertyName = myNextLexeme;
         myNextLexeme = null;
      }
      else
         myPropertyName = scanner.getCurrLexeme();

      PropertyScanner.Token token = scanner.getNextToken();
      if (token == PropertyScanner.Token.TOKEN_WHITESPACE)
         token = scanner.getNextToken();
      // EOI or left bracket or period = end of property.
      if (token == PropertyScanner.Token.TOKEN_EOI ||
          token == PropertyScanner.Token.TOKEN_LEFT_BRACKET ||
          token == PropertyScanner.Token.TOKEN_PERIOD)
      {
         // Property name.  No parameters.
         amIMethod = false;
         return token;
      }
      if (token != PropertyScanner.Token.TOKEN_LEFT_PAREN)
      {
         throw new ParseException("Method name must start with '(': \"" +
            myPropertyText + "\".");
      }
      // If we made it here, then we found a left parenthesis.  Set method to true.
      amIMethod = true;
      
      token = scanner.getNextToken();
      while (token.getCode() >= 0 && token != PropertyScanner.Token.TOKEN_RIGHT_PAREN)
      {
         switch (token)
         {
         case TOKEN_WHITESPACE:
            // Ignore.
            break;
         case TOKEN_STRING:
            // String.
            lexeme = scanner.getCurrLexeme();
            break;
         case TOKEN_COMMA:
            // Delimits parameters.
            if (wasQuoted || lexeme.length() > 0)
               myParameters.add(getTypedParameter(lexeme, wasQuoted));
            wasQuoted = false;
            break;
         case TOKEN_DOUBLE_QUOTE:
            // Double quote.
            wasQuoted = true;
            break;
         case TOKEN_SINGLE_QUOTE:
            // Single quote.
            wasQuoted = true;
            break;
         case TOKEN_EOI:
            throw new ParseException("'(' found without ')': \"" + myPropertyText + "\".");
         default:
            throw new ParseException("Parse error occurred: token " + token + " found: \"" + myPropertyText + "\".");
         }
         token = scanner.getNextToken();
      }

      if (token.getCode() < 0)
         throw new ParseException("A parse error occured: \"" + myPropertyText + "\".");

      // Last parameter before ")".
      if (wasQuoted || lexeme.length() > 0)
      {
         Object value = getTypedParameter(lexeme, wasQuoted);
         if (DEBUG)
            System.err.println("  value's class is \"" + ((value != null) ? value.getClass().getName() : "(null)") + "\".");
         myParameters.add(value);
      }

      token = scanner.getNextToken();
      return token;
   }

   /**
    * Attempts to determine the intended type of the input, and return it as an
    * instance of that type.  Enumerations are supported with the format
    * <code>class:name</code>.  This was made <code>public static</code> for
    * jAgg 0.9.0.
    * @param parameter The parameter's string value.
    * @param wasQuoted Whether the string value was quoted.
    * @return An object of that type.
    */
   @SuppressWarnings("unchecked")
   public static Object getTypedParameter(String parameter, boolean wasQuoted)
   {
      if (DEBUG)
         System.err.println("  fTP: \"" + parameter + "\", quoted: " + wasQuoted);
      Object result;
      if (wasQuoted)
      {
         if (DEBUG)
            System.err.println("  Quoted: String");
         // Null/empty, but quoted: empty string,
         // else force string.
         if (parameter == null || parameter.length() == 0)
            return "";
         else
            return parameter;
      }
      if (parameter.matches(".*[A-Za-z]+.*"))
      {
         // If there is a non-number, non-whitespace character...
         if (DEBUG)
            System.err.println("  Letters found => String");
         // Could be a boolean literal.
         if ("true".equalsIgnoreCase(parameter))
            return true;
         else if ("false".equalsIgnoreCase(parameter))
            return false;
         else if ("null".equalsIgnoreCase(parameter))
            return null;
         else if (parameter.contains(":"))
         {
            int index = parameter.indexOf(":");
            String className = parameter.substring(0, index);
            String enumName = parameter.substring(index + 1);
            try
            {
               if (DEBUG)
                  System.err.println("    Enum parsing: class: \"" + className + "\", name: \"" +
                     enumName + "\".");
               Class<? extends Enum> enumClass = (Class<? extends Enum>) Class.forName(className);
               return Enum.valueOf(enumClass, enumName);
            }
            catch (ClassNotFoundException e)
            {
               throw new ParseException("Unrecognized Enum: \"" + className + "\"", e);
            }
            catch (ClassCastException e)
            {
               throw new ParseException("Not an Enum: \"" + className + "\"", e);
            }
         }
         // Else it's a String.
         return parameter;
      }
      else if (parameter.matches("[-]?[0-9]+\\.[0-9]*"))
      {
         if (DEBUG)
            System.err.println("  Numbers and decimal point => Float/Double/BigDecimal");

         // Don't even try it as a float.  The MethodCache will create a float
         // if only a method with a float is found.
         try
         {
            result = Double.valueOf(parameter);
            return result;
         }
         catch (NumberFormatException ignored) {}
         try
         {
            result = new BigDecimal(parameter);
            return result;
         }
         catch (NumberFormatException ignored) {}
         return parameter;
      }
      else if (parameter.matches("[-]?[0-9]+"))
      {
         if (DEBUG)
            System.err.println("  Numbers => Byte/Short/Integer/Long/BigInteger");
         try
         {
            result = Byte.valueOf(parameter);
            return result;
         }
         catch (NumberFormatException ignored) {}
         try
         {
            result = Short.valueOf(parameter);
            return result;
         }
         catch (NumberFormatException ignored) {}
         try
         {
            result = Integer.valueOf(parameter);
            return result;
         }
         catch (NumberFormatException ignored) {}
         try
         {
            result = Long.valueOf(parameter);
            return result;
         }
         catch (NumberFormatException ignored) {}
         try
         {
            result = new BigInteger(parameter);
            return result;
         }
         catch (NumberFormatException ignored) {}
         return parameter;
      }
      else
      {
         if (DEBUG)
            System.err.println("  Failed other attempts, falling back on String");
         return parameter;
      }
   }

   /**
    * Returns the property or method name.
    * @return The property or method name.
    */
   public String getPropertyName()
   {
      return myPropertyName;
   }

   /**
    * Returns whether this property text represents a method (with parentheses)
    * or a simple property name (without parentheses).
    * @return <code>true</code> if it represents a method, <code>false</code>
    *    if it represents a simple property name.
    */
   public boolean isMethod()
   {
      return amIMethod;
   }

   /**
    * Returns the <code>List</code> of parameters (possibly empty), or
    * <code>null</code> if this is a simple property name.
    * @return A <code>List</code> of parameters (possibly empty), or
    *    <code>null</code> if this is a simple property name.
    */
   public List<Object> getParameters()
   {
      if (!amIMethod)
         return null;
      return myParameters;
   }

   /**
    * Returns whether this parser has finished reading all input.
    * A <code>false</code> means that there is at least one more property to
    * read.
    * @return Whether this parser has finished reading all input.
    * @since 0.8.0
    */
   public boolean isFinished()
   {
      return amIFinished;
   }
}
