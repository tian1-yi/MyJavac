package com.myj.tools.javac.code;

public class Flags {
    public static final int PUBLIC       = 1<<0;
    public static final int PRIVATE      = 1<<1;
    public static final int PROTECTED    = 1<<2;
    public static final int STATIC       = 1<<3;
    public static final int FINAL        = 1<<4;
    public static final int SYNCHRONIZED = 1<<5;
    public static final int VOLATILE     = 1<<6;
    public static final int TRANSIENT    = 1<<7;
    public static final int NATIVE       = 1<<8;
    public static final int INTERFACE    = 1<<9;
    public static final int ABSTRACT     = 1<<10;
    public static final int STRICTFP     = 1<<11;


    public static final int SYNTHETIC    = 1<<12;


    public static final int ANNOTATION   = 1<<13;

    public static final int HASINIT          = 1<<18;

    public static final int ENUM         = 1<<14;

    public static final int NOOUTERTHIS  = 1<<22;

    public static final int UNATTRIBUTED = 1<<28;

    public static final int ANONCONSTR   = 1<<29;
    public static final int ACYCLIC          = 1<<30;


    public static final long PARAMETER   = 1L<<33;

    public static final long VARARGS   = 1L<<34;

    public static final long GENERATEDCONSTR   = 1L<<36;

    public static final int
            AccessFlags           = PUBLIC | PROTECTED | PRIVATE,
            LocalClassFlags       = FINAL | ABSTRACT | STRICTFP | ENUM | SYNTHETIC,
            MemberClassFlags      = LocalClassFlags | INTERFACE | AccessFlags,
            ClassFlags            = LocalClassFlags | INTERFACE | PUBLIC | ANNOTATION,
            InterfaceVarFlags     = FINAL | STATIC | PUBLIC,
            VarFlags              = AccessFlags | FINAL | STATIC |
                    VOLATILE | TRANSIENT | ENUM,
            ConstructorFlags      = AccessFlags,
            InterfaceMethodFlags  = ABSTRACT | PUBLIC,
            MethodFlags           = AccessFlags | ABSTRACT | STATIC | NATIVE |
                    SYNCHRONIZED | FINAL | STRICTFP;

    public static final long
            LocalVarFlags         = FINAL | PARAMETER;

    public static final int StandardFlags = 0x0fff;

    public static final int ACC_SUPER    = 0x0020;
    public static final int ACC_BRIDGE   = 0x0040;
    public static final int ACC_VARARGS  = 0x0080;
}
