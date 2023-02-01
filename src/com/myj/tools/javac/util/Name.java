package com.myj.tools.javac.util;

public abstract class Name implements javax.lang.model.element.Name {


    public int len;

    public Names.Table table;
    public Name(Names.Table table) {
        this.table = table;
    }

    public abstract int getByteLength();

    public abstract int getIndex();

    public abstract byte[] getByteArray();

    public abstract int getByteOffset();

    @Override
    public String toString() {
        byte[] byteArray = getByteArray();
        return new String(byteArray, getByteOffset(), getByteLength());
    }

    public boolean isEmpty() {
        return getByteLength() == 0;
    }

    public Name append(char c, Name n) {
        int len = getByteLength();
        byte[] bs = new byte[len + 1 + n.getByteLength()];
        getBytes(bs, 0);
        bs[len] = (byte) c;
        n.getBytes(bs, len + 1);
        return table.fromUtf(bs, 0, bs.length);
    }

    public void getBytes(byte[] bs, int start) {
        System.arraycopy(getByteArray(), getByteOffset(), bs, start, getByteLength());
    }

    public int lastIndexOf(byte b) {
        byte[] bytes = getByteArray();

        int offset = getByteOffset();
        int i = getByteLength() - 1;
        while (i >= 0 && bytes[offset + i] != b) i --;
        return i;
    }

    public Name subName(int i, int byteLength) {
        if (byteLength < i) byteLength = i;
        return table.fromUtf(getByteArray(), getByteOffset() + i, byteLength - i);
    }

    public byte[] toUtf() {
        byte[] bs = new byte[getByteLength()];
        getBytes(bs, 0);
        return bs;
    }
}
