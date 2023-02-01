package com.myj.tools.javac.jvm;

import com.myj.tools.javac.code.Scope;
import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.comp.AttrContext;
import com.myj.tools.javac.comp.Env;
import com.myj.tools.javac.util.*;

import javax.tools.JavaFileObject;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.myj.tools.javac.code.Kinds.PCK;
import static com.myj.tools.javac.code.Kinds.TYP;
import static com.myj.tools.javac.util.StandardLocation.CLASS_PATH;


public class ClassReader implements Symbol.Completer {


    public static final Context.Key<ClassReader> classReaderKey = new Context.Key<>();

    private Map<Name, Symbol.PackageSymbol> packages;

    private Map<Name, Symbol.ClassSymbol> classes;

    private Names names;

    private Symtab syms;

    private JavaFileManager fileManager;

    private JavaFileManager.Location curLocation;

    public static ClassReader instance(Context context) {
        ClassReader classReader = context.get(classReaderKey);
        if (null == classReader) {
            classReader = new ClassReader(context);
        }
        return classReader;
    }

    private ClassReader(Context context) {
        context.put(classReaderKey, this);

        this.names = Names.instance(context);
        this.fileManager = context.get(JavaFileManager.class);
        this.syms = Symtab.instance(context);

    }

    public void init(Symtab syms, boolean definitive) {
        if (definitive) {
            packages = syms.packages;
            classes = syms.classes;
        } else {
            packages = new HashMap<>();
            classes = new HashMap<>();

        }
        packages.put(names.empty, syms.rootPackage);
        syms.rootPackage.completer = this;
        syms.unnamedPackage.completer = this;
    }

    @Override
    public void complete(Symbol sym) {
        if (sym.flatName() == names.java_lang_Object) {
            return;
        }

        if (sym.kind == TYP) {
            Symbol.ClassSymbol c = (Symbol.ClassSymbol) sym;
            c.members_field = new Scope.ErrorScope(c);
        } else if (sym.kind == PCK){
            Symbol.PackageSymbol p = (Symbol.PackageSymbol) sym;
            // 填充
            fillIn(p);
        } else {

        }
    }

    private void fillIn(Symbol.PackageSymbol p) {
        if (p.members_field == null) p.members_field = new Scope(p);

        String packageName = p.fullname.toString();


        fillIn(p, CLASS_PATH, fileManager.list(CLASS_PATH, packageName, getPackageFileKinds()));


    }

    private void fillIn(Symbol.PackageSymbol p, JavaFileManager.Location location, Iterable<com.myj.tools.javac.util.JavaFileObject> files) {
        curLocation = location;
        for (com.myj.tools.javac.util.JavaFileObject file : files) {
            switch (file.getKind()) {
                case CLASS:
                case SOURCE:
                    String binaryName = fileManager.inferBinaryName(location, file);
                    String simpleName = binaryName.substring(binaryName.lastIndexOf('.') + 1);
                    includeClassFile(p, file);
                    break;
                default:
            }
        }
    }

    /**
     * 处理该文件
     * @param p
     * @param file
     */
    private void includeClassFile(Symbol.PackageSymbol p, com.myj.tools.javac.util.JavaFileObject file) {

        String binaryName = fileManager.inferBinaryName(curLocation, file);
        String simpleName = binaryName.substring(binaryName.lastIndexOf(".") + 1);
        Name className = names.fromString(simpleName);

        Symbol.ClassSymbol c = (Symbol.ClassSymbol) p.members_field.lookup(className).sym;
        if (c == null) {
            c = enterClass(className, p);
            if (c.classFile == null) {
                c.classFile = file;
            }

            if (c.owner == p)
                p.members_field.enter(c);
        }
    }

    /**
     * 加入class
     * @param className
     * @param owner
     * @return
     */
    public Symbol.ClassSymbol enterClass(Name className, Symbol.TypeSymbol owner) {

        Name flatName = Symbol.TypeSymbol.formFlatName(className, owner);
        Symbol.ClassSymbol c = classes.get(flatName);
        if (c == null) {
            //定义该类符号，并加入classes中
            c = defineClass(className, owner);
            classes.put(flatName, c);
        }
        return c;

    }

    /**
     * 定义class
     * @param className
     * @param owner
     * @return
     */
    private Symbol.ClassSymbol defineClass(Name className, Symbol owner) {
        Symbol.ClassSymbol c = new Symbol.ClassSymbol(0, className, owner);
        c.completer = this;
        return c;
    }

    public EnumSet<JavaFileObject.Kind> getPackageFileKinds() {
        return EnumSet.of(JavaFileObject.Kind.CLASS, JavaFileObject.Kind.SOURCE);
    }




    public Symbol.PackageSymbol enterPackage(Name fullName) {
        Symbol.PackageSymbol p = packages.get(fullName);
        if (p == null) {
            p = new Symbol.PackageSymbol(Convert.shortName(fullName), enterPackage(Convert.packagePart(fullName)));
            p.completer = this;
            packages.put(fullName, p);
        }
        return p;
    }

    public Symbol.ClassSymbol enterClass(Name fromString) {
        Symbol.ClassSymbol c = classes.get(fromString);
        if (c == null) {
            return enterClass(fromString, (com.myj.tools.javac.util.JavaFileObject)null);
        } else {
            return c;
        }
    }

    private Symbol.ClassSymbol enterClass(Name fromString, com.myj.tools.javac.util.JavaFileObject javaFileObject) {
        Symbol.ClassSymbol cs = classes.get(fromString);

        Name packageName = Convert.packagePart(fromString);
        Symbol.PackageSymbol owner = packageName.isEmpty() ? syms.unnamedPackage : enterPackage(packageName);
        cs = defineClass(Convert.shortName(fromString), owner);
        cs.classFile = javaFileObject;
        classes.put(fromString, cs);
        return cs;
    }

    public Symbol.ClassSymbol loadClass(Name flatName) {
        boolean absent = classes.get(flatName) == null;
        Symbol.ClassSymbol c = enterClass(flatName);
        if (c.members_field == null && c.completer != null) {
            c.complete();
        }
        return c;
    }
}
