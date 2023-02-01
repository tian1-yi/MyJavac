package com.myj.tools.javac.comp;

import com.myj.tools.javac.code.Flags;
import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.code.Type;
import com.myj.tools.javac.tree.JCTree;
import com.myj.tools.javac.util.Context;
import com.myj.tools.javac.util.Names;
import com.myj.tools.javac.util.Types;

import static com.myj.tools.javac.code.Flags.*;
import static com.myj.tools.javac.code.Kinds.*;
import static com.myj.tools.javac.code.TypeTags.NONE;

public class Check {

    public static final Context.Key<Check> checkKey = new Context.Key<>();

    Names names;

    private Check(Context context) {
        context.put(checkKey, this);

        this.types = Types.instance(context);
        this.names = Names.instance(context);
    }

    Types types;

    public static Check instance(Context context) {
        Check check = context.get(checkKey);
        if (check == null) {
            check = new Check(context);
        }
        return check;
    }

    public Type checkType(int pos, Type owntype, Type pt) {

        if (pt.tag == NONE) {
            return owntype;
        } else if (types.isAssignable(owntype, pt)) {
            return owntype;
        }


        return null;
    }

    public long checkFlags(long flags, Symbol sym, JCTree tree) {
        long mask = 0;
        long implicit = 0;

        switch (sym.kind) {
            case VAR:
                if (sym.owner.kind != TYP) {
                    mask = LocalVarFlags;
                } else if ((sym.owner.flags_field & INTERFACE) != 0) {
                    mask = implicit = InterfaceVarFlags;
                } else {
                    mask = VarFlags;
                }
                break;
            case MTH:
                if (sym.name == names.init) {
                    mask = ConstructorFlags;
                } else {
                    mask = MethodFlags;
                }

                if (((flags|implicit) & ABSTRACT) == 0) {
                    implicit |= sym.owner.flags_field & STRICTFP;
                }
                break;
            default:
                break;
        }

        long illegal = flags & StandardFlags & ~mask;

        return flags & (mask | ~StandardFlags) | implicit;
    }
}
