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
package org.gradle.initialization;

import org.gradle.*;
import org.gradle.api.internal.ClassGenerator;
import org.gradle.api.internal.ExceptionAnalyser;
import org.gradle.api.internal.project.GlobalServicesRegistry;
import org.gradle.api.internal.project.ProjectFactory;
import org.gradle.api.internal.project.ServiceRegistry;
import org.gradle.api.internal.project.TopLevelBuildServiceRegistry;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logging;
import org.gradle.api.logging.StandardOutputListener;
import org.gradle.cache.CacheRepository;
import org.gradle.configuration.BuildConfigurer;
import org.gradle.configuration.ProjectDependencies2TaskResolver;
import org.gradle.invocation.DefaultGradle;
import org.gradle.listener.ListenerManager;
import org.gradle.util.TimeProvider;
import org.gradle.util.WrapUtil;

/**
 * @author Hans Dockter
 */
public class DefaultGradleLauncherFactory implements GradleLauncherFactory {
    private final ServiceRegistry sharedServices;
    private final NestedBuildTracker tracker = new NestedBuildTracker();
    private LoggingConfigurer loggingConfigurer;
    private CommandLine2StartParameterConverter commandLine2StartParameterConverter;

    public DefaultGradleLauncherFactory() {
        loggingConfigurer = new DefaultLoggingConfigurer();
        loggingConfigurer.configure(LogLevel.LIFECYCLE);
        sharedServices = new GlobalServicesRegistry();
        commandLine2StartParameterConverter = sharedServices.get(CommandLine2StartParameterConverter.class);
    }

    public StartParameter createStartParameter(String[] commandLineArgs) {
        return commandLine2StartParameterConverter.convert(commandLineArgs);
    }

    public GradleLauncher newInstance(String[] commandLineArgs) {
        return newInstance(commandLine2StartParameterConverter.convert(commandLineArgs));
    }

    public GradleLauncher newInstance(StartParameter startParameter) {
        loggingConfigurer.configure(startParameter.getLogLevel());

        TopLevelBuildServiceRegistry serviceRegistry = new TopLevelBuildServiceRegistry(sharedServices, startParameter);
        ListenerManager listenerManager = serviceRegistry.get(ListenerManager.class);

        //this hooks up the ListenerManager and LoggingConfigurer so you can call Gradle.addListener() with a StandardOutputListener.
        loggingConfigurer.addStandardOutputListener(listenerManager.getBroadcaster(StandardOutputListener.class));
        loggingConfigurer.addStandardErrorListener(listenerManager.getBroadcaster(StandardOutputListener.class));

        listenerManager.useLogger(new TaskExecutionLogger(Logging.getLogger(TaskExecutionLogger.class)));
        listenerManager.addListener(tracker);
        listenerManager.addListener(new BuildCleanupListener(serviceRegistry));

        DefaultGradle gradle = new DefaultGradle(
                tracker.getCurrentBuild(),
                startParameter, serviceRegistry);
        return new DefaultGradleLauncher(
                gradle,
                serviceRegistry.get(InitScriptHandler.class),
                new SettingsHandler(
                        new EmbeddedScriptSettingsFinder(
                                new DefaultSettingsFinder(WrapUtil.<ISettingsFileSearchStrategy>toList(
                                        new MasterDirSettingsFinderStrategy(),
                                        new ParentDirSettingsFinderStrategy()))),
                        serviceRegistry.get(SettingsProcessor.class),
                        new BuildSourceBuilder(
                                this,
                                new DefaultCacheInvalidationStrategy(),
                                serviceRegistry.get(ClassLoaderFactory.class),
                                serviceRegistry.get(CacheRepository.class),
                                serviceRegistry.get(TimeProvider.class)
                        )),
                new DefaultGradlePropertiesLoader(),
                new BuildLoader(
                        new ProjectFactory(
                                startParameter.getBuildScriptSource(),
                                serviceRegistry.get(ClassGenerator.class))),
                new BuildConfigurer(new ProjectDependencies2TaskResolver()),
                loggingConfigurer,
                gradle.getBuildListenerBroadcaster(),
                serviceRegistry.get(ExceptionAnalyser.class));
    }

    public void setCommandLine2StartParameterConverter(
            CommandLine2StartParameterConverter commandLine2StartParameterConverter) {
        this.commandLine2StartParameterConverter = commandLine2StartParameterConverter;
    }

    private static class BuildCleanupListener extends BuildAdapter {
        private final TopLevelBuildServiceRegistry services;

        private BuildCleanupListener(TopLevelBuildServiceRegistry services) {
            this.services = services;
        }

        @Override
        public void buildFinished(BuildResult result) {
            services.close();
        }
    }
}
