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

package org.gradle.util;

import groovy.lang.Closure;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.Tar;
import org.apache.tools.ant.taskdefs.Zip;
import org.gradle.api.UncheckedIOException;
import org.hamcrest.Matcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

public class TestFile extends File {
    private boolean useNativeTools;

    public TestFile(File file, Object... path) {
        super(join(file, path).getAbsolutePath());
    }

    public TestFile(URI uri) {
        this(new File(uri));
    }

    public TestFile(URL url) {
        this(toUri(url));
    }

    public TestFile usingNativeTools() {
        useNativeTools = true;
        return this;
    }

    private static URI toUri(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static File join(File file, Object[] path) {
        File current = file.getAbsoluteFile();
        for (Object p : path) {
            current = new File(current, p.toString());
        }
        return GFileUtils.canonicalise(current);
    }

    public TestFile file(Object... path) {
        return new TestFile(this, path);
    }

    public List<TestFile> files(Object... paths) {
        List<TestFile> files = new ArrayList<TestFile>();
        for (Object path : paths) {
            files.add(file(path));
        }
        return files;
    }

    public TestFile writelns(String... lines) {
        return writelns(Arrays.asList(lines));
    }

    public TestFile write(Object content) {
        try {
            FileUtils.writeStringToFile(this, content.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Could not write to test file '%s'", this), e);
        }
        return this;
    }

    public TestFile leftShift(Object content) {
        return write(content);
    }

    public String getText() {
        assertIsFile();
        try {
            return FileUtils.readFileToString(this);
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Could not read from test file '%s'", this), e);
        }
    }

    public List<String> linesThat(Matcher<? super String> matcher) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this));
            try {
                List<String> lines = new ArrayList<String>();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (matcher.matches(line)) {
                        lines.add(line);
                    }
                }
                return lines;
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void unzipTo(File target) {
        assertIsFile();
        new TestFileHelper(this).unzipTo(target, useNativeTools);
    }

    public void untarTo(File target) {
        assertIsFile();

        new TestFileHelper(this).untarTo(target, useNativeTools);
    }

    public void copyTo(File target) {
        try {
            FileUtils.copyFile(this, target);
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Could not copy test file '%s' to '%s'", this, target), e);
        }
    }

    public void copyFrom(URL resource) {
        try {
            FileUtils.copyURLToFile(resource, this);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public TestFile touch() {
        try {
            FileUtils.touch(this);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        assertIsFile();
        return this;
    }

    /**
     * Creates a directory structure specified by the given closure.
     * <pre>
     * dir.create {
     *     subdir1 {
     *        file 'somefile.txt'
     *     }
     *     subdir2 { nested { file 'someFile' } }
     * }
     * </pre>
     */
    public TestFile create(Closure structure) {
        assertTrue(isDirectory() || mkdirs());
        new TestDirHelper(this).apply(structure);
        return this;
    }

    @Override
    public String toString() {
        return getPath();
    }

    public TestFile writelns(Iterable<String> lines) {
        Formatter formatter = new Formatter();
        for (String line : lines) {
            formatter.format("%s%n", line);
        }
        return write(formatter);
    }

    public TestFile assertExists() {
        assertTrue(String.format("%s does not exist", this), exists());
        return this;
    }

    public TestFile assertIsFile() {
        assertTrue(String.format("%s is not a file", this), isFile());
        return this;
    }

    public TestFile assertIsDir() {
        assertTrue(String.format("%s is not a directory", this), isDirectory());
        return this;
    }

    public TestFile assertDoesNotExist() {
        assertFalse(String.format("%s should not exist", this), exists());
        return this;
    }

    public TestFile assertContents(Matcher<String> matcher) {
        assertThat(getText(), matcher);
        return this;
    }

    public TestFile assertPermissions(Matcher<String> matcher) {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            assertThat(String.format("mismatched permissions for '%s'", this), getPermissions(), matcher);
        }
        return this;
    }

    private String getPermissions() {
        return new TestFileHelper(this).getPermissions();
    }

    /**
     * Asserts that this file contains exactly the given set of descendants.
     */
    public TestFile assertHasDescendants(String... descendants) {
        Set<String> actual = new TreeSet<String>();
        assertIsDir();
        visit(actual, "", this);
        Set<String> expected = new TreeSet<String>(Arrays.asList(descendants));
        assertEquals(expected, actual);
        return this;
    }

    private void visit(Set<String> names, String prefix, File file) {
        for (File child : file.listFiles()) {
            if (child.isFile()) {
                names.add(prefix + child.getName());
            } else if (child.isDirectory()) {
                visit(names, prefix + child.getName() + "/", child);
            }
        }
    }

    public boolean isSelfOrDescendent(File file) {
        if (file.getAbsolutePath().equals(getAbsolutePath())) {
            return true;
        }
        return file.getAbsolutePath().startsWith(getAbsolutePath() + File.separatorChar);
    }

    public TestFile createDir() {
        assertTrue(isDirectory() || mkdirs());
        return this;
    }

    public TestFile deleteDir() {
        GradleUtil.deleteDir(this);
        return this;
    }

    public TestFile createFile() {
        new TestFile(getParentFile()).createDir();
        try {
            assertTrue(isFile() || createNewFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public TestFile zipTo(TestFile zipFile) {
        Zip zip = new Zip();
        zip.setBasedir(this);
        zip.setDestFile(zipFile);
        AntUtil.execute(zip);
        return this;
    }

    public TestFile tarTo(TestFile zipFile) {
        Tar tar = new Tar();
        tar.setBasedir(this);
        tar.setDestFile(zipFile);
        AntUtil.execute(tar);
        return this;
    }

    public Snapshot snapshot() {
        assertIsFile();
        return new Snapshot();
    }

    public void assertHasChangedSince(Snapshot snapshot) {
        Snapshot now = snapshot();
        assertTrue(now.modTime != snapshot.modTime || !Arrays.equals(now.hash, snapshot.hash));
    }

    public void assertHasNotChangedSince(Snapshot snapshot) {
        Snapshot now = snapshot();
        assertEquals(now.modTime, snapshot.modTime);
        assertArrayEquals(now.hash, snapshot.hash);
    }

    public class Snapshot {
        private final long modTime;
        private final byte[] hash;

        public Snapshot() {
            modTime = lastModified();
            hash = HashUtil.createHash(TestFile.this);
        }
    }
}
