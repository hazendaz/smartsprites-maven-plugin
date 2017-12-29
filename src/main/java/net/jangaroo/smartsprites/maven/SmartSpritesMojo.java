/**
 * Copyright (c) 2012-2017 Hazendaz.
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.carrot2.labs.smartsprites.SmartSpritesParameters;
import org.carrot2.labs.smartsprites.SpriteBuilder;
import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.PrintStreamMessageSink;

/**
 * Goal which creates Spritesheets from given css and image files Explanation of every variable is taken directly from
 * the smartsprites documentation (http://csssprites.org/)
 *
 * For further information on the use of smartsprites please refer to: http://csssprites.org/
 */
@Mojo(name = "smartsprites", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresProject = false, threadSafe = true)
public class SmartSpritesMojo extends AbstractMojo {

    /** The static root directory mode. */
    private static final String ROOT_DIR_MODE  = "rootDirMode";

    /** The static css files mode. */
    private static final String CSS_FILES_MODE = "cssFilesMode";

    /**
     * Directory in which SmartSprites processing should be done, required if css-files not specified or if
     * output-dir-path specified, default: not specified.
     *
     * SmartSprites will process all files with the *.css extension found in root-dir-path or any subdirectory of it.
     * For more fine-grained control over the processed CSS files, see the css-files option.
     *
     * If the provided root directory path is relative, it will be resolved against the current working directory.
     *
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/sprites", property = "rootDirPath")
    private File                rootDirPath;

    /**
     * Paths of CSS files to process, required if root-dir-path not specified, default: not specified.
     *
     * SmartSprites will process all CSS files listed using this option. If css-files is to be used together with
     * output-dir-path, root-dir-path must also be specified so that SmartSprites can preserve the directory structure
     * found in root-dir-path in output-dir-path. If root-dir-path and output-dir-path are used, css-files outside of
     * root-dir-path will be ignored.
     *
     * Relative CSS file paths provided using this option will be resolved against the current working directory. Please
     * note that SmartSprites will not expand any wildcards (like style/*.css), it assumes the expansion is performed at
     * the command line shell level.
     *
     * To specify the list of CSS files to process in the SmartSprites Ant task, use one or more nested fileset
     * elements. Please see the build.xml file in the distribution archive for an example.
     */
    @Parameter(property = "cssFiles")
    private List<File>          cssFiles;

    /**
     * Output directory for processed CSS files and CSS-relative sprite images, optional, default: not specified.
     *
     * If a non-empty output-dir-path is specified, a non-empty root-dir-path must also be provided. The directory
     * structure relative to the root-dir-path will be preserved in the output directory. E.g. if CSS files are
     * contained in the css/base directory of root-dir-path, the processed results will be written to
     * output-dir-path/css/base. Also, CSS-relative sprite images will be written to the output directory. Sprite images
     * with document-root-relative URLs will be written relative to the document-root-dir-path. If the output-dir-path
     * directory does not exist, it will be created. If the provided output directory path is relative, it will be
     * resolved against the current working directory.
     *
     * You can leave the output-dir-path empty, in which case the CSS files will be written next to the original CSS
     * files with the css-file-suffix, and sprite images will be written relative to the original CSS files. If you are
     * using a non-empty output-dir-path, you might want to use an empty css-file-suffix.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-resources/META-INF/resources/spritesheets", property = "outPutDirPath")
    private File                outputDirPath;

    /**
     * Document root path for document-root-relative (starting with /) image urls in CSS, optional, default: not
     * specified.
     *
     * All document-root-relative image and sprite URLs will be taken relative to document-root-dir-path. Also
     * document-root-relative sprite URLs will be written relative to document-root-dir-path. You can leave this
     * property empty if your CSS uses only CSS-relative image URLs. If the provided document root directory path is
     * relative, it will be resolved against the current working directory.
     */
    @Parameter(property = "documentRootDirPath")
    private File                documentRootDirPath;

    /**
     * Message logging level, optional, default: WARN.
     *
     * Messages less important than log-level will not be shown. SmartSprites has 3 levels of log messages (in the
     * increasing order of importance): INFO: information messages, can be safely ignored IE6NOTICE: notices related to
     * possible quality loss when creating IE6-friendly sprite images, see also the IE6-friendly PNG option WARN:
     * warnings related to syntax, IO and sprite rendering quality loss problems that may cause the converted
     * sprite-based designs look broken
     */
    @Parameter(defaultValue = "WARN", property = "logLevel")
    private String              logLevel;

    /**
     * Color depth of sprites in the PNG format, optional, default: AUTO. AUTO: PNG color depth will be chosen
     * automatically. If the sprite image does not contain partial transparencies (alpha channel) and has less than 256
     * colors, PNG8 will be used. Otherwise, the sprite will be saved in PNG24. DIRECT: PNG sprites will always be saved
     * in the PNG24 format. INDEXED: PNG sprites will always be saved in the PNG8 format. If the sprite image contains
     * partial transparencies (alpha channel) or has more than 255 colors, image quality loss may occur and appropriate
     * warnings will be issued. See also the sprite-matte-color property.
     */
    @Parameter(defaultValue = "AUTO", property = "spritePngDepth")
    private String              spritePngDepth;

    /**
     * Enables generation of IE6-friendly sprite images, optional, default: disabled.
     *
     * If sprite-png-ie6 is specified, for each PNG sprite image with partial transparencies (alpha channel) or more
     * than 255 colors and any transparencies, SmartSprites will generate a corresponding color-reduced PNG8 file for
     * IE6.
     *
     * An extra IE6-only CSS rule will be added to the generated CSS file to ensure that IE6 (and only IE6) uses the
     * color-reduced version: #web { width: 17px; height: 17px; background-repeat: no-repeat; background-image:
     * url('../img/mysprite.png'); -background-image: url('../img/mysprite-ie6.png'); background-position: left -0px; }
     *
     * See also the sprite-matte-color property.
     */
    @Parameter(defaultValue = "false", property = "spritePngIeSix")
    private boolean             spritePngIeSix;

    /**
     * The encoding to assume for input and output CSS files, default: UTF-8. For the list of allowed values, please see
     * the list of encodings supported in Java.
     */
    @Parameter(defaultValue = "UTF-8", property = "cssFileEncoding")
    private String              cssFileEncoding;

    /**
     * Suffix to be appended to the processed CSS file name, optional, default: .
     */
    @Parameter(defaultValue = "", property = "cssFileSuffix")
    private String              cssFileSuffix;

    /**
     * To make sure the different modes are working correctly a mode has to be specified. Modes: - rootDirMode (rootDir
     * is set, optionally outputdir! No css-Files!) - cssFilesMode (only cssFiles is set, no rootDir, no OutputDir) -
     * cssFilesWithOutputDirMode (cssFiles are set, as well as rootDir and outputDir)
     */
    @Parameter(defaultValue = SmartSpritesMojo.ROOT_DIR_MODE, property = "workingMode")
    private String              workingMode;

    /**
     * To enable skipping run of plugin.
     */
    @Parameter(defaultValue = "false", alias = "skip", property = "skip")
    private Boolean             skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Check if plugin run should be skipped
        if (this.skip) {
            this.getLog().info("Smartsprites is skipped");
            return;
        }

        // Check for the correct log-level
        Message.MessageLevel msgLogLevel;
        try {
            msgLogLevel = Message.MessageLevel.valueOf(this.logLevel.toUpperCase());
        } catch (final Exception e) {
            throw new MojoExecutionException("LogLevel Error - please select a valid value! (INFO, IE6NOTICE, WARN) ",
                    e);
        }

        // Check for the correct PNG-Depth
        SmartSpritesParameters.PngDepth pngDepth;
        try {
            pngDepth = SmartSpritesParameters.PngDepth.valueOf(this.spritePngDepth);
        } catch (final Exception e) {
            throw new MojoExecutionException("PngDepth Error - please select a valid value! (AUTO, DIRECT, INDEXED)  ",
                    e);
        }

        // Make sure we are using only correct workingModes
        if (!(this.workingMode.equals(SmartSpritesMojo.ROOT_DIR_MODE)
                || this.workingMode.equals(SmartSpritesMojo.CSS_FILES_MODE)
                || this.workingMode.equals("cssFilesWithOutputDirMode"))) {
            throw new MojoExecutionException(
                    "workingMode Error - plese select a valid value! (rootDirMode, cssFilesMode, cssFilesWithOutputDirMode)");
        }

        // Variables for paths
        String rootDirPathTemp = "";
        String outputDirPathTemp = "";
        String documentRootDirPathTemp = "";
        List<String> cssFilesTemp = new ArrayList<>();

        // Check if we should set cssFiles to null, or if we are in a working mode
        // where we want to use cssFiles
        if (!this.workingMode.equals(SmartSpritesMojo.ROOT_DIR_MODE)) {
            for (final File cssFile : this.cssFiles) {
                if (cssFile.exists()) {
                    cssFilesTemp.add(cssFile.toString());
                } else {
                    throw new MojoExecutionException("The following css-file doesn't exist: " + cssFile);
                }
            }
        } else {
            this.cssFiles = null;
        }

        // Check if the folders exist or are at least configured
        if (this.rootDirPath != null && !this.workingMode.equals(SmartSpritesMojo.CSS_FILES_MODE)) {
            if (this.rootDirPath.exists()) {
                rootDirPathTemp = this.rootDirPath.toString();
            } else {
                throw new MojoExecutionException("The rootDirPath doesn't exist. " + this.rootDirPath.toString());
            }
        }

        // Check if the outputDirPath is set
        if (this.outputDirPath != null && !this.workingMode.equals(SmartSpritesMojo.CSS_FILES_MODE)) {
            outputDirPathTemp = this.outputDirPath.toString();
        }

        // Check if the documentRootDirPath is set
        if (this.documentRootDirPath != null) {
            documentRootDirPathTemp = this.documentRootDirPath.toString();
        }

        // Determine which workingMode we are in and check if all conditions for that
        // specific mode are matched
        if (this.workingMode.equals(SmartSpritesMojo.ROOT_DIR_MODE)) {
            if (this.rootDirPath == null) {
                throw new MojoExecutionException("Please configure a rootDirPath.");
            }
            cssFilesTemp = null;
        } else if (this.workingMode.equals(SmartSpritesMojo.CSS_FILES_MODE)) {
            if (this.cssFiles == null) {
                throw new MojoExecutionException("Please configure some cssFiles.");
            }
            rootDirPathTemp = null;
            outputDirPathTemp = null;
        } else if (this.workingMode.equals("cssFilesWithOutputDirMode") && this.cssFiles == null
                || this.rootDirPath == null || this.outputDirPath == null) {
            throw new MojoExecutionException("Please configure cssFiles and/or a rootDirPath and/or an outputDirPath");
        }

        // Configure the SmartSpritesParameters for execution
        SmartSpritesParameters smartParameters;

        smartParameters = new SmartSpritesParameters(rootDirPathTemp, cssFilesTemp, outputDirPathTemp,
                documentRootDirPathTemp, msgLogLevel, this.cssFileSuffix, pngDepth, this.spritePngIeSix,
                this.cssFileEncoding);

        final MessageLog messageLog = new MessageLog(
                new PrintStreamMessageSink(System.out, smartParameters.getLogLevel()));
        SpriteBuilder spriteBuilder;

        // Try to execute SmartSprites with the configured parameters and the defined messageLog
        try {
            spriteBuilder = new SpriteBuilder(smartParameters, messageLog);
            spriteBuilder.buildSprites();
        } catch (final IOException e) {
            throw new MojoExecutionException("Smartsprites error: ", e);
        }
    }

}
