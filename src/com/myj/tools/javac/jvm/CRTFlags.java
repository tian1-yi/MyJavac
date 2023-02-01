package com.myj.tools.javac.jvm;

public interface CRTFlags {
    public static final int CRT_STATEMENT       = 0x0001;
    public static final int CRT_BLOCK           = 0x0002;
    public static final int CRT_ASSIGNMENT      = 0x0004;
    public static final int CRT_FLOW_CONTROLLER = 0x0008;
    public static final int CRT_FLOW_TARGET     = 0x0010;
    public static final int CRT_INVOKE          = 0x0020;
    public static final int CRT_CREATE          = 0x0040;
    public static final int CRT_BRANCH_TRUE     = 0x0080;
    public static final int CRT_BRANCH_FALSE    = 0x0100;

    /** The mask for valid flags
     */
    public static final int CRT_VALID_FLAGS = CRT_STATEMENT | CRT_BLOCK | CRT_ASSIGNMENT |
            CRT_FLOW_CONTROLLER | CRT_FLOW_TARGET | CRT_INVOKE |
            CRT_CREATE | CRT_BRANCH_TRUE | CRT_BRANCH_FALSE;
}
