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
package org.gradle.api.internal.artifacts.ivyservice;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.*;
import org.gradle.api.internal.artifacts.DefaultResolvedArtifactTest;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;
import org.gradle.util.GUtil;
import org.gradle.util.WrapUtil;
import static org.gradle.util.WrapUtil.toList;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * @author Hans Dockter
 */
@RunWith(JMock.class)
public class DefaultIvyDependencyResolverTest {
    private JUnit4Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private Configuration configurationStub = context.mock(Configuration.class, "<configuration>");
    private Ivy ivyStub = context.mock(Ivy.class);
    private DefaultIvyReportConverter ivyReportConverterStub = context.mock(DefaultIvyReportConverter.class);
    private ResolveReport resolveReportMock = context.mock(ResolveReport.class);

    private DefaultIvyDependencyResolver ivyDependencyResolver = new DefaultIvyDependencyResolver(ivyReportConverterStub);

    @Before
    public void setUp() {
        context.checking(new Expectations() {{
            allowing(configurationStub).getName();
            will(returnValue("someConfName"));
        }});
    }

    @Test
    public void testResolveAndGetFilesWithDependencySubset() throws IOException, ParseException {
        prepareResolveReport();
        final ModuleDependency moduleDependencyDummy1 = context.mock(ModuleDependency.class, "dep1");
        final ModuleDependency moduleDependencyDummy2 = context.mock(ModuleDependency.class, "dep2");
        final SelfResolvingDependency selfResolvingDependencyDummy = context.mock(SelfResolvingDependency.class);
        final ResolvedDependency resolvedDependency1 = context.mock(ResolvedDependency.class, "resolved1");
        final ResolvedDependency resolvedDependency2 = context.mock(ResolvedDependency.class, "resolved2");
        ResolvedDependency resolvedDependency3 = context.mock(ResolvedDependency.class, "resolved3");
        final IvyConversionResult conversionResultStub = context.mock(IvyConversionResult.class);
        final Map<Dependency, Set<ResolvedDependency>> firstLevelResolvedDependencies = GUtil.map(
                moduleDependencyDummy1,
                WrapUtil.toSet(resolvedDependency1, resolvedDependency2),
                moduleDependencyDummy2,
                WrapUtil.toSet(resolvedDependency3));

        context.checking(new Expectations() {{
            allowing(resolvedDependency1).getAllArtifacts(null);
            will(returnValue(WrapUtil.toSet(DefaultResolvedArtifactTest.createResolvedArtifact(context, "someName", "someType", "someExtension", new File("file1")))));
            allowing(resolvedDependency2).getAllArtifacts(null);
            will(returnValue(WrapUtil.toSet(DefaultResolvedArtifactTest.createResolvedArtifact(context, "someName2", "someType", "someExtension", new File("file2")))));
            allowing(configurationStub).getAllDependencies();
            will(returnValue(WrapUtil.toSet(moduleDependencyDummy1, moduleDependencyDummy2, selfResolvingDependencyDummy)));
            allowing(configurationStub).getAllDependencies(ModuleDependency.class);
            will(returnValue(WrapUtil.toSet(moduleDependencyDummy1, moduleDependencyDummy2)));
            allowing(ivyReportConverterStub).convertReport(resolveReportMock, configurationStub);
            will(returnValue(conversionResultStub));
            allowing(conversionResultStub).getFirstLevelResolvedDependencies();
            will(returnValue(firstLevelResolvedDependencies));
        }});
        ModuleDescriptor moduleDescriptor = createAnonymousModuleDescriptor();
        prepareTestsThatRetrieveDependencies(moduleDescriptor);

        Set<File> actualFiles = ivyDependencyResolver.resolve(configurationStub, ivyStub, moduleDescriptor).getFiles(
                new Spec<Dependency>() {
                    public boolean isSatisfiedBy(Dependency element) {
                        return element == moduleDependencyDummy1 || element == selfResolvingDependencyDummy;
                    }
                });
        assertThat(actualFiles, equalTo(WrapUtil.toSet(new File("file1"), new File("file2"))));
    }

    @Test
    public void testGetModuleDependencies() throws IOException, ParseException {
        prepareResolveReport();
        final ModuleDependency moduleDependencyDummy1 = context.mock(ModuleDependency.class, "dep1");
        final ResolvedDependency resolvedDependency1 = context.mock(ResolvedDependency.class, "resolved1");
        final ResolvedDependency resolvedDependency2 = context.mock(ResolvedDependency.class, "resolved2");
        final IvyConversionResult conversionResultStub = context.mock(IvyConversionResult.class);
        Set<ResolvedDependency> resolvedDependenciesSet = WrapUtil.toSet(resolvedDependency1, resolvedDependency2);
        final Map<Dependency, Set<ResolvedDependency>> firstLevelResolvedDependencies = GUtil.map(
                moduleDependencyDummy1,
                resolvedDependenciesSet);

        context.checking(new Expectations() {{
            allowing(ivyReportConverterStub).convertReport(resolveReportMock, configurationStub);
            will(returnValue(conversionResultStub));
            allowing(conversionResultStub).getFirstLevelResolvedDependencies();
            will(returnValue(firstLevelResolvedDependencies));
        }});
        ModuleDescriptor moduleDescriptor = createAnonymousModuleDescriptor();
        prepareTestsThatRetrieveDependencies(moduleDescriptor);

        Set<ResolvedDependency> actualFirstLevelModuleDependencies = ivyDependencyResolver.resolve(configurationStub, ivyStub, moduleDescriptor).getFirstLevelModuleDependencies();
        assertThat(actualFirstLevelModuleDependencies, equalTo(resolvedDependenciesSet));
    }

    @Test
    public void testGetResolvedArtifacts() throws IOException, ParseException {
        prepareResolveReport();
        final IvyConversionResult conversionResultStub = context.mock(IvyConversionResult.class);
        final ResolvedArtifact resolvedArtifactDummy = context.mock(ResolvedArtifact.class);
        final Set<ResolvedArtifact> resolvedArtifacts = WrapUtil.toSet(resolvedArtifactDummy);
        context.checking(new Expectations() {{
            allowing(ivyReportConverterStub).convertReport(resolveReportMock, configurationStub);
            will(returnValue(conversionResultStub));
            allowing(conversionResultStub).getResolvedArtifacts();
            will(returnValue(resolvedArtifacts));
        }});
        ModuleDescriptor moduleDescriptor = createAnonymousModuleDescriptor();
        prepareTestsThatRetrieveDependencies(moduleDescriptor);
        assertThat(ivyDependencyResolver.resolve(configurationStub, ivyStub, moduleDescriptor)
                .getResolvedArtifacts(), equalTo(resolvedArtifacts));
    }

    @Test
    public void testResolveAndGetFilesWithMissingDependenciesShouldThrowGradleEx() throws IOException, ParseException {
        ModuleDescriptor moduleDescriptor = createAnonymousModuleDescriptor();
        prepareTestsThatRetrieveDependencies(moduleDescriptor);
        prepareResolveReportWithError();
        ResolvedConfiguration configuration = ivyDependencyResolver.resolve(configurationStub, ivyStub, moduleDescriptor);
        context.checking(new Expectations() {{
            allowing(configurationStub).getAllDependencies();
            allowing(configurationStub).getAllDependencies(ModuleDependency.class);
        }});
        try {
            configuration.getFiles(Specs.SATISFIES_ALL);
            fail();
        } catch (GradleException e) {
            assertThat(e.getMessage(), startsWith("Could not resolve all dependencies for <configuration>"));
        }
    }

    @Test
    public void testResolveAndRethrowFailureWithMissingDependenciesShouldThrowGradleEx() throws IOException, ParseException {
        ModuleDescriptor moduleDescriptor = createAnonymousModuleDescriptor();
        prepareTestsThatRetrieveDependencies(moduleDescriptor);
        prepareResolveReportWithError();
        ResolvedConfiguration configuration = ivyDependencyResolver.resolve(configurationStub, ivyStub,
                moduleDescriptor);

        assertTrue(configuration.hasError());
        try {
            configuration.rethrowFailure();
            fail();
        } catch (GradleException e) {
            assertThat(e.getMessage(), startsWith("Could not resolve all dependencies for <configuration>"));
        }
    }

    @Test
    public void testResolveAndRethrowFailureWithNoMissingDependenciesShouldDoNothing() throws IOException, ParseException {
        ModuleDescriptor moduleDescriptor = createAnonymousModuleDescriptor();
        prepareTestsThatRetrieveDependencies(moduleDescriptor);
        prepareResolveReport();
        context.checking(new Expectations() {{
            allowing(ivyReportConverterStub).convertReport(resolveReportMock, configurationStub);
        }});
        ResolvedConfiguration configuration = ivyDependencyResolver.resolve(configurationStub, ivyStub,
                moduleDescriptor);

        assertFalse(configuration.hasError());
        configuration.rethrowFailure();
    }

    @Test
    public void testResolveAndGetReport() throws IOException, ParseException {
        prepareResolveReport();
        context.checking(new Expectations() {{
            allowing(ivyReportConverterStub).convertReport(resolveReportMock, configurationStub);
        }});
        ModuleDescriptor moduleDescriptor = createAnonymousModuleDescriptor();
        prepareTestsThatRetrieveDependencies(moduleDescriptor);
        assertEquals(false, ivyDependencyResolver.resolve(configurationStub, ivyStub, moduleDescriptor).hasError());
    }

    @Test
    public void testResolveAndGetReportWithMissingDependenciesAndFailFalse() throws IOException, ParseException {
        prepareResolveReportWithError();
        ModuleDescriptor moduleDescriptor = createAnonymousModuleDescriptor();
        prepareTestsThatRetrieveDependencies(moduleDescriptor);
        assertEquals(true, ivyDependencyResolver.resolve(configurationStub, ivyStub, moduleDescriptor).hasError());
    }

    private ModuleDescriptor createAnonymousModuleDescriptor() {
        return DefaultModuleDescriptor.newDefaultInstance(
                ModuleRevisionId.newInstance("org", "name", "1.0", new HashMap()));
    }

    private void prepareResolveReport() throws IOException, ParseException {
        context.checking(new Expectations() {
            {
                allowing(resolveReportMock).hasError();
                will(returnValue(false));
            }
        });
    }

    private void prepareResolveReportWithError() throws IOException, ParseException {
        context.checking(new Expectations() {
            {
                allowing(resolveReportMock).hasError();
                will(returnValue(true));
                allowing(resolveReportMock).getAllProblemMessages();
                will(returnValue(toList("a problem")));
            }
        });
    }

    private void prepareTestsThatRetrieveDependencies(final ModuleDescriptor moduleDescriptor) throws IOException, ParseException {
        final String confName = configurationStub.getName();
        context.checking(new Expectations() {
            {
                allowing(ivyStub).resolve(with(equal(moduleDescriptor)), with(equaltResolveOptions(confName)));
                will(returnValue(resolveReportMock));
            }
        });
    }

    Matcher<ResolveOptions> equaltResolveOptions(final String... confs) {
        return new BaseMatcher<ResolveOptions>() {
            public boolean matches(Object o) {
                ResolveOptions otherOptions = (ResolveOptions) o;
                return Arrays.equals(confs, otherOptions.getConfs());
            }

            public void describeTo(Description description) {
                description.appendText("Checking Resolveoptions");
            }
        };
    }

}
