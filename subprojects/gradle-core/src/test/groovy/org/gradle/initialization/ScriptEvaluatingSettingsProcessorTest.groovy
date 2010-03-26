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

package org.gradle.initialization

import groovy.mock.interceptor.MockFor
import org.gradle.StartParameter
import org.gradle.util.JUnit4GroovyMockery
import org.jmock.lib.legacy.ClassImposteriser
import org.junit.Before
import org.junit.Test
import org.gradle.groovy.scripts.*

import static org.junit.Assert.*
import org.gradle.configuration.ScriptPluginFactory
import org.gradle.configuration.ScriptPlugin
import org.gradle.api.internal.GradleInternal

/**
 * @author Hans Dockter
 */
class ScriptEvaluatingSettingsProcessorTest {
    static final File TEST_ROOT_DIR = new File('rootDir')
    static final File TEST_CURRENT_DIR = new File('currentDir')
    ScriptEvaluatingSettingsProcessor settingsProcessor
    DefaultSettingsFinder expectedSettingsFinder
    SettingsFactory settingsFactory
    StartParameter expectedStartParameter
    DefaultSettings expectedSettings
    MockFor settingsFactoryMocker
    ScriptSource scriptSourceMock
    IGradlePropertiesLoader propertiesLoaderMock
    ScriptPluginFactory configurerFactoryMock
    Map expectedGradleProperties
    URLClassLoader urlClassLoader
    GradleInternal gradleMock

    JUnit4GroovyMockery context = new JUnit4GroovyMockery()

    @Before public void setUp() {
        context.setImposteriser(ClassImposteriser.INSTANCE)
        configurerFactoryMock = context.mock(ScriptPluginFactory)
        settingsFactory = context.mock(SettingsFactory)
        settingsProcessor = new ScriptEvaluatingSettingsProcessor(configurerFactoryMock, settingsFactory)
        expectedSettingsFinder = new DefaultSettingsFinder()
        scriptSourceMock = context.mock(ScriptSource)
        gradleMock = context.mock(GradleInternal)
        expectedStartParameter = new StartParameter()
        expectedGradleProperties = [a: 'b']
        propertiesLoaderMock = [getGradleProperties: { expectedGradleProperties }] as IGradlePropertiesLoader
        urlClassLoader = new URLClassLoader(new URL[0]);
        initExpectedSettings()
    }

    private void initExpectedSettings() {
        expectedSettings = new DefaultSettings()
        DefaultProjectDescriptorRegistry projectDescriptorRegistry = new DefaultProjectDescriptorRegistry()
        expectedSettings.setRootProjectDescriptor(new DefaultProjectDescriptor(null, TEST_ROOT_DIR.name,
                TEST_ROOT_DIR, projectDescriptorRegistry))
        expectedSettings.setProjectDescriptorRegistry(projectDescriptorRegistry)
        expectedSettings.setStartParameter(expectedStartParameter)
        context.checking {
            one(settingsFactory).createSettings(gradleMock, TEST_ROOT_DIR, scriptSourceMock, expectedGradleProperties, expectedStartParameter, urlClassLoader)
            will(returnValue(expectedSettings))
        }
    }

    @Test public void testProcessWithSettingsFile() {
        expectedStartParameter.setCurrentDir(TEST_ROOT_DIR)
        ScriptPlugin configurerMock = context.mock(ScriptPlugin)

        context.checking {
            one(configurerFactoryMock).create(scriptSourceMock)
            will(returnValue(configurerMock))

            one(configurerMock).setClassLoader(urlClassLoader)
            one(configurerMock).setScriptBaseClass(SettingsScript)
            one(configurerMock).apply(expectedSettings)
        }
        
        SettingsLocation settingsLocation = new SettingsLocation(TEST_ROOT_DIR, scriptSourceMock)
        assertSame(expectedSettings, settingsProcessor.process(gradleMock, settingsLocation, urlClassLoader, expectedStartParameter, propertiesLoaderMock))
    }
}
