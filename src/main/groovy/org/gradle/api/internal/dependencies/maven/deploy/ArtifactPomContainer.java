/*
 * Copyright 2007-2008 the original author or authors.
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
package org.gradle.api.internal.dependencies.maven.deploy;

import org.apache.ivy.core.module.descriptor.Artifact;

import java.io.File;
import java.util.Map;

/**
 * @author Hans Dockter
 */
public interface ArtifactPomContainer {
    void addArtifact(Artifact artifact, File src);

    ArtifactPom getDefaultArtifactPom();

    void setDefaultArtifactPom(ArtifactPom artifactPom);

    void addArtifactPom(ArtifactPom artifactPom);

    ArtifactPom getArtifactPom(String name);


    Map<File, File> createDeployableUnits();
}