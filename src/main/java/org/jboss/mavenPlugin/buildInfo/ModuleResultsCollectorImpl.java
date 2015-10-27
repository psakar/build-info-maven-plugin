package org.jboss.mavenPlugin.buildInfo;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = ModuleResultsCollector.class)
public class ModuleResultsCollectorImpl implements ModuleResultsCollector {

	private final Map<String, List<Artifact>> moduleBuildArtifacts = new HashMap<>();
	private final Map<String, List<Dependency>> moduleDependencies = new HashMap<>();
	
	public List<Artifact> createListOfBuildArtifacts(List<String> excludedModules) {
		Map<String, List<Artifact>> filteredModuleBuildArtifacts = new HashMap<>();
		for (Entry<String, List<Artifact>> entry : moduleBuildArtifacts.entrySet()) {
			if (!isExcludedModule(excludedModules, entry.getKey())) {
				filteredModuleBuildArtifacts.put(entry.getKey(), entry.getValue());
			} else {
				debug("exclude build artifacts of module " + entry.getKey());
			}
		}
		List<Artifact> result = filteredModuleBuildArtifacts.values().stream()
		        .flatMap(list -> list.stream())
		        .collect(toList());
//		System.err.println("Build artifacts count " + result.size());
		return result;
	}

	public void addModuleBuildArtifacts(String moduleId, List<Artifact> buildArtifacts) {
		moduleBuildArtifacts.put(moduleId, buildArtifacts);
	}

	@Override
	public List<Dependency> createListOfDependencies(List<String> excludedModules) {
		Map<String, List<Dependency>> filteredModuleDependencies = new HashMap<>();
		for (Entry<String, List<Dependency>> entry : moduleDependencies.entrySet()) {
			if (!isExcludedModule(excludedModules, entry.getKey())) {
				filteredModuleDependencies.put(entry.getKey(), entry.getValue());
			} else {
				debug("exclude dependencies of module " + entry.getKey());
			}
		}
		List<Dependency> result = filteredModuleDependencies
				.values().stream()
		        .flatMap(list -> list.stream())
		        .collect(toList());
//		System.err.println("Build artifacts count " + result.size());
		return result;
	}

	private boolean isExcludedModule(List<String> excludedModules, String key) {
		for (String excludedModule : excludedModules) {
//			debug("key " + key + " module " + excludedModule + ":");
			if (key.startsWith(excludedModule + ":"))
				return true;
		}
		return false;
	}

	@Override
	public void addModuleDependencies(String moduleId, List<Dependency> dependencies) {
		moduleDependencies.put(moduleId, dependencies);		
	}
	
	private void debug(String string) {
		System.out.println("ZZ " + string);
		System.err.println("ZZ " + string);
	}
	
}
