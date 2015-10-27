package org.jboss.mavenPlugin.buildInfo;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;

public interface ModuleResultsCollector {
	
	List<Artifact> createListOfBuildArtifacts(List<String> excludedModules);
	void addModuleBuildArtifacts(String moduleId, List<Artifact> buildArtifacts);
	List<Dependency> createListOfDependencies(List<String> excludedModules);
	void addModuleDependencies(String moduleId, List<Dependency> dependencies);
}