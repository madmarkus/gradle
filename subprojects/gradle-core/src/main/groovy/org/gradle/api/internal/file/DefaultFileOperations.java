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
package org.gradle.api.internal.file;

import groovy.lang.Closure;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.PathValidation;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.archive.TarFileTree;
import org.gradle.api.internal.file.archive.ZipFileTree;
import org.gradle.api.internal.file.copy.CopyActionImpl;
import org.gradle.api.internal.file.copy.CopySpecImpl;
import org.gradle.api.internal.file.copy.FileCopyActionImpl;
import org.gradle.api.internal.file.copy.FileCopySpecVisitor;
import org.gradle.api.internal.tasks.TaskResolver;
import org.gradle.api.tasks.WorkResult;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static org.gradle.util.ConfigureUtil.*;

public class DefaultFileOperations implements FileOperations {
    private final FileResolver fileResolver;
    private final TaskResolver taskResolver;
    private final TemporaryFileProvider temporaryFileProvider;

    public DefaultFileOperations(FileResolver fileResolver, TaskResolver taskResolver, TemporaryFileProvider temporaryFileProvider) {
        this.fileResolver = fileResolver;
        this.taskResolver = taskResolver;
        this.temporaryFileProvider = temporaryFileProvider;
    }

    public File file(Object path) {
        return fileResolver.resolve(path);
    }

    public File file(Object path, PathValidation validation) {
        return fileResolver.resolve(path, validation);
    }

    public URI uri(Object path) {
        return fileResolver.resolveUri(path);
    }
    
    public ConfigurableFileCollection files(Object... paths) {
        return new PathResolvingFileCollection(fileResolver, taskResolver, paths);
    }

    public ConfigurableFileCollection files(Object paths, Closure configureClosure) {
        return configure(configureClosure, files(paths));
    }

    public ConfigurableFileTree fileTree(Object baseDir) {
        return new FileSet(baseDir, fileResolver);
    }

    public FileSet fileTree(Map<String, ?> args) {
        return new FileSet(args, fileResolver);
    }

    public FileSet fileTree(Closure closure) {
        return configure(closure, new FileSet(Collections.emptyMap(), fileResolver));
    }

    public FileTree zipTree(Object zipPath) {
        return new ZipFileTree(file(zipPath), getExpandDir());
    }

    public FileTree tarTree(Object tarPath) {
        return new TarFileTree(file(tarPath), getExpandDir());
    }

    private File getExpandDir() {
        return temporaryFileProvider.newTemporaryFile("expandedArchives");
    }

    public String relativePath(Object path) {
        return fileResolver.resolveAsRelativePath(path);
    }

    public File mkdir(Object path) {
        File dir = fileResolver.resolve(path);
        if (dir.isFile()) {
            throw new InvalidUserDataException(String.format("Can't create directory. The path=%s points to an existing file.", path));
        }
        dir.mkdirs();
        return dir;
    }

    public WorkResult copy(Closure closure) {
        CopyActionImpl action = configure(closure, new FileCopyActionImpl(fileResolver, new FileCopySpecVisitor()));
        action.execute();
        return action;
    }

    public CopySpec copySpec(Closure closure) {
        return configure(closure, new CopySpecImpl(fileResolver));
    }

    public FileResolver getFileResolver() {
        return fileResolver;
    }
}
