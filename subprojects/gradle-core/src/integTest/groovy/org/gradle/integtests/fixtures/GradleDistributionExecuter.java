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
package org.gradle.integtests.fixtures;

import org.gradle.StartParameter;
import org.gradle.api.logging.LogLevel;
import org.gradle.integtests.ForkingGradleExecuter;
import org.gradle.util.TestFile;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A Junit rule which provides a {@link GradleExecuter} implementation that executes Gradle using a given {@link
 * GradleDistribution}. If not supplied in the constructor, this rule locates a field on the test object with type
 * {@link GradleDistribution}.
 *
 * By default, this executer will execute Gradle in a forked process. There is a system property which enables
 * executing Gradle in the current process.
 */
public class GradleDistributionExecuter extends AbstractGradleExecuter implements MethodRule {
    private static final String NOFORK_SYS_PROP = "org.gradle.integtest.nofork";
    private static final boolean FORK;
    private GradleDistribution dist;
    private StartParameterModifier inProcessStartParameterModifier;
    private Map<String, String> environmentVars = new HashMap<String, String>();
    private String script;

    static {
        FORK = System.getProperty(NOFORK_SYS_PROP, "false").equalsIgnoreCase("false");
    }

    public GradleDistributionExecuter(GradleDistribution dist) {
        this.dist = dist;
    }

    public GradleDistributionExecuter() {
    }

    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        if (dist == null) {
            dist = RuleHelper.findField(target, GradleDistribution.class);
        }
        if (dist == null) {
            throw new RuntimeException(String.format("Could not find a GradleDistribution field on %s.", target));
        }
        inDirectory(dist.getTestDir());
        return base;
    }

    @Override
    public GradleExecuter usingExecutable(String script) {
        this.script = script;
        return this;
    }

    public ExecutionResult run() {
        return configureExecuter().run();
    }

    public ExecutionFailure runWithFailure() {
        return configureExecuter().runWithFailure();
    }

    @Override
    public GradleExecuter withEnvironmentVars(Map<String, ?> environment) {
        environmentVars.clear();
        for (Map.Entry<String, ?> entry : environment.entrySet()) {
            environmentVars.put(entry.getKey(), entry.getValue().toString());
        }
        return this;
    }

    public void setInProcessStartParameterModifier(StartParameterModifier inProcessStartParameterModifier) {
        this.inProcessStartParameterModifier = inProcessStartParameterModifier;
    }

    private GradleExecuter configureExecuter() {
        if (!getClass().desiredAssertionStatus()) {
            throw new RuntimeException("Assertions should be enabled when running integration tests.");
        }
        StartParameter parameter = new StartParameter();
        parameter.setLogLevel(LogLevel.INFO);
        parameter.setGradleHomeDir(dist.getGradleHomeDir());
        parameter.setSearchUpwards(false);
        if (!isDisableTestGradleUserHome()) {
            parameter.setGradleUserHomeDir(dist.getUserHomeDir());
        }

        InProcessGradleExecuter inProcessGradleExecuter = new InProcessGradleExecuter(parameter);
        copyTo(inProcessGradleExecuter);

        GradleExecuter returnedExecuter = inProcessGradleExecuter;

        if (FORK || inProcessGradleExecuter.getParameter().isShowVersion() || !environmentVars.isEmpty()
                || script != null) {
            ForkingGradleExecuter forkingGradleExecuter = new ForkingGradleExecuter(dist);
            copyTo(forkingGradleExecuter);
            forkingGradleExecuter.setDisableTestGradleUserHome(isDisableTestGradleUserHome());
            forkingGradleExecuter.withEnvironmentVars(environmentVars);
            forkingGradleExecuter.usingExecutable(script);
            returnedExecuter = forkingGradleExecuter;
        } else {
            if (inProcessStartParameterModifier != null) {
                inProcessStartParameterModifier.modify(inProcessGradleExecuter.getParameter());
            }
        }

        boolean settingsFound = false;
        for (
                File dir = new TestFile(getWorkingDir()); dir != null && dist.isFileUnderTest(dir) && !settingsFound;
                dir = dir.getParentFile()) {
            if (new File(dir, "settings.gradle").isFile()) {
                settingsFound = true;
            }
        }
        if (settingsFound) {
            returnedExecuter.withSearchUpwards();
        }

        return returnedExecuter;
    }

    public static interface StartParameterModifier {
        void modify(StartParameter startParameter);
    }
}