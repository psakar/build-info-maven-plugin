package org.jboss.mavenPlugin.buildInfo;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "list-dependencies", defaultPhase = LifecyclePhase.TEST_COMPILE, requiresDependencyResolution = ResolutionScope.TEST)
public class ListDependenciesMojo extends AbstractParentMojo {

	/**
	 * Location of the output file.
	 */
	@Parameter(defaultValue = "${project.build.directory}/dependencies.txt", property = "outputFile", required = true)
	private File outputFile;

	public void execute() throws MojoExecutionException {
		List<Dependency> dependencies = createListOfDependencies();
		collector.addModuleDependencies(createProjectId(project), dependencies);
		createOuputFile(dependencies);
	}

	private List<Dependency> createListOfDependencies() {
		return project.getDependencies();
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
