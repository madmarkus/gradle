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
package org.gradle.integtests

import org.junit.runner.RunWith
import org.junit.Test
import org.gradle.util.OperatingSystem

@RunWith(DistributionIntegrationTestRunner.class)
public class CommandLineIntegrationTest {

    // Injected by test runner
    private GradleDistribution dist;
    private GradleExecuter executer;

    @Test
    public void hasNonZeroExitCodeOnBuildFailure() {
        File javaprojectDir = new File(dist.samplesDir, 'java/quickstart')
        ExecutionFailure failure = executer.inDirectory(javaprojectDir).withTasks('unknown').runWithFailure()
        failure.assertHasDescription("Task 'unknown' not found in root project 'quickstart'.")
    }

    @Test
    public void canonicalisesWorkingDirectory() {
        File javaprojectDir;
        if (OperatingSystem.current().isWindows()) {
            javaprojectDir = new File(dist.samplesDir, 'java/QUICKS~1')
        } else if (!OperatingSystem.current().isCaseSensitiveFileSystem()) {
            javaprojectDir = new File(dist.samplesDir, 'JAVA/QuickStart')
        } else {
            javaprojectDir = new File(dist.samplesDir, 'java/multiproject/../quickstart')
        }
        executer.inDirectory(javaprojectDir).withTasks('classes').run()
    }

    @Test
    public void canUseVersionCommandLineOption() {
        executer.withArguments('-v').run()
    }
}