package org.jboss.mavenPlugin.buildInfo;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "list-dependencies-aggregator", requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.TEST_COMPILE, aggregator = true, instantiationStrategy = InstantiationStrategy.SINGLETON)
public class ListDependenciesAggregatorMojo extends AbstractParentMojo {

	/**
	 * Location of the output file.
	 */
	@Parameter(defaultValue = "${project.build.directory}/dependencies_all.txt", property = "outputFile", required = true)
	private File outputFile;

	public void execute() throws MojoExecutionException {
		List<Dependency> dependencies = collector.createListOfDependencies(getExcludedModules());
		createOuputFile(dependencies);
	}

	private void createOuputFile(List<Dependency> dependencies) throws MojoExecutionException {
		Result<List<Dependency>> result = new Result<>(project, dependencies);
		new DependenciesToLogWriter(getLog()).write(result);
		if (outputFile != null) {
			assertOutputFileFolderExists(outputFile);
			new DependenciesToFileWriter(outputFile).write(result);
		}
	}

}
