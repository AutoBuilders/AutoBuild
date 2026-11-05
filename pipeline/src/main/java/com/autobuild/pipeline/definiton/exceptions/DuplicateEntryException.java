package com.autobuild.pipeline.definiton.exceptions;

/**
 * Exception to throw when duplicate entry of any entity is provided.
 * @author Suvabrata Chowdhury
 */
public class DuplicateEntryException extends Exception{
    public DuplicateEntryException(String msg) {
        super(msg);
    }
}
