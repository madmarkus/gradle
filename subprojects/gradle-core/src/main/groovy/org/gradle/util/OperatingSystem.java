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

public class OperatingSystem {
    private static final OperatingSystem WINDOWS = new OperatingSystem() {
        @Override
        public boolean isCaseSensitiveFileSystem() {
            return false;
        }

        @Override
        public boolean isWindows() {
            return true;
        }

        @Override
        public String getScriptName(String scriptPath) {
            if (scriptPath.toLowerCase().endsWith(".bat")) {
                return scriptPath;
            }
            return scriptPath + ".bat";
        }
    };

    private static final OperatingSystem OS_X = new OperatingSystem() {
        @Override
        public boolean isCaseSensitiveFileSystem() {
            return false;
        }
    };

    private static final OperatingSystem OTHER = new OperatingSystem();
    private static final OperatingSystem CURRENT;

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            CURRENT = WINDOWS;
        } else if (osName.contains("mac os x")) {
            CURRENT = OS_X;
        } else {
            CURRENT = OTHER;
        }
    }

    public static OperatingSystem current() {
        return CURRENT;
    }

    public boolean isWindows() {
        return false;
    }

    public boolean isUnix() {
        // Not quite true
        return !isWindows();
    }

    public boolean isCaseSensitiveFileSystem() {
        return true;
    }

    public String getScriptName(String scriptPath) {
        return scriptPath;
    }
}
