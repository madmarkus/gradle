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

import org.gradle.api.UncheckedIOException;
import org.gradle.api.internal.ClassPathRegistry;
import org.gradle.messaging.MessagingServer;
import org.gradle.messaging.ObjectConnection;
import org.gradle.util.exec.ExecHandle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

public class DefaultWorkerProcessFactory implements WorkerProcessFactory {
    private final MessagingServer server;
    private final ClassPathRegistry classPathRegistry;

    public DefaultWorkerProcessFactory(MessagingServer server, ClassPathRegistry classPathRegistry) {
        this.server = server;
        this.classPathRegistry = classPathRegistry;
    }

    public WorkerProcessBuilder newProcess() {
        return new DefaultWorkerProcessBuilder();
    }

    private class DefaultWorkerProcessBuilder extends WorkerProcessBuilder {
        public DefaultWorkerProcessBuilder() {
            getJavaCommand().mainClass(GradleWorkerMain.class.getName());
            getJavaCommand().classpath(classPathRegistry.getClassPathFiles("WORKER_PROCESS"));
        }

        @Override
        public WorkerProcess build() {
            if (getWorker() == null) {
                throw new IllegalStateException("No worker action specified for this worker process.");
            }
            
            ObjectConnection connection = server.createUnicastConnection();

            List<URL> implementationClassPath = Arrays.asList(
                    ((URLClassLoader) getWorker().getClass().getClassLoader()).getURLs());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);
                objectStream.writeObject(getApplicationClasspath());
                objectStream.writeObject(getSharedPackages());
                objectStream.writeObject(implementationClassPath);
                objectStream.writeObject(getWorker());
                objectStream.writeObject(connection.getLocalAddress());
                objectStream.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            getJavaCommand().getCommand().standardInput(new ByteArrayInputStream(outputStream.toByteArray()));
            ExecHandle execHandle = getJavaCommand().build();

            return new DefaultWorkerProcess(connection, execHandle);
        }
    }
}