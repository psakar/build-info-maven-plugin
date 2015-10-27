package org.jboss.mavenPlugin.buildInfo;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "list-build-artifacts-aggregator", defaultPhase = LifecyclePhase.PACKAGE, instantiationStrategy = InstantiationStrategy.SINGLETON, aggregator = true)
public class ListBuildArtifactsAggregatorMojo extends AbstractParentMojo {

	/**
	 * Location of the output file.
	 */
	@Parameter(defaultValue = "${project.build.directory}/build_artifacts_all.txt", property = "outputFile", required = false)
	private File outputFile;

	public void execute() throws MojoExecutionException {
		createOuputFile(collector.createListOfBuildArtifacts(getExcludedModules()));
	}

	private void createOuputFile(List<Artifact> artifacts) throws MojoExecutionException {
		Result<List<Artifact>> result = new Result<>(project, artifacts);
		new ArtifactsToLogWriter(getLog()).write(result);
		if (outputFile != null) {
			assertOutputFileFolderExists(outputFile);
			new ArtifactsToFileWriter(outputFile).write(result);
		}
	}
	
}
