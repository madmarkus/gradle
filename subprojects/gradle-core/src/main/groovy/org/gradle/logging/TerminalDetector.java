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
package org.gradle.logging;

import org.gradle.api.specs.Spec;
import org.jruby.ext.posix.POSIX;
import org.jruby.ext.posix.POSIXFactory;
import org.jruby.ext.posix.POSIXHandler;

import java.io.File;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.PrintStream;

public class TerminalDetector implements Spec<FileDescriptor> {
    public boolean isSatisfiedBy(FileDescriptor element) {
        return POSIXFactory.getPOSIX(new POSIXHandlerImpl(), true).isatty(element);
    }
    
    private static class POSIXHandlerImpl implements POSIXHandler {
        public void error(POSIX.ERRORS errors, String message) {
            throw new UnsupportedOperationException();
        }

        public void unimplementedError(String message) {
            throw new UnsupportedOperationException();
        }

        public void warn(WARNING_ID warningId, String message, Object... objects) {
        }

        public boolean isVerbose() {
            return false;
        }

        public File getCurrentWorkingDirectory() {
            throw new UnsupportedOperationException();
        }

        public String[] getEnv() {
            throw new UnsupportedOperationException();
        }

        public InputStream getInputStream() {
            return System.in;
        }

        public PrintStream getOutputStream() {
            return System.out;
        }

        public int getPID() {
            throw new UnsupportedOperationException();
        }

        public PrintStream getErrorStream() {
            return System.err;
        }
    }
}