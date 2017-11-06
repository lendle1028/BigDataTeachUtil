package net.sf.jagg.math;

/**
 * A <code>DoubleDouble</code> is used when extra precision is necessary to
 * cut way down on floating point errors.
 *
 * @author Randy Gettman
 * @since 0.4.0
 */
public strictfp class DoubleDouble implements Comparable<DoubleDouble>
{
   /**
    * The <code>DoubleDouble</code> <code>NaN</code> (Not a Number), immutable.
    */
   public static final DoubleDouble NaN = new ImmutableDoubleDouble(Double.NaN, 0);
   /**
    * The <code>DoubleDouble</code> zero, immutable.
    */
   public static final DoubleDouble ZERO = new ImmutableDoubleDouble();
   
   /**
    * Used to "split" a <code>double</code> into two parts, each with 26 bits.
    */
   private static final double SPLIT = 134217729.0;  // 2 to the 27th + 1

   private double myHigh;
   private double myLow;

   /**
    * Create a <code>DoubleDouble</code>, initialized to zero.
    */
   public DoubleDouble()
   {
      myHigh = 0;
      myLow = 0;
   }

   /**
    * Create a <code>DoubleDouble</code> from a <code>double</code>.
    * @param d A <code>double</code>.
    */
   public DoubleDouble(double d)
   {
      myHigh = d;
      myLow = 0;
   }

   /**
    * Create a <code>DoubleDouble</code> from high and low parts.
    * @param hi The high-order part.
    * @param lo The low-order part.
    */
   public DoubleDouble(double hi, double lo)
   {
      normalize(hi, lo, 0);
   }

   /**
    * Copy constructor.
    * @param dd Another <code>DoubleDouble</code>.
    */
   public DoubleDouble(DoubleDouble dd)
   {
      myHigh = dd.myHigh;
      myLow = dd.myLow;
   }

   /**
    * Sets this <code>DoubleDouble</code> equal to zero.
    */
   public void reset()
   {
      myHigh = 0;
      myLow = 0;
   }

   /**
    * Returns the <code>double</code> that is closest in value to this
    * <code>DoubleDouble</code>.
    * @return A <code>double</code> (the high portion of this
    *    <code>DoubleDouble</code>).
    */
   public double doubleValue()
   {
      return myHigh;
   }

   /**
    * Returns the low-order portion of this <code>DoubleDouble</code>.
    * @return The low-order portion of this <code>DoubleDouble</code>.
    */
   public double getLow()
   {
      return myLow;
   }

   /**
    * Returns whether this <code>DoubleDouble</code> is NaN.
    * @return Whether this <code>DoubleDouble</code> is NaN.
    */
   public boolean isNaN()
   {
      return Double.isNaN(myHigh);
   }

   /**
    * Adds another <code>DoubleDouble</code> to this one.
    * @param dd Another <code>DoubleDouble</code>.
    */
   public void addToSelf(DoubleDouble dd)
   {
      if (isNaN())
         return;
      // Algorithm is based on "Algorithms for Quad-Double Precision Floating
      // Point Arithmetic" by Hida, Li, and Bailey, 2000, Berkeley.
      double e, e2, e3, f, s0, s1, v;
      // Two Sum: "a" is myHigh, "b" is dd.myHigh, "s" is s0, "e" is e.
      s0 = myHigh + dd.myHigh;
      v = s0 - myHigh;
      e = (myHigh - (s0 - v)) + (dd.myHigh - v);
      // Two Sum: "a" is myLow, "b" is dd.myLow, "s" is f, "e" is e2.
      f = myLow + dd.myLow;
      v = f - myLow;
      e2 = (myLow - (f - v)) + (dd.myLow - v);
      // Two Sum: "a" is f, "b" is e, "s" is s1, "e" is e3.
      s1 = f + e;
      v = s1 - f;
      e3 = (f - (s1 - v)) + (e - v);
      // Straight sum: e is reused.
      e = e2 + e3;

      normalize(s0, s1, e);
   }

   /**
    * Adds a <code>double</code> to this.  Algorithm is based on "Algorithms
    * for Quad-Double Precision Floating Point Arithmetic" by Hida, Li, and
    * Bailey, 2000, Berkeley.
    * @param d A <code>double</code>.
    */
   public void addToSelf(double d)
   {
      if (isNaN())
         return;
      // Algorithm is based on "Algorithms for Quad-Double Precision Floating
      // Point Arithmetic" by Hida, Li, and Bailey, 2000, Berkeley.
      double e, s0, s1, v;
      // Two Sum: "a" is myHigh, "b" is d, "s" is s0, "e" is e.
      s0 = myHigh + d;
      v = s0 - myHigh;
      e = (myHigh - (s0 - v)) + (d - v);
      // Two Sum: "a" is myLow, "b" is e, "s" is s1, "e" is e.
      s1 = myLow + e;
      v = s1 - myLow;
      e = (myLow - (s1 - v)) + (e - v);

      normalize(s0, s1, e);
   }

   /**
    * Subtracts another <code>DoubleDouble</code> from this one.
    * @param dd Another <code>DoubleDouble</code>.
    */
   public void subtractFromSelf(DoubleDouble dd)
   {
      DoubleDouble negative = new DoubleDouble(dd);
      negative.negateSelf();
      addToSelf(negative);
   }

   /**
    * Subtracts a <code>double</code> from this.
    * @param d A <code>double</code>.
    */
   public void subtractFromSelf(double d)
   {
      addToSelf(-d);
   }

   /**
    * Negate each part.  NaNs aren't negated (or negatable).
    * The negation of NaN is NaN.
    */
   public void negateSelf()
   {
      myHigh = -myHigh;
      myLow = -myLow;
   }

   /**
    * Multiplies self by another <code>DoubleDouble</code>.
    * @param dd Another <code>DoubleDouble</code>.
    */
   public void multiplySelfBy(DoubleDouble dd)
   {
      if (isNaN())
         return;
      double ah, al, bh, bl, ch, cl, e0, e1, e2, p0, p1, p2, v;
      // Algorithm is based on "Algorithms for Quad-Double Precision Floating
      // Point Arithmetic" by Hida, Li, and Bailey, 2000, Berkeley.
      // Two Prod: "a" is myHigh, "b" is dd.myHigh, "s" is p0, "e" is e0.
      p0 = myHigh * dd.myHigh;
      // Split: "a" is myHigh.
      v = SPLIT * myHigh;
      ah = v - (v - myHigh);
      al = myHigh - ah;
      // Split: "a" is dd.myHigh.
      v = SPLIT * dd.myHigh;
      bh = v - (v - dd.myHigh);
      bl = dd.myHigh - bh;
      e0 = (((ah * bh - p0) + ah * bl) + al * bh) + al * bl;

      // Two Prod: "a" is myHigh, "b" is dd.myLow, "s" is p1, "e" is e1.
      p1 = myHigh * dd.myLow;
      // Store the splits from dd.myHigh in (ch, cl) to avoid recomputing them
      // later.
      ch = bh;
      cl = bl;
      // Split with myHigh is still in (ah, al).
      // Split: "a" is dd.myLow.
      v = SPLIT * dd.myLow;
      bh = v - (v - dd.myLow);
      bl = dd.myLow - bh;
      e1 = (((ah * bh - p1) + ah * bl) + al * bh) + al * bl;

      // Two Prod: "a" is myLow, "b" is dd.myHigh, "s" is p2, "e" is e2.
      p2 = myLow * dd.myHigh;
      // Split: "a" is myLow.
      v = SPLIT * myLow;
      ah = v - (v - myLow);
      al = myLow - ah;
      // Split with dd.myHigh still in (ch, cl).
      e2 = (((ah * ch - p2) + ah * cl) + al * ch) + al * cl;
      // Note: ah, al, bh, bl, ch, cl can now be reused.

      // low-order terms
      // Three Sum: "x" is e0, "y" is p1, "z" is p2, "r0" is p1 (reused), "r1" is e0 (reused).
      // Two Sum: "a" is e0, "b" is p1, "s" is bh, "e" is bl.
      bh = e0 + p1;
      v = (bh - e0);
      bl = (e0 - (bh - v)) + (p1 - v);
      // Note: e0, p0, and p1 can now be reused.
      // Two Sum: "a" is bh, "b" is p2, "s" is p1, "e" is e0.
      p1 = bh + p2;
      v = (p1 - bh);
      e0 = (bh - (p1 - v)) + (p2 - v);
      // Use normal addition here.  Gather the othe error in the three sum
      // (bl), then add the lower-order terms.  This includes the
      // multiplication of the low-order terms, which can also be done in
      // normal multiplication.
      e0 += bl + (myLow * dd.myLow) + e1 + e2;

      normalize(p0, p1, e0);
   }

   /**
    * Multiplies self by a <code>double</code>.
    * @param d A <code>double</code>.
    */
   public void multiplySelfBy(double d)
   {
      if (isNaN())
         return;
      // Algorithm is based on "Algorithms for Quad-Double Precision Floating
      // Point Arithmetic" by Hida, Li, and Bailey, 2000, Berkeley.
      double ah, al, bh, bl, e, e2, f, p0, p1, v;
      // Two Prod: "a" is myHigh, "b" is d, "p" is p0, "e" is e.
      p0 = myHigh * d;
      // Split: "a" is myHigh.
      v = SPLIT * myHigh;
      ah = v - (v - myHigh);
      al = myHigh - ah;
      // Split: "a" is d.
      v = SPLIT * d;
      bh = v - (v - d);
      bl = d - bh;
      e = (((ah * bh - p0) + ah * bl) + al * bh) + al * bl;
      // Two Prod: "a" is myLow, "b" is d, "p" is f, "e" is e2.
      f = myLow * d;
      // Split: "a" is myLow.
      v = SPLIT * myLow;
      ah = v - (v - myLow);
      al = myLow - ah;
      // Split: The splits from d are still in (bh, bl).
      e2 = (((ah * bh - f) + ah * bl) + al * bh) + al * bl;
      // Two Sum: "a" is e, "b" is f, "s" is p1, "e" is e (reused).
      p1 = e + f;
      v = p1 - e;
      e = (e - (p1 - v)) + (f - v);
      // Normal add
      e += e2;

      normalize(p0, p1, e);
   }

   /**
    * Squares self.
    */
   public void squareSelf()
   {
      if (isNaN())
         return;
      double ah, al, bh, bl, e0, e1, p0, p1, v;
      // Algorithm is based on "Algorithms for Quad-Double Precision Floating
      // Point Arithmetic" by Hida, Li, and Bailey, 2000, Berkeley.
      // Two Prod: "a" is myHigh, "b" is myHigh, "s" is p0, "e" is e0.
      p0 = myHigh * myHigh;
      // Split: "a" is myHigh.
      v = SPLIT * myHigh;
      ah = v - (v - myHigh);
      al = myHigh - ah;
      e0 = (((ah * ah - p0) + ah * al) + al * ah) + al * al;

      // Two Prod: "a" is myHigh, "b" is myLow, "s" is p1, "e" is e1.
      p1 = myHigh * myLow;
      // Split with myHigh is still in (ah, al).
      // Split: "a" is myLow.
      v = SPLIT * myLow;
      bh = v - (v - myLow);
      bl = myLow - bh;
      e1 = (((ah * bh - p1) + ah * bl) + al * bh) + al * bl;

      // The high-low term is doubled.
      p1 *= 2;
      e1 *= 2;

      // low-order terms
      // Two Sum: "a" is e0, "b" is p1, "s" is bh (reused), "e" is bl (reused).
      bh = e0 + p1;
      v = (bh - e0);
      bl = (e0 - (bh - v)) + (p1 - v);
      // Use normal addition here.  This includes the multiplication of the
      // low-order terms, which can also be done in normal multiplication.
      bl += (myLow * myLow) + e1;

      normalize(p0, bh, bl);
   }

   /**
    * Divides self by a <code>DoubleDouble</code>.
    * @param dd Another <code>DoubleDouble</code>.
    */
   public void divideSelfBy(DoubleDouble dd)
   {
      // Karp's method for High-Precision Division.
      double x, y;
      DoubleDouble r;
      x = 1.0 / dd.myHigh;
      y = myHigh * x;
      r = new DoubleDouble(dd);
      r.multiplySelfBy(y);
      r.negateSelf();
      r.addToSelf(this);
      r.multiplySelfBy(x);
      r.addToSelf(y);

      myHigh = r.myHigh;
      myLow = r.myLow;
   }

   /**
    * Divides self by a <code>double</code>.
    * @param d A <code>double</code>.
    */
   public void divideSelfBy(double d)
   {
      // Karp's method for High-Precision Division.
      double x, y;
      DoubleDouble r;
      x = 1.0 / d;
      y = myHigh * x;
      r = new DoubleDouble(d);
      r.multiplySelfBy(y);
      r.negateSelf();
      r.addToSelf(this);
      r.multiplySelfBy(x);
      r.addToSelf(y);

      myHigh = r.myHigh;
      myLow = r.myLow;
   }

   /**
    * Takes the square root of self.
    */
   public void sqrtSelf()
   {
      // Karp's method for High-Precision Square Root.
      double x, y;
      DoubleDouble r;
      x = 1.0 / Math.sqrt(myHigh);
      y = myHigh * x;
      r = new DoubleDouble(y);
      r.squareSelf();
      r.negateSelf();
      r.addToSelf(this);
      r.multiplySelfBy(x);
      r.divideSelfBy(2.0);
      r.addToSelf(y);

      myHigh = r.myHigh;
      myLow = r.myLow;
   }

   /**
    * Raise self to an integer exponent.
    * @param exponent The exponent.
    */
   public void powSelf(long exponent)
   {
      if (isNaN())
         return;
      if (exponent == 0)
      {
         if (myHigh == 0)
         {
            // 0^0 = NaN
            myHigh = Double.NaN;
            myLow = 0;
         }
         else
         {
            // x^0 = 1
            myHigh = 1;
            myLow = 0;
         }
      }
      // 0^y = 0, 1^y = 1.
      if (myHigh == 0 || (myHigh == 1 && myLow == 0))
         return;
      
      boolean invert = exponent < 0;
      exponent = Math.abs(exponent);

      if (exponent == 2)
         squareSelf();
      else if (exponent > 2)
      {
         // Exponentiation by repeated squaring.
         DoubleDouble result = new DoubleDouble(1.0);
         DoubleDouble square = new DoubleDouble(this);
         while(exponent >= 1)
         {
            if ((exponent & 1) == 1) // if odd
               result.multiplySelfBy(square);
            square.squareSelf();
            exponent >>>= 1;  // int divide by 2
         }
         myHigh = result.myHigh;
         myLow = result.myLow;
      }

      if (invert)
      {
         DoubleDouble reciprocal = new DoubleDouble(1.0);
         reciprocal.divideSelfBy(this);
         myHigh = reciprocal.myHigh;
         myLow = reciprocal.myLow;
      }
   }

   /**
    * Takes the <em>n</em>th root of self.
    * @param n The root.
    */
   public void nthRootSelf(long n)
   {
      // Modified Karp's method.
      double x, y;
      DoubleDouble r;
      x = Math.pow(myHigh, (1.0 - n) / n);
      y = myHigh * x;
      r = new DoubleDouble(y);
      r.powSelf(n);
      r.negateSelf();
      r.addToSelf(this);
      r.multiplySelfBy(x);
      r.divideSelfBy(n);
      r.addToSelf(y);

      myHigh = r.myHigh;
      myLow = r.myLow;
   }

   /**
    * Normalize this <code>Double</code> following an arithmetic computation.
    * @param s0 The high order term.
    * @param s1 The low order term.
    * @param e The error.
    */
   private void normalize(double s0, double s1, double e)
   {
      double t0, t1, t2, v;
      int k = 0;
      // Normalize: "a0" is s0, "a1" is s1, "a2" is e.
      // Quick Two Sum: "a" is s1, "b" is e, "s" is v (reused variable), "e" is t2.
      v = s1 + e;
      t2 = e - (v - s1);
      // Quick Two Sum: "a" is s0, "b" is v, "s" is t0, "e" is t1.
      t0 = s0 + v;
      t1 = v - (t0 - s0);

      myHigh = myLow = 0;
      // Quick Two Sum: "a" is t0, "b" is t1, "s" is v (reused variable), "e" is e.
      v = t0 + t1;
      e = t1 - (v - t0);

      if (v != 0)
      {
         myHigh = v;
         myLow = 0;
         v = e;
         k++;
      }
      // Quick Two Sum: "a" is v, "b" is t2, "s" is s0 (reused variable), "e" is e.
      s0 = v + t2;
      // Calculation of e only if needed later (in myLow if k == 0).
      if (s0 != 0)
      {
         if (k == 0)
         {
            myHigh = s0;
            myLow = t2 - (s0 - v);
         }
         else
         {
            myLow = s0;
         }
      }
   }

   /**
    * Returns an integer less than zero, equal to zero, or greater than zero,
    * depending on whether this compares less than, equal to, or greather than
    * another <code>DoubleDouble</code>.
    * @param other Another <code>DoubleDouble</code>.
    * @return An integer less than zero, equal to zero, or greater than zero,
    *    depending on whether this compares less than, equal to, or greather
    *    than another <code>DoubleDouble</code>.
    */
   public int compareTo(DoubleDouble other)
   {
      if (myHigh < other.myHigh)
         return -1;
      if (myHigh > other.myHigh)
         return 1;
      if (myLow < other.myLow)
         return -1;
      if (myLow > other.myLow)
         return 1;
      return 0;
   }

   /**
    * Used to initialize constants.  All operations that change the contents
    * throw <code>UnsupportedOperationException</code> to prevent it.
    */
   private static strictfp class ImmutableDoubleDouble extends DoubleDouble
   {
      /**
       * Effectively creates an <code>ImmutableDoubleDouble</code> constant for
       * zero.
       */
      public ImmutableDoubleDouble()
      {
         super(0, 0);
      }

      /**
       * Construct an <code>ImmutableDoubleDouble</code>.
       * @param hi The high-order part.
       * @param lo The low-order part.
       */
      public ImmutableDoubleDouble(double hi, double lo)
      {
         super(hi, lo);
      }

      public void addToSelf(DoubleDouble dd) { notSupported(); }
      public void addToSelf(double d) { notSupported(); }
      public void divideSelfBy(DoubleDouble dd) { notSupported(); }
      public void divideSelfBy(double d) { notSupported(); }
      public void multiplySelfBy(DoubleDouble dd) { notSupported(); }
      public void multiplySelfBy(double d) { notSupported(); }
      public void negateSelf() { notSupported(); }
      public void nthRootSelf(long n) { notSupported(); }
      public void powSelf(long n) { notSupported(); }
      public void reset() { notSupported(); }
      public void sqrtSelf() { notSupported(); }
      public void squareSelf() { notSupported(); }
      public void subtractFromSelf(DoubleDouble dd) { notSupported(); }
      public void subtractFromSelf(double d) { notSupported(); }

      private void notSupported()
      {
         throw new UnsupportedOperationException("Can't modify constant DoubleDouble value.");
      }
   }
}
