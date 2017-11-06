package net.sf.jagg.msd;

import java.util.List;

/**
 * A <code>Discriminable</code> object knows which properties in itself to
 * be discriminated.  
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public interface Discriminable
{
   /**
    * <p>Returns a <code>List</code> of properties on which to discriminate on
    * this object.  Such properties must map to an actual method name on the
    * object in the bean naming convention, e.g. a property of "value" maps to
    * a method "getValue()" that takes no arguments.  All properties must map
    * to methods whose returns types are themselves <code>Discriminable</code>,
    * or of a Java built-in type:</p>
    * <p>
    * <ul>
    * <li><code>Boolean</code>
    * <li><code>Character</code>
    * <li><code>CharSequence</code> (all implementing classes such as
    *    <code>String</code>, <code>CharBuffer</code>,
    *    <code>StringBuffer</code>, and <code>StringBuilder</code>)
    * <li><code>Number</code> (includes <code>BigDecimal</code>,
    *    <code>BigInteger</code>, <code>Byte</code>, <code>Double</code>,
    *    <code>Float</code>, <code>Integer</code>, <code>Long</code>, and
    *    <code>Short</code>)
    * <li>Any <code>Date</code>
    * <li>Any <code>Calendar</code>
    * <li>Any <code>Enum</code>
    * <li>Any <code>Collection</code>
    * <li>Any <code>List</code>
    * <li>Any primitive type: <code>boolean</code>, <code>char</code>,
    *    <code>byte</code>, <code>short</code>, <code>int</code>,
    *    <code>long</code>, <code>float</code>, <code>double</code>.
    * <li>An array of any above type.
    * </ul>
    * </p>
    * <p>If a <code>Discriminable</code> object is also
    * <code>Comparable</code>, then the properties listed here should also be
    * the same properties used in <code>compareTo</code>, for consistency.</p>
    * <p>The <code>List</code> of properties returned must be the same for any
    * and every instance of each <code>Discriminable</code> class.  For this
    * reason, it is recommended to return a <code>static final List</code>.</p>
    *
    * @return A <code>List</code> of properties.
    */
   public List<String> getDiscriminableProperties();
}
