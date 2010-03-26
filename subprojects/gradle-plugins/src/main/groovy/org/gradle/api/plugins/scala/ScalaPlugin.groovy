/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.plugins.scala;


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.scala.ScalaDoc

public class ScalaPlugin implements Plugin<Project> {
    // tasks
    public static final String SCALA_DOC_TASK_NAME = "scaladoc";

    public void apply(Project project) {
        project.plugins.apply(ScalaBasePlugin.class);
        project.plugins.apply(JavaPlugin.class);

        configureScaladoc(project);
    }

    private void configureScaladoc(final Project project) {
        project.getTasks().withType(ScalaDoc.class).allTasks {ScalaDoc scalaDoc ->
            scalaDoc.conventionMapping.classpath = { project.sourceSets.main.classes + project.sourceSets.main.compileClasspath }
            scalaDoc.conventionMapping.defaultSource = { project.sourceSets.main.scala }
        }
        project.tasks.add(SCALA_DOC_TASK_NAME, ScalaDoc.class).description = "Generates scaladoc for the source code.";
    }
}
