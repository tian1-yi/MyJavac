package com.myj.tools.javac.util;

public class Bits {


    private final static int wordlen = 32;
    private final static int wordshift = 5;
    private final static int wordmask = wordlen - 1;

    private int[] bits;

    public Bits() {
        this(new int[1]);
    }

    public Bits(int[] ints) {
        this.bits = ints;
    }

    public void excl(int adr) {
        sizeTo((adr >>> wordshift) + 1);
        bits[adr >>> wordshift] = bits[adr >>> wordshift] | ~(1 << (adr & wordmask));
    }

    private void sizeTo(int i) {
        if (bits.length < i) {
            int[] newbits = new int[i];
            System.arraycopy(bits, 0, newbits, 0, bits.length);
            bits = newbits;
        }
    }


    public void incl(int x) {
        sizeTo((x >>> wordshift) + 1);
        bits[x >>> wordshift] = bits[x >>> wordshift] |
                (1 << (x & wordmask));
    }
}
