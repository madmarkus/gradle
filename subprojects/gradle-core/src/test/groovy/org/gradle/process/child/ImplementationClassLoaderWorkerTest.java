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

package org.gradle.process.child;

import org.gradle.api.Action;
import org.gradle.api.logging.LogLevel;
import org.gradle.initialization.LoggingConfigurer;
import org.gradle.util.JUnit4GroovyMockery;
import org.gradle.util.ObservableUrlClassLoader;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import static org.gradle.util.WrapUtil.*;

@RunWith(JMock.class)
public class ImplementationClassLoaderWorkerTest {
    private final JUnit4Mockery context = new JUnit4GroovyMockery();
    private final ClassLoader applicationClassLoader = getClass().getClassLoader();
    private final LoggingConfigurer loggingConfigurer = context.mock(LoggingConfigurer.class);
    private final ObservableUrlClassLoader implementationClassLoader = new ObservableUrlClassLoader(applicationClassLoader);
    private final WorkerContext workerContext = context.mock(WorkerContext.class);
    private final SerializableMockHelper helper = new SerializableMockHelper();

    @Test
    public void createsClassLoaderAndInstantiatesAndExecutesWorker() throws Exception {
        final Action<WorkerContext> action = context.mock(Action.class);
        final List<URL> implementationClassPath = toList(new File(".").toURL());

        Action<WorkerContext> serializableAction = helper.serializable(action, implementationClassLoader);
        ImplementationClassLoaderWorker worker = new TestImplementationClassLoaderWorker(LogLevel.DEBUG, toList("a", "b"), implementationClassPath, serializableAction);

        context.checking(new Expectations() {{
            one(loggingConfigurer).configure(LogLevel.DEBUG);
            allowing(workerContext).getApplicationClassLoader();
            will(returnValue(applicationClassLoader));
            one(action).execute(workerContext);
        }});


        worker.execute(workerContext);
    }

    private class TestImplementationClassLoaderWorker extends ImplementationClassLoaderWorker {
        private TestImplementationClassLoaderWorker(LogLevel logLevel, Collection<String> sharedPackages,
                                    Collection<URL> implementationClassPath, Action<WorkerContext> workerAction) {
            super(logLevel, sharedPackages, implementationClassPath, workerAction);
        }

        @Override
        protected LoggingConfigurer createLoggingConfigurer() {
            return loggingConfigurer;
        }

        @Override
        protected ObservableUrlClassLoader createImplementationClassLoader(ClassLoader system,
                                                                           ClassLoader application) {
            return implementationClassLoader;
        }
    }
}
