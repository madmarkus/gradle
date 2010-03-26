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
package org.gradle.api.internal.artifacts.publish.maven.deploy;

import org.apache.commons.io.FileUtils;
import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.cache.DefaultRepositoryCacheManager;
import org.apache.ivy.core.cache.RepositoryCacheManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.DownloadReport;
import org.apache.ivy.core.resolve.DownloadOptions;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.core.search.ModuleEntry;
import org.apache.ivy.core.search.OrganisationEntry;
import org.apache.ivy.core.search.RevisionEntry;
import org.apache.ivy.plugins.namespace.Namespace;
import org.apache.ivy.plugins.resolver.ResolverSettings;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.maven.artifact.ant.AttachedArtifact;
import org.apache.maven.artifact.ant.InstallDeployTaskSupport;
import org.apache.maven.artifact.ant.Pom;
import org.apache.maven.settings.Settings;
import org.apache.tools.ant.Project;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.artifacts.maven.MavenPom;
import org.gradle.api.artifacts.maven.MavenResolver;
import org.gradle.api.artifacts.maven.PomFilterContainer;
import org.gradle.api.artifacts.maven.PublishFilter;
import org.gradle.api.logging.DefaultStandardOutputCapture;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.StandardOutputCapture;
import org.gradle.util.AntUtil;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

/**
 * @author Hans Dockter
 */
public abstract class AbstractMavenResolver implements MavenResolver {
    protected final static String SETTINGS_XML = "<settings/>"; 

    private String name;
    
    private ArtifactPomContainer artifactPomContainer;

    private PomFilterContainer pomFilterContainer;

    private Settings settings;

    public AbstractMavenResolver(String name, PomFilterContainer pomFilterContainer, ArtifactPomContainer artifactPomContainer) {
        this.name = name;
        this.pomFilterContainer = pomFilterContainer;
        this.artifactPomContainer = artifactPomContainer;
    }

    protected abstract InstallDeployTaskSupport createPreConfiguredTask(Project project);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public ResolvedModuleRevision getDependency(DependencyDescriptor dd, ResolveData data) throws ParseException {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public ResolvedResource findIvyFileRef(DependencyDescriptor dd, ResolveData data) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public DownloadReport download(Artifact[] artifacts, DownloadOptions options) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public ArtifactDownloadReport download(ArtifactOrigin artifact, DownloadOptions options) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public boolean exists(Artifact artifact) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public ArtifactOrigin locate(Artifact artifact) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public void reportFailure() {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public void reportFailure(Artifact art) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public String[] listTokenValues(String token, Map otherTokenValues) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public Map[] listTokenValues(String[] tokens, Map criteria) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public OrganisationEntry[] listOrganisations() {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public ModuleEntry[] listModules(OrganisationEntry org) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public RevisionEntry[] listRevisions(ModuleEntry module) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public Namespace getNamespace() {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public void dumpSettings() {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }


    public void publish(Artifact artifact, File src, boolean overwrite) throws IOException {
        if (isIgnorable(artifact)) {
            return;
        }
        getArtifactPomContainer().addArtifact(artifact, src);
    }

    private boolean isIgnorable(Artifact artifact) {
        return artifact.getType().equals("ivy");
    }

    public void beginPublishTransaction(ModuleRevisionId module, boolean overwrite) throws IOException {
        // do nothing
    }

    public void abortPublishTransaction() throws IOException {
        // do nothing
    }

    public void commitPublishTransaction() throws IOException {
        InstallDeployTaskSupport installDeployTaskSupport = createPreConfiguredTask(AntUtil.createProject());
        Set<DeployableFilesInfo> deployableFilesInfos = getArtifactPomContainer().createDeployableFilesInfos();
        File emptySettingsXml = createEmptyMavenSettingsXml();
        installDeployTaskSupport.setSettingsFile(emptySettingsXml);
        for (DeployableFilesInfo deployableFilesInfo : deployableFilesInfos) {
            addPomAndArtifact(installDeployTaskSupport, deployableFilesInfo);
            execute(installDeployTaskSupport);
        }
        emptySettingsXml.delete();
        settings = ((CustomInstallDeployTaskSupport) installDeployTaskSupport).getSettings();
    }

    private void execute(InstallDeployTaskSupport deployTask) {
        StandardOutputCapture outputCapture = new DefaultStandardOutputCapture(true, LogLevel.INFO).start();
        try {
            deployTask.execute();
        } finally {
            outputCapture.stop();
        }
    }

    private void addPomAndArtifact(InstallDeployTaskSupport installOrDeployTask, DeployableFilesInfo deployableFilesInfo) {
        Pom pom = new Pom();
        pom.setProject(installOrDeployTask.getProject());
        pom.setFile(deployableFilesInfo.getPomFile());
        installOrDeployTask.addPom(pom);
        installOrDeployTask.setFile(deployableFilesInfo.getArtifactFile());
        for (ClassifierArtifact classifierArtifact : deployableFilesInfo.getClassifierArtifacts()) {
            AttachedArtifact attachedArtifact = installOrDeployTask.createAttach();
            attachedArtifact.setClassifier(classifierArtifact.getClassifier());
            attachedArtifact.setFile(classifierArtifact.getFile());
            attachedArtifact.setType(classifierArtifact.getType());
        }
    }

    private File createEmptyMavenSettingsXml() {
        try {
            File settingsXml = File.createTempFile("gradle_empty_settings", ".xml");
            FileUtils.writeStringToFile(settingsXml, SETTINGS_XML);
            return settingsXml;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void setSettings(ResolverSettings settings) {
        // do nothing
    }

    public RepositoryCacheManager getRepositoryCacheManager() {
        return new DefaultRepositoryCacheManager();
    }

    public ArtifactPomContainer getArtifactPomContainer() {
        return artifactPomContainer;
    }

    public void setArtifactPomContainer(ArtifactPomContainer artifactPomContainer) {
        this.artifactPomContainer = artifactPomContainer;
    }

    public Settings getSettings() {
        return settings;
    }

    public PublishFilter getFilter() {
        return pomFilterContainer.getFilter();
    }

    public void setFilter(PublishFilter defaultFilter) {
        pomFilterContainer.setFilter(defaultFilter);
    }

    public MavenPom getPom() {
        return pomFilterContainer.getPom();
    }

    public void setPom(MavenPom defaultPom) {
        pomFilterContainer.setPom(defaultPom);
    }

    public MavenPom addFilter(String name, PublishFilter publishFilter) {
        return pomFilterContainer.addFilter(name, publishFilter);
    }

    public PublishFilter filter(String name) {
        return pomFilterContainer.filter(name);
    }

    public MavenPom pom(String name) {
        return pomFilterContainer.pom(name);
    }

    public Iterable<PomFilter> getActivePomFilters() {
        return pomFilterContainer.getActivePomFilters();
    }

    public PomFilterContainer getPomFilterContainer() {
        return pomFilterContainer;
    }

    public void setPomFilterContainer(PomFilterContainer pomFilterContainer) {
        this.pomFilterContainer = pomFilterContainer;
    }
}
