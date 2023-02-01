package com.myj.tools.javac.util;

public enum StandardLocation implements JavaFileManager.Location {
    CLASS_OUTPUT,
    SOURCE_OUTPUT,
    CLASS_PATH,
    SOURCE_PATH,
    ANNOTATION_PROCESSOR_PATH,
    PLATFORM_CLASS_PATH;

    private StandardLocation() {}


}
