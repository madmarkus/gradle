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
import org.gradle.util.ClassLoaderObjectInputStream;
import org.gradle.util.ClasspathUtil;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.concurrent.Callable;

public class SystemApplicationClassLoaderWorker implements Callable<Void> {
    private final byte[] serializedWorker;
    private final Collection<URL> applicationClassPath;

    public SystemApplicationClassLoaderWorker(Collection<URL> applicationClassPath, byte[] serializedWorker) {
        this.applicationClassPath = applicationClassPath;
        this.serializedWorker = serializedWorker;
    }

    public Void call() throws Exception {
        final ClassLoader applicationClassLoader = ClassLoader.getSystemClassLoader();
        ClasspathUtil.addUrl((URLClassLoader) applicationClassLoader, applicationClassPath);

        ClassLoaderObjectInputStream instr = new ClassLoaderObjectInputStream(new ByteArrayInputStream(
                serializedWorker), getClass().getClassLoader());
        Action<WorkerContext> action = (Action<WorkerContext>) instr.readObject();
        action.execute(new WorkerContext() {
            public ClassLoader getApplicationClassLoader() {
                return applicationClassLoader;
            }
        });
        return null;
    }
}
