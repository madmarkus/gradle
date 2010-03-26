/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.tasks.javadoc;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;
import org.gradle.external.javadoc.JavadocExecHandleBuilder;
import org.gradle.external.javadoc.MinimalJavadocOptions;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.gradle.util.GUtil;
import org.gradle.util.exec.ExecHandle;

import java.io.File;
import java.util.*;

/**
 * <p>Generates Javadoc from Java source.</p>
 *
 * @author Hans Dockter
 */
public class Javadoc extends SourceTask {
    private JavadocExecHandleBuilder javadocExecHandleBuilder = new JavadocExecHandleBuilder();

    private File destinationDir;

    private boolean failOnError = true;

    private String title;

    private String maxMemory;

    private MinimalJavadocOptions options = new StandardJavadocDocletOptions();

    private FileCollection classpath;

    @TaskAction
    protected void generate() {
        final File destinationDir = getDestinationDir();

        if (options.getDestinationDirectory() == null) {
            options.destinationDirectory(destinationDir);
        }

        options.classpath(new ArrayList<File>(getClasspath().getFiles()));

        if (!GUtil.isTrue(options.getWindowTitle()) && GUtil.isTrue(getTitle())) {
            options.windowTitle(getTitle());
        }
        if (options instanceof StandardJavadocDocletOptions) {
            StandardJavadocDocletOptions docletOptions = (StandardJavadocDocletOptions) options;
            if (!GUtil.isTrue(docletOptions.getDocTitle()) && GUtil.isTrue(getTitle())) {
                docletOptions.setDocTitle(getTitle());
            }
        }

        if (maxMemory != null) {
            final List<String> jFlags = options.getJFlags();
            final Iterator<String> jFlagsIt = jFlags.iterator();
            boolean containsXmx = false;
            while (!containsXmx && jFlagsIt.hasNext()) {
                final String jFlag = jFlagsIt.next();
                if (jFlag.startsWith("-Xmx")) {
                    containsXmx = true;
                }
            }
            if (!containsXmx) {
                options.jFlags("-Xmx" + maxMemory);
            }
        }

        List<String> sourceNames = new ArrayList<String>();
        for (File sourceFile : getSource()) {
            sourceNames.add(sourceFile.getAbsolutePath());
        }
        options.setSourceNames(sourceNames);

        executeExternalJavadoc();
    }

    private void executeExternalJavadoc() {
        javadocExecHandleBuilder.execDirectory(getProject().getRootDir()).options(options).optionsFile(getOptionsFile());

        final ExecHandle execHandle = javadocExecHandleBuilder.getExecHandle();

        switch (execHandle.startAndWaitForFinish()) {
            case SUCCEEDED:
                break;
            case ABORTED:
                throw new GradleException(
                        "Javadoc generation ended in aborted state (should not happen)." + execHandle.getState());
            case FAILED:
                if (failOnError) {
                    throw new GradleException("Javadoc generation failed.", execHandle.getFailureCause());
                } else {
                    break;
                }
            default:
                throw new GradleException("Javadoc generation ended in an unknown end state." + execHandle.getState());
        }
    }

    void setJavadocExecHandleBuilder(JavadocExecHandleBuilder javadocExecHandleBuilder) {
        if (javadocExecHandleBuilder == null) {
            throw new IllegalArgumentException("javadocExecHandleBuilder == null!");
        }
        this.javadocExecHandleBuilder = javadocExecHandleBuilder;
    }

    /**
     * <p>Returns the directory to generate the documentation into.</p>
     *
     * @return The directory.
     */
    @OutputDirectory
    public File getDestinationDir() {
        return destinationDir;
    }

    /**
     * <p>Sets the directory to generate the documentation into.</p>
     */
    public void setDestinationDir(File destinationDir) {
        this.destinationDir = destinationDir;
    }

    /**
     * Returns the amount of memory allocated to this task.
     */
    public String getMaxMemory() {
        return maxMemory;
    }

    /**
     * Sets the amount of memory allocated to this task.
     *
     * @param maxMemory The amount of memory
     */
    public void setMaxMemory(String maxMemory) {
        this.maxMemory = maxMemory;
    }

    /**
     * <p>Returns the title for the generated documentation.</p>
     *
     * @return The title, possibly null.
     */
    public String getTitle() {
        return title;
    }

    /**
     * <p>Sets the title for the generated documentation.</p>
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns whether javadoc generation is accompanied by verbose output.
     *
     * @see #setVerbose(boolean)
     */
    public boolean isVerbose() {
        return options.isVerbose();
    }

    /**
     * Sets whether javadoc generation is accompanied by verbose output or not. The verbose output is done via println
     * (by the underlying ant task). Thus it is not catched by our logging.
     *
     * @param verbose Whether the output should be verbose.
     */
    public void setVerbose(boolean verbose) {
        if (verbose) {
            options.verbose();
        }
    }

    @InputFiles
    public FileCollection getClasspath() {
        return classpath;
    }

    public void setClasspath(FileCollection configuration) {
        this.classpath = configuration;
    }

    public MinimalJavadocOptions getOptions() {
        return options;
    }

    public void setOptions(MinimalJavadocOptions options) {
        this.options = options;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public File getOptionsFile() {
        return new File(getTemporaryDir(), "javadoc.options");
    }
}
