package com.myj.tools.javac.util;

public class SharedTable extends Names.Table {


    public NameImpl[] names;

    public byte[] bytes;

    public int nc = 0;

    public int hashSize;

    public SharedTable(Names names) {
        super(names);
        this.hashSize = 0x8000 -1;
        this.names = new NameImpl[0x8000];
        this.bytes = new byte[0x20000];

    }

    @Override
    public Name fromChars(char[] cs, int start, int len) {

        int nc = this.nc;
        byte[] bytes = this.bytes;
        while (nc + len * 3 >= bytes.length) {
            byte[] newArray = new byte[bytes.length * 2];
            System.arraycopy(bytes, 0, newArray, 0, bytes.length);
            bytes = this.bytes = newArray;
        }

        int i = charBuf(bytes, nc, cs, start, len);

        int hash = hashValue(bytes, nc, len) & hashSize;
        NameImpl name = names[hash];
        while (name != null && (
                len != name.len || !equals(bytes, nc, bytes, name.index, len)
                )) {
            name = name.next;
        }

        if (null == name) {
            name = new NameImpl(this);
            name.index = nc;
            name.len = i;
            name.next = names[hash];
            names[hash] = name;
            this.nc = nc + len;
            if (len == 0) {
                this.nc ++;
            }
        }

        return name;
    }

    /**
     * 判断两数组中是否一样
     * @param bytes 数组1
     * @param nc 数组1起始位置
     * @param bytes1 数组2
     * @param index 数组2起始位置
     * @param len 长度
     * @return
     */
    private boolean equals(byte[] bytes, int nc, byte[] bytes1, int index, int len) {
        int i = 0;
        while (i < len && bytes[nc + i] == bytes1[index + i]) {
            i ++;
        }
        return i == len;
    }

    private int charBuf(byte[] bytes, int nc, char[] cs, int start, int len) {
        int index = nc;
        int i = 0;
        while (i < len) {
            char c = cs[i];
            bytes[index ++] = (byte) c;
            i ++;
        }
        return len;
    }

    public Name fromUtf(byte[] bs, int i, int length) {
        int h = hashValue(bs, i, length) & hashSize;
        NameImpl name = names[h];
        byte[] names = this.bytes;
        while (name != null && (name.getByteLength() != length || !equals(names, name.index, bs, i, length))) {
            name = name.next;
        }
        if (name == null) {
            int nc = this.nc;
            while (nc + length > names.length) {
                byte[] newArray = new byte[names.length * 2];
                System.arraycopy(names, 0, newArray, 0, bytes.length);
                names = this.bytes = newArray;
            }
            System.arraycopy(bs, i, names, nc, length);
            name = new NameImpl(this);
            name.index = nc;
            name.len = length;
            name.next = this.names[h];
            this.names[h] = name;
            this.nc = nc + length;
            if (length == 0) {
                this.nc ++;
            }
        }
        return name;
    }


    protected static int hashValue(byte bytes[], int offset, int length) {
        int h = 0;
        int off = offset;

        for (int i = 0; i < length; i++) {
            h = (h << 5) - h + bytes[off++];
        }
        return h;
    }

    public class NameImpl extends Name {
        /**
         * 链表结构
         * 下一个
         */
        public NameImpl next;

        /**
         * 起始索引
         */
        public int index;

        /**
         * 长度
         */
        public int len;

        public NameImpl(SharedTable sharedTable) {
            super(sharedTable);
        }

        @Override
        public int getByteLength() {
            return len;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public byte[] getByteArray() {
            return ((SharedTable) table).bytes;
        }

        @Override
        public int getByteOffset() {
            return index;
        }


        @Override
        public boolean contentEquals(CharSequence cs) {
            return false;
        }

        @Override
        public int length() {
            return toString().length();
        }

        @Override
        public char charAt(int index) {
            return toString().charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return toString().subSequence(start, end);
        }


        @Override
        public int hashCode() {
            return index;
        }
    }


}
