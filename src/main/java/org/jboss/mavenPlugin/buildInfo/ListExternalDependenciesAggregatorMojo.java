package org.jboss.mavenPlugin.buildInfo;

import static java.util.stream.Collectors.toList;
import static org.codehaus.plexus.util.StringUtils.defaultString;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
//import org.apache.maven.project.MavenProject;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

@Mojo(name = "list-external-dependencies-aggregator", requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.PACKAGE, aggregator = true, instantiationStrategy = InstantiationStrategy.SINGLETON)
public class ListExternalDependenciesAggregatorMojo extends AbstractParentMojo {

	/**
	 * Location of the output file.
	 */
	@Parameter(defaultValue = "${project.build.directory}/dependencies_external_all.txt", property = "outputFile", required = true)
	private File outputFile;

//	@Parameter(defaultValue = "${reactorProjects}", required = true, readonly = true)
//	private List<MavenProject> reactorProjects;
	
//	private File outputFileAggregator;
//	private MavenProject projectAggregator;
//	private int count = 0;
	
	public void execute() throws MojoExecutionException {
		/*
		if (outputFileAggregator == null) {
			outputFileAggregator = outputFile;
		}
		if (projectAggregator == null) {
			projectAggregator = project;
		}
		count ++;
		// check if last project - see http://stackoverflow.com/questions/132976/how-do-you-force-a-maven-mojo-to-be-executed-only-once-at-the-end-of-a-build
		// FIXME test monitor approach
		MavenProject lastProject = reactorProjects.get(reactorProjects.size() - 1);
//		System.err.println("ZZ count " + count + " reactorProjects.size() " + reactorProjects.size() + " project " + project.getArtifactId() + " last " +  lastProject.getArtifactId());
		if (!project.equals(lastProject)) {
//			System.err.println("ZZ Ignore modules");
			return;
		}
		outputFile = outputFileAggregator;
		project = projectAggregator;
		*/
		List<Dependency> dependencies = collector.createListOfDependencies(getExcludedModules());
		List<Artifact> buildArtifacts = collector.createListOfBuildArtifacts(getExcludedModules());
		ImmutableList<Dependency> uniqueDependencies = ImmutableList.copyOf(Iterables.filter(dependencies, new DependencyDuplicateRemover()));
		List<Dependency> externalDependencies = removeBuildArtifactsFromDependencies(uniqueDependencies, buildArtifacts);
		List<Dependency> sortedExternalDependencies = sortDependencies(externalDependencies);
		createOuputFile(sortedExternalDependencies);
	}

	private List<Dependency> sortDependencies(List<Dependency> dependencies) {
		return new Ordering<Dependency>() {
			@Override
			public int compare(Dependency left, Dependency right) {
				int groupIdComparison = left.getGroupId().compareTo(right.getGroupId());
				if (groupIdComparison != 0)
					return groupIdComparison;
				int artifactIdComparison = left.getArtifactId().compareTo(right.getArtifactId());
				return artifactIdComparison;
			}
			
		}.immutableSortedCopy(dependencies);
	}

	private List<Dependency> removeBuildArtifactsFromDependencies(List<Dependency> dependencies,
			List<Artifact> buildArtifacts) {
		final Set<String> buildArtifactKeys = new HashSet<>();
		final Set<String> buildArtifactKeysWithoutVersion = new HashSet<>();
		System.err.println("Build artifacts");
		for (Artifact buildArtifact : buildArtifacts) {
			System.err.println("Build artifact " + buildArtifact.getGroupId() + ":" + buildArtifact.getArtifactId() + ":" + buildArtifact.getVersion() + ":" + defaultString(buildArtifact.getClassifier()) + ":" + defaultString(buildArtifact.getType()));
			buildArtifactKeys.add(createDependencyKey(buildArtifact));
			buildArtifactKeysWithoutVersion.add(createDependencyKeyWithoutVersion(buildArtifact));
		}
		java.util.function.Predicate<? super Dependency> predicate = new java.util.function.Predicate<Dependency>() {
			@Override
			public boolean test(Dependency dependency) {
				if ("".equals(defaultString(dependency.getVersion()))) {
					return !buildArtifactKeysWithoutVersion.contains(createDependencyKeyWithoutVersion(dependency));
				}
				return !buildArtifactKeys.contains(createDependencyKey(dependency));
			}
		};
		List<Dependency> resolvedDependencies = dependencies.stream().filter(predicate).collect(toList());
		return resolvedDependencies;
	}

	private void createOuputFile(List<Dependency> dependencies) throws MojoExecutionException {
		Log log = getLog();
		Result<List<Dependency>> result = new Result<>(project, dependencies);
		new DependenciesToLogWriter(log).write(result);
		if (outputFile != null) {
			assertOutputFileFolderExists(outputFile);
			log.info("Write list of external dependencies to file " + outputFile.getAbsolutePath());
			new DependenciesToFileWriter(outputFile).write(result);
		}
	}

	static String createDependencyKey(Artifact input) {
		return input.getGroupId() + ":" + input.getArtifactId() + ":" + defaultString(input.getVersion()) + ":" + defaultString(input.getClassifier());
	}

	static String createDependencyKeyWithoutVersion(Artifact input) {
		return input.getGroupId() + ":" + input.getArtifactId() + "::" + defaultString(input.getClassifier());
	}
	
	static String createDependencyKey(Dependency input) {
		return input.getGroupId() + ":" + input.getArtifactId() + ":" + defaultString(input.getVersion()) + ":" + defaultString(input.getClassifier());
	}

	static String createDependencyKeyWithoutVersion(Dependency input) {
		return input.getGroupId() + ":" + input.getArtifactId() + "::" + defaultString(input.getClassifier());
	}
	static abstract class DuplicateRemover<T> implements Predicate<T> {

	    private final Set<String> set = new HashSet<>();

	    @Override
	    public boolean apply(T input) {

	        String key = getKey(input);
			boolean exists = set.contains(key);

	        if (!exists) {
	            set.add(key);
	        }

	        return !exists;
	    }
	    
	    protected abstract String getKey(T input);
	}
	
	static class DependencyDuplicateRemover extends DuplicateRemover<Dependency> {

		@Override
		protected String getKey(Dependency input) {
			return createDependencyKey(input);
		}
		
	}
}
