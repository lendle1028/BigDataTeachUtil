package net.sf.jagg.msd;

import java.util.List;

/**
 * This class holds the memory needed by a <code>Discriminator</code>.  This is
 * meant to be used by exactly one thread at a time.
 *
 * @author Randy Gettman
 * @since 0.5.0
 */
public class MsdWorkspace
{
   /**
    * The size of the array of lists and the array of used indexes.
    */
   public static final int SIZE = 65536;

   /**
    * The array of lists of elements.
    */
   public final List[] myLists = new List[SIZE];
   /**
    * The array of used indexes.
    */
   public final int[] myUsedIndexes = new int[SIZE];

}
