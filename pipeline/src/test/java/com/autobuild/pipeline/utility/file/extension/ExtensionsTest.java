package com.autobuild.pipeline.utility.file.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


public class ExtensionsTest {
    // private Extensions extensions;

    @Test
    public void testNameForBashScripts() {
        assertEquals(".sh", Extensions.nameFor("Bash"));
        assertEquals(".sh", Extensions.nameFor("BASH"));
        assertEquals(".sh", Extensions.nameFor("bash"));
        assertEquals(".sh", Extensions.nameFor("BaSh"));
    }

    @Test
    public void testNameForOtherScritps() {
        assertThrows(IllegalArgumentException.class, () -> Extensions.nameFor("python"));
    }
}
