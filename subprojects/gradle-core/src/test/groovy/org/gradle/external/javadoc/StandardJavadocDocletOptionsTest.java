/*
 * Copyright 2009 the original author or authors.
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

package org.gradle.external.javadoc;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.jmock.Expectations;
import org.gradle.external.javadoc.optionfile.JavadocOptionFile;
import org.gradle.external.javadoc.optionfile.LinksOfflineJavadocOptionFileOption;
import org.gradle.external.javadoc.optionfile.GroupsJavadocOptionFileOption;

import java.util.*;
import java.io.File;

/**
 * @author Tom Eyckmans
 */
public class StandardJavadocDocletOptionsTest {

    private final JUnit4Mockery context = new JUnit4Mockery();
    private StandardJavadocDocletOptions options;

    @Before
    public void setUp() {
        context.setImposteriser(ClassImposteriser.INSTANCE);

        options = new StandardJavadocDocletOptions();
    }

    @Test
    public void testDefaults() {
        // core javadoc options
        assertNull(options.getOverview());
        assertNull(options.getMemberLevel());
        assertNull(options.getDoclet());
        assertEmpty(options.getDocletClasspath());
        assertNull(options.getSource());
        assertEmpty(options.getSourcepath());
        assertEmpty(options.getClasspath());
        assertEmpty(options.getSubPackages());
        assertEmpty(options.getExclude());
        assertEmpty(options.getBootClasspath());
        assertEmpty(options.getExtDirs());
        assertEquals(options.getOutputLevel(), JavadocOutputLevel.QUIET);
        assertFalse(options.isBreakIterator());
        assertNull(options.getLocale());
        assertNull(options.getEncoding());
        assertEmpty(options.getJFlags());
        assertEmpty(options.getPackageNames());
        assertEmpty(options.getSourceNames());
        assertEmpty(options.getOptionFiles());
        // standard doclet options
        assertNull(options.getDestinationDirectory());
        assertFalse(options.isUse());
        assertFalse(options.isVersion());
        assertFalse(options.isAuthor());
        assertFalse(options.isSplitIndex());
        assertNull(options.getWindowTitle());
        assertNull(options.getDocTitle());
        assertNull(options.getFooter());
        assertNull(options.getBottom());
        assertEmpty(options.getLinks());
        assertEmpty(options.getLinksOffline());
        assertFalse(options.isLinkSource());
        assertEmpty(options.getGroups());
        assertFalse(options.isNoDeprecated());
        assertFalse(options.isNoDeprecatedList());
        assertFalse(options.isNoSince());
        assertFalse(options.isNoTree());
        assertFalse(options.isNoIndex());
        assertFalse(options.isNoHelp());
        assertFalse(options.isNoNavBar());
        assertNull(options.getHelpFile());
        assertNull(options.getStylesheetFile());
        assertFalse(options.isSerialWarn());
        assertNull(options.getCharSet());
        assertNull(options.getDocEncoding());
        assertFalse(options.isKeyWords());
        assertEmpty(options.getTags());
        assertEmpty(options.getTagletPath());
        assertFalse(options.isDocFilesSubDirs());
        assertEmpty(options.getExcludeDocFilesSubDir());
        assertEmpty(options.getNoQualifiers());
        assertFalse(options.isNoTimestamp());
        assertFalse(options.isNoComment());
    }

    @Test
    public void testConstructor() {
        final JavadocOptionFile optionFileMock = context.mock(JavadocOptionFile.class);

        context.checking(new Expectations(){{
            // core options
            one(optionFileMock).addStringOption("overview");
            one(optionFileMock).addEnumOption("memberLevel");
            one(optionFileMock).addStringOption("doclet");
            one(optionFileMock).addPathOption("docletclasspath");
            one(optionFileMock).addStringOption("source");
            one(optionFileMock).addPathOption("sourcepath");
            one(optionFileMock).addPathOption("classpath");
            one(optionFileMock).addStringsOption("subpackages", ";");
            one(optionFileMock).addStringsOption("exclude", ":");
            one(optionFileMock).addPathOption("bootclasspath");
            one(optionFileMock).addPathOption("extdirs");
            one(optionFileMock).addEnumOption("outputLevel", JavadocOutputLevel.QUIET);
            one(optionFileMock).addBooleanOption("breakiterator");
            one(optionFileMock).addStringOption("locale");
            one(optionFileMock).addStringOption("encoding");
            // standard doclet options
            one(optionFileMock).addFileOption("d");
            one(optionFileMock).addBooleanOption("use");
            one(optionFileMock).addBooleanOption("version");
            one(optionFileMock).addBooleanOption("author");
            one(optionFileMock).addBooleanOption("splitindex");
            one(optionFileMock).addStringOption("windowtitle");
            one(optionFileMock).addStringOption("doctitle");
            one(optionFileMock).addStringOption("footer");
            one(optionFileMock).addStringOption("bottom");
            one(optionFileMock).addStringOption("link");
            allowing(optionFileMock).addOption(new LinksOfflineJavadocOptionFileOption("linkoffline"));
            one(optionFileMock).addBooleanOption("linksource");
            one(optionFileMock).addOption(new GroupsJavadocOptionFileOption("group"));
            one(optionFileMock).addBooleanOption("nodeprecated");
            one(optionFileMock).addBooleanOption("nodeprecatedlist");
            one(optionFileMock).addBooleanOption("nosince");
            one(optionFileMock).addBooleanOption("notree");
            one(optionFileMock).addBooleanOption("noindex");
            one(optionFileMock).addBooleanOption("nohelp");
            one(optionFileMock).addBooleanOption("nonavbar");
            one(optionFileMock).addFileOption("helpfile");
            one(optionFileMock).addFileOption("stylesheetfile");
            one(optionFileMock).addBooleanOption("serialwarn");
            one(optionFileMock).addStringOption("charset");
            one(optionFileMock).addStringOption("docencoding");
            one(optionFileMock).addBooleanOption("keywords");
            one(optionFileMock).addStringOption("tags");
            one(optionFileMock).addPathOption("tagletpath");
            one(optionFileMock).addBooleanOption("docfilessubdirs");
            one(optionFileMock).addStringsOption("excludedocfilessubdir", ":");
            one(optionFileMock).addStringsOption("noqualifier", ":");
            one(optionFileMock).addBooleanOption("notimestamp");
            one(optionFileMock).addBooleanOption("nocomment");
        }});

        options = new StandardJavadocDocletOptions();
    }

    @Test
    public void testFluentOverview() {
        final String overviewValue = "overview";
        assertEquals(options, options.overview(overviewValue));
        assertEquals(overviewValue, options.getOverview());
    }

    @Test
    public void testShowAll() {
        assertEquals(options, options.showAll());
        assertEquals(JavadocMemberLevel.PRIVATE, options.getMemberLevel());
    }

    @Test
    public void testShowFromPublic() {
        assertEquals(options, options.showFromPublic());
        assertEquals(JavadocMemberLevel.PUBLIC, options.getMemberLevel());
    }

    @Test
    public void testShowFromPackage() {
        assertEquals(options, options.showFromPackage());
        assertEquals(JavadocMemberLevel.PACKAGE, options.getMemberLevel());
    }

    @Test
    public void testShowFromProtected() {
        assertEquals(options, options.showFromProtected());
        assertEquals(JavadocMemberLevel.PROTECTED, options.getMemberLevel());
    }

    @Test
    public void testShowFromPrivate() {
        assertEquals(options, options.showFromPrivate());
        assertEquals(JavadocMemberLevel.PRIVATE, options.getMemberLevel());
    }

    @Test
    public void testFluentDocletClass() {
        final String docletValue = "org.gradle.CustomDocletClass";
        assertEquals(options, options.doclet(docletValue));
        assertEquals(docletValue, options.getDoclet());
    }

    @Test
    public void testFluentDocletClasspath() {
        final File[] docletClasspathValue = new File[]{new File("doclet.jar"), new File("doclet-dep.jar")};
        assertEquals(options, options.docletClasspath(docletClasspathValue));
        assertArrayEquals(docletClasspathValue, options.getDocletClasspath().toArray());
    }

    @Test
    public void testFluentSource() {
        final String sourceValue = "1.5";
        assertEquals(options, options.source(sourceValue));
        assertEquals(sourceValue, options.getSource());
    }

    @Test
    public void testFluentSourcepath() {
        final File[] sourcepathValue = new File[]{new File("sources"), new File("sources2")};
        assertEquals(options, options.sourcepath(sourcepathValue));
        assertArrayEquals(sourcepathValue, options.getSourcepath().toArray());
    }

    @Test
    public void testFluentClasspath() {
        final File[] classpathValue = new File[]{new File("classpath.jar"), new File("classpath-dir")};
        assertEquals(options, options.classpath(classpathValue));
        assertArrayEquals(classpathValue, options.getClasspath().toArray());
    }

    @Test
    public void testFluentSubpackages() {
        final String[] subPackagesValue = new String[]{"org.gradle.util", "org.gradle.api"};
        assertEquals(options, options.subPackages(subPackagesValue));
        assertArrayEquals(subPackagesValue, options.getSubPackages().toArray());
    }

    @Test
    public void testFluentExcludes() {
        final String[] excludesValue = new String[]{"org.gradle.util.exec", "org.gradle.api.internal"};
        assertEquals(options, options.exclude(excludesValue));
        assertArrayEquals(excludesValue, options.getExclude().toArray());
    }

    @Test
    public void testFluentBootclasspath() {
        final File[] bootClasspathValue = new File[]{new File("bootclasspath.jar"), new File("bootclasspath2.jar")};
        assertEquals(options, options.bootClasspath(bootClasspathValue));
        assertArrayEquals(bootClasspathValue, options.getBootClasspath().toArray());
    }

    @Test
    public void testFluentExtDirs() {
        final File[] extDirsValue = new File[]{new File("extDirOne"), new File("extDirTwo")};
        assertEquals(options, options.extDirs(extDirsValue));
        assertArrayEquals(extDirsValue, options.getExtDirs().toArray());
    }

    @Test
    public void testQuietOutputLevel() {
        assertEquals(options, options.quiet());
        assertEquals(JavadocOutputLevel.QUIET, options.getOutputLevel());
    }

    @Test
    public void testVerboseOutputLevel() {
        assertEquals(options, options.verbose());
        assertEquals(JavadocOutputLevel.VERBOSE, options.getOutputLevel());
        assertTrue(options.isVerbose());
    }

    @Test
    public void testFluentBreakIterator() {
        assertEquals(options, options.breakIterator());
        assertTrue(options.isBreakIterator());
    }

    @Test
    public void testFluentLocale() {
        final String localeValue = "nl";
        assertEquals(options, options.locale(localeValue));
        assertEquals(localeValue, options.getLocale());
    }

    @Test
    public void testFluentEncoding() {
        final String encodingValue = "UTF-8";
        assertEquals(options, options.encoding(encodingValue));
        assertEquals(encodingValue, options.getEncoding());
    }

    @Test
    public void testFluentDirectory() {
        final File directoryValue = new File("testOutput");
        assertEquals(options, options.destinationDirectory(directoryValue));
        assertEquals(directoryValue, options.getDestinationDirectory());
    }

    @Test
    public void testFluentUse() {
        assertEquals(options, options.use());
        assertTrue(options.isUse());
    }

    @Test
    public void testFluentVersion() {
        assertEquals(options, options.version());
        assertTrue(options.isVersion());
    }

    @Test
    public void testFluentAuthor() {
        assertEquals(options, options.author());
        assertTrue(options.isAuthor());
    }

    @Test
    public void testFluentSplitIndex() {
        assertEquals(options, options.splitIndex());
        assertTrue(options.isSplitIndex());
    }

    @Test
    public void testFluentWindowTitle() {
        final String windowTitleValue = "windowTitleValue";
        assertEquals(options, options.windowTitle(windowTitleValue));
        assertEquals(windowTitleValue, options.getWindowTitle());
    }

    @Test
    public void testFluentDocTitle() {
        final String docTitleValue = "docTitleValue";
        assertEquals(options, options.docTitle(docTitleValue));
        assertEquals(docTitleValue, options.getDocTitle());
    }

    @Test
    public void testFluentFooter() {
        final String footerValue = "footerValue";
        assertEquals(options, options.footer(footerValue));
        assertEquals(footerValue, options.getFooter());
    }

    @Test
    public void testFluentBottom() {
        final String bottomValue = "bottomValue";
        assertEquals(options, options.bottom(bottomValue));
        assertEquals(bottomValue, options.getBottom());
    }

    @Test
    public void testFluentLink() {
        final String[] linkValue = new String[]{"http://otherdomain.org/javadoc"};
        assertEquals(options, options.links(linkValue));
        assertArrayEquals(linkValue, options.getLinks().toArray());
    }

    @Test
    public void testFluentLinkOffline() {
        final String extDocUrl = "http://otherdomain.org/javadoc";
        final String packageListLoc = "/home/someuser/used-lib-local-javadoc-list";
        assertEquals(options, options.linksOffline(extDocUrl, packageListLoc));
        assertEquals(extDocUrl, options.getLinksOffline().get(0).getExtDocUrl());
        assertEquals(packageListLoc, options.getLinksOffline().get(0).getPackagelistLoc());
    }

    @Test
    public void testFluentLinkSource() {
        assertEquals(options, options.linkSource());
        assertTrue(options.isLinkSource());
    }

    @Test
    public void testFluentGroup() {
        final String groupOneName = "groupOneName";
        final String[] groupOnePackages = new String[]{"java.lang", "java.io"};

        final String groupTwoName = "gradle";
        final String[] groupTwoPackages = new String[]{"org.gradle"};

        assertEquals(options, options.group(groupOneName, groupOnePackages));
        assertEquals(options, options.group(groupTwoName, groupTwoPackages));
        assertEquals(2, options.getGroups().size());
        assertArrayEquals(groupOnePackages, options.getGroups().get(groupOneName).toArray());
        assertArrayEquals(groupTwoPackages, options.getGroups().get(groupTwoName).toArray());
    }

    @Test
    public void testFluentNoDeprecated() {
        assertEquals(options, options.noDeprecated());
        assertTrue(options.isNoDeprecated());
    }

    @Test
    public void testFluentNoDeprecatedList() {
        assertEquals(options, options.noDeprecatedList());
        assertTrue(options.isNoDeprecatedList());
    }

    @Test
    public void testFluentNoSince() {
        assertEquals(options, options.noSince());
        assertTrue(options.isNoSince());
    }

    @Test
    public void testFluentNoTree() {
        assertEquals(options, options.noTree());
        assertTrue(options.isNoTree());
    }

    @Test
    public void testFluentNoIndex() {
        assertEquals(options, options.noIndex());
        assertTrue(options.isNoIndex());
    }

    @Test
    public void testFluentNoNavBar() {
        assertEquals(options, options.noNavBar());
        assertTrue(options.isNoNavBar());
    }

    @Test
    public void testFluentHelpFile() {
        final File helpFileValue = new File("help-file.txt");
        assertEquals(options, options.helpFile(helpFileValue));
        assertEquals(helpFileValue, options.getHelpFile());
    }

    @Test
    public void testFluentStylesheetFile() {
        final File stylesheetFileValue = new File("stylesheet.css");
        assertEquals(options, options.stylesheetFile(stylesheetFileValue));
        assertEquals(stylesheetFileValue, options.getStylesheetFile());
    }

    @Test
    public void testFluentSerialWarn() {
        assertEquals(options, options.serialWarn());
        assertTrue(options.isSerialWarn());
    }

    @Test
    public void testFluentCharset() {
        final String charsetValue = "dummy-charset";
        assertEquals(options, options.charSet(charsetValue));
        assertEquals(charsetValue, options.getCharSet());
    }

    @Test
    public void testFluentDocEncoding() {
        final String docEncodingValue = "UTF-16";
        assertEquals(options, options.docEncoding(docEncodingValue));
        assertEquals(docEncodingValue, options.getDocEncoding());
    }

    @Test
    public void testFluentKeywords() {
        assertEquals(options, options.keyWords());
        assertTrue(options.isKeyWords());
    }

    @Test
    public void testFluentTagsAndTaglets() {
        final String[] tagletsValue = new String[]{"com.sun.tools.doclets.ToDoTaglet"};
        final String[] tagsValue = new String[]{"param", "return", "todo:a:\"To Do:\""};

        final List<String> tempList = new ArrayList<String>();
        tempList.addAll(Arrays.asList(tagletsValue));
        tempList.addAll(Arrays.asList(tagsValue));

        final Object[] totalTagsValue = tempList.toArray();
        assertEquals(options, options.taglets(tagletsValue));
        assertEquals(options, options.tags(tagsValue));
        assertArrayEquals(totalTagsValue, options.getTags().toArray());
    }

    @Test
    public void testFluentTagletPath() {
        final File[] tagletPathValue = new File[]{new File("tagletOne.jar"), new File("tagletTwo.jar")};
        assertEquals(options, options.tagletPath(tagletPathValue));
        assertArrayEquals(tagletPathValue, options.getTagletPath().toArray());
    }

    @Test
    public void testFluentDocFilesSubDirs() {
        assertEquals(options, options.docFilesSubDirs());
        assertTrue(options.isDocFilesSubDirs());
    }

    @Test
    public void testFluentExcludeDocFilesSubDir() {
        final String[] excludeDocFilesSubDirValue = new String[]{".hg",".svn",".bzr", ".git"};
        assertEquals(options, options.excludeDocFilesSubDir(excludeDocFilesSubDirValue));
        assertArrayEquals(excludeDocFilesSubDirValue, options.getExcludeDocFilesSubDir().toArray());
    }

    @Test
    public void testFluentNoQualifier() {
        String[] noQualifierValue = new String[]{"java.lang", "java.io"};
        assertEquals(options, options.noQualifiers(noQualifierValue));
        assertArrayEquals(noQualifierValue, options.getNoQualifiers().toArray());
    }

    @Test
    public void testFluetNoTimestamp() {
        assertEquals(options, options.noTimestamp());
        assertTrue(options.isNoTimestamp());
    }

    @Test
    public void testFluentNoComment() {
        assertEquals(options, options.noComment());
        assertTrue(options.isNoComment());
    }

    @After
    public void tearDown() {
        options = null;
    }

    public static void assertEmpty(Collection shouldBeEmptyCollection) {
        assertNotNull(shouldBeEmptyCollection);
        assertTrue(shouldBeEmptyCollection.isEmpty());
    }

    public static void assertEmpty(Map shouldBeEmptyMap) {
        assertNotNull(shouldBeEmptyMap);
        assertTrue(shouldBeEmptyMap.isEmpty());
    }
}