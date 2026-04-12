/*
 * Copyright (c) 2012-2026 Hazendaz.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of The Apache Software License,
 * Version 2.0 which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Contributors:
 *     Hazendaz (Jeremy Landis).
 */
package net.jangaroo.smartsprites.maven;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Path;

import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoExtension;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link SmartSpritesMojo} using the Maven Plugin Testing Harness 3.5.1 (JUnit 5).
 */
@MojoTest
class SmartSpritesMojoTest {

    /**
     * When {@code skip=true} the mojo must return without executing any processing.
     */
    @Test
    @InjectMojo(goal = "smartsprites")
    @MojoParameter(name = "skip", value = "true")
    void testSkipExecution(SmartSpritesMojo mojo) {
        assertDoesNotThrow(mojo::execute);
    }

    /**
     * An unrecognised log level must cause a {@link MojoExecutionException}.
     */
    @Test
    @InjectMojo(goal = "smartsprites")
    @MojoParameter(name = "logLevel", value = "INVALID_LEVEL")
    void testInvalidLogLevelThrowsException(SmartSpritesMojo mojo) {
        assertThrows(MojoExecutionException.class, mojo::execute);
    }

    /**
     * An unrecognised PNG depth value must cause a {@link MojoExecutionException}.
     */
    @Test
    @InjectMojo(goal = "smartsprites")
    @MojoParameter(name = "spritePngDepth", value = "INVALID_DEPTH")
    void testInvalidSpritePngDepthThrowsException(SmartSpritesMojo mojo) {
        assertThrows(MojoExecutionException.class, mojo::execute);
    }

    /**
     * An unrecognised working mode must cause a {@link MojoExecutionException}.
     */
    @Test
    @InjectMojo(goal = "smartsprites")
    @MojoParameter(name = "workingMode", value = "invalidMode")
    void testInvalidWorkingModeThrowsException(SmartSpritesMojo mojo) {
        assertThrows(MojoExecutionException.class, mojo::execute);
    }

    /**
     * Using rootDirMode with a rootDirPath that does not exist must cause a {@link MojoExecutionException}.
     */
    @Test
    @InjectMojo(goal = "smartsprites")
    void testRootDirPathDoesNotExistThrowsException(SmartSpritesMojo mojo) throws IllegalAccessException {
        MojoExtension.setVariableValueToObject(mojo, "rootDirPath", new File("/non/existent/directory/path"));
        assertThrows(MojoExecutionException.class, mojo::execute);
    }

    /**
     * Using rootDirMode with an empty directory must complete successfully without processing any sprites.
     */
    @Test
    @InjectMojo(goal = "smartsprites")
    void testRootDirModeSucceedsWithEmptyDirectory(SmartSpritesMojo mojo, @TempDir Path tempDir)
            throws IllegalAccessException {
        MojoExtension.setVariableValueToObject(mojo, "rootDirPath", tempDir.toFile());
        MojoExtension.setVariableValueToObject(mojo, "outputDirPath", tempDir.resolve("output").toFile());
        assertDoesNotThrow(mojo::execute);
    }
}
