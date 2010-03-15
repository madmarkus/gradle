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

package org.gradle.process;

import org.gradle.api.internal.ClassPathRegistry;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.logging.LogLevel;
import org.gradle.messaging.MessagingServer;
import org.gradle.messaging.ObjectConnection;
import org.gradle.process.child.IsolatedApplicationClassLoaderWorkerFactory;
import org.gradle.process.child.SystemClassLoaderWorkerFactory;
import org.gradle.process.child.WorkerFactory;
import org.gradle.process.launcher.GradleWorkerMain;
import org.gradle.util.ClasspathUtil;
import org.gradle.util.GUtil;
import org.gradle.util.IdGenerator;
import org.gradle.util.exec.ExecHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

public class DefaultWorkerProcessFactory implements WorkerProcessFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWorkerProcessFactory.class);
    private final LogLevel workerLogLevel;
    private final MessagingServer server;
    private final ClassPathRegistry classPathRegistry;
    private final FileResolver resolver;
    private final IdGenerator<?> idGenerator;

    public DefaultWorkerProcessFactory(LogLevel workerLogLevel, MessagingServer server,
                                       ClassPathRegistry classPathRegistry, FileResolver resolver, IdGenerator<?> idGenerator) {
        this.workerLogLevel = workerLogLevel;
        this.server = server;
        this.classPathRegistry = classPathRegistry;
        this.resolver = resolver;
        this.idGenerator = idGenerator;
    }

    public WorkerProcessBuilder newProcess() {
        return new DefaultWorkerProcessBuilder();
    }

    private class DefaultWorkerProcessBuilder extends WorkerProcessBuilder {
        public DefaultWorkerProcessBuilder() {
            super(resolver);
            setLogLevel(workerLogLevel);
            getJavaCommand().mainClass(GradleWorkerMain.class.getName());
        }

        @Override
        public WorkerProcess build() {
            if (getWorker() == null) {
                throw new IllegalStateException("No worker action specified for this worker process.");
            }

            ObjectConnection connection = server.createUnicastConnection();

            List<URL> implementationClassPath = ClasspathUtil.getClasspath(getWorker().getClass().getClassLoader());
            Object id = idGenerator.generateId();
            String displayName = String.format("Gradle Worker %s", id);

            WorkerFactory workerFactory;
            if (isLoadApplicationInSystemClassLoader()) {
                workerFactory = new SystemClassLoaderWorkerFactory(id, displayName, this, implementationClassPath, connection.getLocalAddress(), classPathRegistry);
            } else {
                workerFactory = new IsolatedApplicationClassLoaderWorkerFactory(id, displayName, this, implementationClassPath, connection.getLocalAddress(), classPathRegistry);
            }
            Callable<?> workerMain = workerFactory.create();
            getJavaCommand().classpath(workerFactory.getSystemClasspath());

            // Build configuration for GradleWorkerMain
            byte[] config = GUtil.serialize(workerMain);

            LOGGER.debug("Creating {}", displayName);
            LOGGER.debug("Using application classpath {}", getApplicationClasspath());
            LOGGER.debug("Using implementation classpath {}", implementationClassPath);

            getJavaCommand().standardInput(new ByteArrayInputStream(config));
            ExecHandle execHandle = getJavaCommand().build();

            return new DefaultWorkerProcess(connection, execHandle);
        }
    }
}
