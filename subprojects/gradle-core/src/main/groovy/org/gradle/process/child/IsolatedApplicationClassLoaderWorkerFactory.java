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

import org.gradle.api.internal.ClassPathRegistry;
import org.gradle.process.WorkerProcessBuilder;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;

public class IsolatedApplicationClassLoaderWorkerFactory implements WorkerFactory {
    private final Object workerId;
    private final String displayName;
    private final WorkerProcessBuilder processBuilder;
    private final Collection<URL> implementationClassPath;
    private final URI serverAddress;
    private final ClassPathRegistry classPathRegistry;

    public IsolatedApplicationClassLoaderWorkerFactory(Object workerId, String displayName, WorkerProcessBuilder processBuilder,
                                            Collection<URL> implementationClassPath, URI serverAddress,
                                            ClassPathRegistry classPathRegistry) {
        this.workerId = workerId;
        this.displayName = displayName;
        this.processBuilder = processBuilder;
        this.implementationClassPath = implementationClassPath;
        this.serverAddress = serverAddress;
        this.classPathRegistry = classPathRegistry;
    }

    public Collection<File> getSystemClasspath() {
        return classPathRegistry.getClassPathFiles("WORKER_PROCESS");
    }

    public Callable<?> create() {
        Set<URL> applicationClassPath = processBuilder.getApplicationClasspath();
        ActionExecutionWorker injectedWorker = new ActionExecutionWorker(processBuilder.getWorker(), workerId, displayName, serverAddress);
        ImplementationClassLoaderWorker worker = new ImplementationClassLoaderWorker(processBuilder.getLogLevel(), processBuilder.getSharedPackages(),
                implementationClassPath, injectedWorker);
        return new IsolatedApplicationClassLoaderWorker(applicationClassPath, worker);
    }
}
