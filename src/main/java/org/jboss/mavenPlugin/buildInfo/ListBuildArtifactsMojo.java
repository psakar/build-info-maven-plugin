package org.jboss.mavenPlugin.buildInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "list-build-artifacts", defaultPhase = LifecyclePhase.PACKAGE) //, instantiationStrategy = InstantiationStrategy.SINGLETON
public class ListBuildArtifactsMojo extends AbstractParentMojo {

	/**
	 * Location of the output file.
	 */
	@Parameter(defaultValue = "${project.build.directory}/build_artifacts.txt", property = "outputFile", required = false)
	private File outputFile;

	public void execute() throws MojoExecutionException {
		List<Artifact> buildArtifacts = createListOfBuildArtifacts();
		collector.addModuleBuildArtifacts(createProjectId(project), buildArtifacts);
		createOuputFile(buildArtifacts);
	}

	private List<Artifact> createListOfBuildArtifacts() {
		List<Artifact> artifacts = new ArrayList<>();
		artifacts.add(project.getArtifact());
		artifacts.addAll(project.getAttachedArtifacts());
		return artifacts;
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
