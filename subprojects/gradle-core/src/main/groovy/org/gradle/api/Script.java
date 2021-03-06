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
package org.gradle.api;

import groovy.lang.Closure;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileTree;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.WorkResult;

import java.io.File;
import java.net.URI;
import java.util.Map;

/**
 * <p>The base class for all scripts executed by Gradle. This is a extension to the Groovy {@code Script} class, which
 * adds in some Gradle-specific methods. As your compiled script class will implement this interface, you can use the
 * methods and properties declared here directly in your script.</p>
 *
 * <p>Generally, a {@code Script} object will have a delegate object attached to it. For example, a build script will
 * have a {@link Project} instance attached to it, and an initialization script will have a {@link
 * org.gradle.api.invocation.Gradle} instance attached to it. Any property reference or method call which is not found
 * on this {@code Script} object is forwarded to the delegate object.</p>
 */
public interface Script {
    /**
     * <p>Configures the delegate object for this script using plugins or scripts. The given closure is used to
     * configure an {@link org.gradle.api.plugins.ObjectConfigurationAction} which is then used to configure the
     * delegate object.</p>
     *
     * @param closure The closure to configure the {@code ObjectConfigurationAction}.
     */
    void apply(Closure closure);

    /**
     * <p>Configures the delegate object for this script using plugins or scripts. The following options are
     * available:</p>
     *
     * <ul><li>{@code from}: A script to apply to the delegate object. Accepts any path supported by {@link
     * #uri(Object)}.</li>
     *
     * <li>{@code plugin}: The id or implementation class of the plugin to apply to the delegate object.</li>
     *
     * <li>{@code to}: The target delegate object or objects.</li></ul>
     *
     * <p>For more detail, see {@link org.gradle.api.plugins.ObjectConfigurationAction}.</p>
     *
     * @param options The options to use to configure the {@code ObjectConfigurationAction}.
     */
    void apply(Map<String, ?> options);

    /**
     * Returns the script handler for this script. You can use this handler to manage the classpath used to compile and
     * execute this script.
     *
     * @return the classpath handler. Never returns null.
     */
    ScriptHandler getBuildscript();

    /**
     * Configures the classpath for this script. The given closure is executed against this script's {@link
     * ScriptHandler}. The {@link ScriptHandler} is passed to the closure as the closure's delegate.
     *
     * @param configureClosure the closure to use to configure the script classpath.
     */
    void buildscript(Closure configureClosure);

    /**
     * <p>Resolves a file path relative to the directory containing this script. This works as described for {@link
     * Project#file(Object)}</p>
     *
     * @param path The object to resolve as a File.
     * @return The resolved file. Never returns null.
     */
    File file(Object path);

    /**
     * <p>Resolves a file path relative to the directory containing this script and validates it using the given scheme.
     * See {@link PathValidation} for the list of possible validations.</p>
     *
     * @param path An object to resolve as a File.
     * @param validation The validation to perform on the file.
     * @return The resolved file. Never returns null.
     * @throws InvalidUserDataException When the file does not meet the given validation constraint.
     */
    File file(Object path, PathValidation validation) throws InvalidUserDataException;

    /**
     * <p>Resolves a file path to a URI, relative to the directory containing this script. Evaluates the provided path
     * object as described for {@link #file(Object)}, with the exception that any URI scheme is supported, not just
     * 'file:' URIs.</p>
     *
     * @param path The object to resolve as a URI.
     * @return The resolved URI. Never returns null.
     */
    URI uri(Object path);

    /**
     * <p>Returns a {@link ConfigurableFileCollection} containing the given files. This works as described for {@link
     * Project#files(Object...)}. Relative paths are resolved relative to the directory containing this script.</p>
     *
     * @param paths The paths to the files. May be empty.
     * @return The file collection. Never returns null.
     */
    ConfigurableFileCollection files(Object... paths);

    /**
     * <p>Creates a new {@code ConfigurableFileCollection} using the given paths. The file collection is configured
     * using the given closure. This method works as described for {@link Project#files(Object, groovy.lang.Closure)}.
     * Relative paths are resolved relative to the directory containing this script.</p>
     *
     * @param paths The contents of the file collection. Evaluated as for {@link #files(Object...)}.
     * @param configureClosure The closure to use to configure the file collection.
     * @return the configured file tree. Never returns null.
     */
    ConfigurableFileCollection files(Object paths, Closure configureClosure);

    /**
     * <p>Returns the relative path from the directory containing this script to the given path. The given path object
     * is (logically) resolved as described for {@link #file(Object)}, from which a relative path is calculated.</p>
     *
     * @param path The path to convert to a relative path.
     * @return The relative path. Never returns null.
     */
    String relativePath(Object path);

    /**
     * <p>Creates a new {@code ConfigurableFileTree} using the given base directory. The given baseDir path is evaluated
     * as for {@link #file(Object)}.</p>
     *
     * <p>The returned file tree is lazy, so that it scans for files only when the contents of the file tree are
     * queried. The file tree is also live, so that it scans for files each time the contents of the file tree are
     * queried.</p>
     *
     * @param baseDir The base directory of the file tree. Evaluated as for {@link #file(Object)}.
     * @return the file tree. Never returns null.
     */
    ConfigurableFileTree fileTree(Object baseDir);

    /**
     * <p>Creates a new {@code ConfigurableFileTree} using the provided map of arguments.  The map will be applied as
     * properties on the new file tree.  Example:</p>
     *
     * <pre>
     * fileTree(dir:'src', excludes:['**&#47;ignore/**','**&#47;.svn/**'])
     * </pre>
     *
     * <p>The returned file tree is lazy, so that it scans for files only when the contents of the file tree are
     * queried. The file tree is also live, so that it scans for files each time the contents of the file tree are
     * queried.</p>
     *
     * @param args map of property assignments to {@code ConfigurableFileTree} object
     * @return the configured file tree. Never returns null.
     */
    ConfigurableFileTree fileTree(Map<String, ?> args);

    /**
     * <p>Creates a new {@code ConfigurableFileTree} using the provided closure.  The closure will be used to configure
     * the new file tree. The file tree is passed to the closure as its delegate.  Example:</p>
     *
     * <pre>
     * fileTree {
     *    from 'src'
     *    exclude '**&#47;.svn/**'
     * }.copy { into 'dest'}
     * </pre>
     *
     * <p>The returned file tree is lazy, so that it scans for files only when the contents of the file tree are
     * queried. The file tree is also live, so that it scans for files each time the contents of the file tree are
     * queried.</p>
     *
     * @param closure Closure to configure the {@code ConfigurableFileTree} object
     * @return the configured file tree. Never returns null.
     */
    ConfigurableFileTree fileTree(Closure closure);

    /**
     * <p>Creates a new {@code FileTree} which contains the contents of the given ZIP file. The given zipPath path is
     * evaluated as for {@link #file(Object)}. You can combine this method with the {@link #copy(groovy.lang.Closure)}
     * method to unzip a ZIP file.</p>
     *
     * <p>The returned file tree is lazy, so that it scans for files only when the contents of the file tree are
     * queried. The file tree is also live, so that it scans for files each time the contents of the file tree are
     * queried.</p>
     *
     * @param zipPath The ZIP file. Evaluated as for {@link #file(Object)}.
     * @return the file tree. Never returns null.
     */
    FileTree zipTree(Object zipPath);

    /**
     * <p>Creates a new {@code FileTree} which contains the contents of the given TAR file. The given tarPath path is
     * evaluated as for {@link #file(Object)}. You can combine this method with the {@link #copy(groovy.lang.Closure)}
     * method to untar a TAR file.</p>
     *
     * <p>The returned file tree is lazy, so that it scans for files only when the contents of the file tree are
     * queried. The file tree is also live, so that it scans for files each time the contents of the file tree are
     * queried.</p>
     *
     * @param tarPath The TAR file. Evaluated as for {@link #file(Object)}.
     * @return the file tree. Never returns null.
     */
    FileTree tarTree(Object tarPath);

    /**
     * Copy the specified files.  The given closure is used to configure a {@link org.gradle.api.file.CopySpec}, which
     * is then used to copy the files. Example:
     * <pre>
     * copy {
     *    from configurations.runtime
     *    into 'build/deploy/lib'
     * }
     * </pre>
     * Note that CopySpecs can be nested:
     * <pre>
     * copy {
     *    into 'build/webroot'
     *    exclude '**&#47;.svn/**'
     *    from('src/main/webapp') {
     *       include '**&#47;*.jsp'
     *       filter(ReplaceTokens, tokens:[copyright:'2009', version:'2.3.1'])
     *    }
     *    from('src/main/js') {
     *       include '**&#47;*.js'
     *    }
     * }
     * </pre>
     *
     * @param closure Closure to configure the CopySpec
     * @return {@link org.gradle.api.tasks.WorkResult} that can be used to check if the copy did any work.
     */
    WorkResult copy(Closure closure);

    /**
     * Creates a {@link org.gradle.api.file.CopySpec} which can later be used to copy files or create an archive. The
     * given closure is used to configure the {@link org.gradle.api.file.CopySpec} before it is returned by this
     * method.
     *
     * @param closure Closure to configure the CopySpec
     * @return The CopySpec
     */
    CopySpec copySpec(Closure closure);

    /**
     * Creates a directory and returns a file pointing to it.
     *
     * @param path The path for the directory to be created. Evaluated as for {@link #file(Object)}.
     * @return the created directory
     * @throws org.gradle.api.InvalidUserDataException If the path points to an existing file.
     */
    File mkdir(Object path);

    /**
     * Disables redirection of standard output during script execution. By default redirection is enabled.
     *
     * @see #captureStandardOutput(org.gradle.api.logging.LogLevel)
     */
    void disableStandardOutputCapture();

    /**
     * Starts redirection of standard output during to the logging system during script execution. By default
     * redirection is enabled and the output is redirected to the QUIET level. System.err is always redirected to the
     * ERROR level. Redirection of output at execution time can be configured via the tasks.
     *
     * For more fine-grained control on redirecting standard output see {@link org.gradle.api.logging.StandardOutputLogging}.
     *
     * @param level The level standard out should be logged to.
     * @see #disableStandardOutputCapture()
     */
    void captureStandardOutput(LogLevel level);

    /**
     * Returns the logger for this script. You can use this in your script to write log messages.
     *
     * @return The logger. Never returns null.
     */
    Logger getLogger();
}
