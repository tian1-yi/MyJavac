package com.myj.tools.javac.jvm;

import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.code.Type;
import com.myj.tools.javac.util.Symtab;
import com.myj.tools.javac.util.Types;

import java.lang.management.MemoryType;
import java.util.Properties;

import static com.myj.tools.javac.code.Flags.INTERFACE;
import static com.myj.tools.javac.jvm.ByteCodes.*;

public class Items {

    int INTcode         = 0,
            LONGcode        = 1,
            FLOATcode       = 2,
            DOUBLEcode      = 3,
            OBJECTcode      = 4,
            BYTEcode        = 5,
            CHARcode        = 6,
            SHORTcode       = 7,
            VOIDcode        = 8,
            TypeCodeCount   = 9;

    Pool pool;

    Code code;

    Symtab syms;

    Types types;

    final Item voidItem;

    final Item thisItem;

    final Item superItem;

    final Item[] stackeItem = new Item[TypeCodeCount];

    public Items(Pool pool, Code code, Symtab syms, Types types) {
        this.pool = pool;
        this.code = code;
        this.syms = syms;
        this.types = types;

        voidItem = new Item(VOIDcode) {
            @Override
            public String toString() {
                return "void";
            }
        };
        this.thisItem = new SelfItem(false);
        this.superItem = new SelfItem(true);

        for (int i = 0; i < VOIDcode; i ++) stackeItem[i] = new StackItem(i);
        stackeItem[VOIDcode] = voidItem;
    }

    public Item makeImmediateItem(Type type, Object constValue) {
        return new ImmediateItem(type, constValue);
    }

    public LocalItem makeLocalItem(Symbol.VarSymbol v) {
        return new LocalItem(v.enasure(types), v.adr);
    }

    public Item makeThisItem() {
        return null;
    }

    public Item makeSuperItem() {
        return superItem;
    }

    public Item makeMemberItem(Symbol sym, boolean b) {
        return new MemberItem(sym, b);
    }

    public Item makeAssignItem(Item l) {
        return new AssignItem(l);
    }

    public Item makeStackItem(Type restType) {
        return stackeItem[Code.typeCode(restType)];
    }

    public Item makeStaticItem(Symbol member) {
        return new StaticItem(member);
    }



    abstract class Item {

        int typecode;

        public Item(int typecode) {
            this.typecode = typecode;
        }

        Item load() { return null; }

        public Item coerce(Type pt) {
            return coerce(Code.typeCode(pt));
        }
        Item coerce(int targetCode) {
            if (typecode == targetCode) {
                return this;
            } else {
                load();
                return null;
            }
        }

        public void store() {
        }

        public Item invoke() {
            return null;
        }

        public void drop() {
        }
    }

    class MemberItem extends Item{

        Symbol member;

        boolean nonvirtual;

        MemberItem(Symbol member, boolean nonvirtual) {
            super(Code.typeCode(member.enasure(types)));
            this.member = member;
            this.nonvirtual = nonvirtual;
        }

        @Override
        Item load() {
            code.emitop2(getfield, pool.put(member));
            return stackeItem[typecode];
        }

        @Override
        public Item invoke() {
            Type.MethodType mType = (Type.MethodType) member.externalType(types);
            int rescode = Code.typeCode(mType.restType);

            if ((member.owner.flags() & INTERFACE) != 0) {

            } else if (nonvirtual) {
                code.emitInvokespecial(pool.put(member), mType);
            }
            return stackeItem[rescode];
        }
    }

    class AssignItem extends Item {
        Item lhs;

        AssignItem(Item lhs) {
            super(lhs.typecode);
            this.lhs = lhs;
        }

        Item load() {

            lhs.store();
            return stackeItem[typecode];
        }

        @Override
        public void drop() {
            lhs.store();
        }
    }

    class SelfItem extends Item {

        boolean isSuper;

        public SelfItem(boolean isSuper) {
            super(OBJECTcode);
            this.isSuper = isSuper;
        }

        @Override
        Item load() {
            code.emitop0(aload_0);
            return stackeItem[typecode];
        }

        @Override
        public String toString() {
            return isSuper ? "super" : "this";
        }
    }

    class StackItem extends Item {

        StackItem(int typecode) {
            super(typecode);
        }

        @Override
        Item load() {
            return this;
        }
    }

    class ImmediateItem extends Item {
        Object value;

        ImmediateItem(Type type, Object value) {
            super(Code.typeCode(type));
            this.value = value;
        }

        @Override
        Item load() {
            switch (typecode) {
                case ByteCodes.INTcode: case ByteCodes.BYTEcode: case ByteCodes.SHORTcode: case ByteCodes.CHARcode:
                    int ival = ((Number) value).intValue();
                    if (-1 <= ival && ival <= 5) {
                        code.emitop0(iconst_0 + ival);
                    }
                    break;

            }
            return stackeItem[typecode];
        }
    }

    class StaticItem extends Item {
        Symbol member;

        StaticItem(Symbol member) {
            super(Code.typeCode(member.enasure(types)));
            this.member = member;
        }


        @Override
        Item load() {
            code.emitop2(getstatic, pool.put(member));
            return stackeItem[typecode];
        }

        @Override
        public void store() {
            code.emitop2(putstatic, pool.put(member));
        }
    }

    class LocalItem extends Item {
        int reg;

        Type type;

        public LocalItem(Type type, int reg) {
            super(Code.typeCode(type));
            this.reg = reg;
            this.type = type;
        }

        @Override
        public void store() {
            if (reg <= 3) {
                code.emitop0(istore_0 + Code.truncate(typecode) * 4 + reg);
            } else {
//                code.emitop1w(istore + Code.truncate(typecode), reg);
            }
            code.setDefined(reg);
        }

        @Override
        Item load() {
            if (reg <= 3) {
                code.emitop0(iload_0 + Code.truncate(typecode) * 4 + reg);
            }
            return stackeItem[typecode];
        }
    }



}
