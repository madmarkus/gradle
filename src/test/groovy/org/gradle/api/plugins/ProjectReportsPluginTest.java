/*
 * Copyright 2008 the original author or authors.
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
package org.gradle.api.plugins;

import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.integration.junit4.JMock;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.gradle.util.HelperUtil;
import org.gradle.api.Project;
import org.gradle.api.tasks.diagnostics.TaskReportTask;
import org.gradle.api.tasks.diagnostics.PropertyReportTask;
import org.gradle.api.tasks.diagnostics.DependencyReportTask;

public class ProjectReportsPluginTest {
    @Test
    public void addsTasksToProject() {
        Project project = HelperUtil.createRootProject();
        new ProjectReportsPlugin().apply(project, null, null);
        assertThat(project.findTask("taskReport"), instanceOf(TaskReportTask.class));
        assertThat(project.findTask("propertyReport"), instanceOf(PropertyReportTask.class));
        assertThat(project.findTask("dependencyReport"), instanceOf(DependencyReportTask.class));
    }
}