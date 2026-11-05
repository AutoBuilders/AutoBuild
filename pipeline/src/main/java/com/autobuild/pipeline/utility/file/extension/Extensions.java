package com.autobuild.pipeline.utility.file.extension;

/**
 * Used to fetch extensions for specific script types.
 * 
 * @author Suvabrata Chowdhury
 */

 //TODO: Switch cases are not extensible. Use some other pattern
public class Extensions {
    private static final String SHELL_SCRIPT_EXTENSION = ".sh";
    private static final String LOG_FILE_EXTENSION = ".log";

    public static String nameFor(String fileType) {
        String extension = "";

        switch (fileType.toLowerCase()) {
            case "bash":
                extension = SHELL_SCRIPT_EXTENSION;
                break;
            case "log":
                extension = LOG_FILE_EXTENSION;
                break;
            default:
                throw new IllegalArgumentException("Given Script type " + fileType + " is not allowed");
        }

        return extension;
    }
}
