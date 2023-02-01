package com.myj.tools.javac.util;

public class Convert {

    public static int string2int(String s, int radix) {
        if (radix == 10) {
            return Integer.parseInt(s, radix);
        } else {
            return radix;
        }
    }

    public static Name shortName(Name fullName) {
        return fullName.subName(fullName.lastIndexOf((byte) '.') + 1, fullName.getByteLength());
    }

    public static Name packagePart(Name fullName) {
        return fullName.subName(0, fullName.lastIndexOf((byte) '.'));

    }
}
