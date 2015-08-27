package org.jboss.mavenPlugin.buildInfo;

import static org.codehaus.plexus.util.StringUtils.defaultString;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.maven.model.Dependency;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "list-dependencies", defaultPhase = LifecyclePhase.TEST_COMPILE, requiresDependencyResolution = ResolutionScope.TEST)
public class ListDependenciesMojo extends AbstractMojo {
	private static final String SEPARATOR = ":";

	/**
	 * Location of the output file.
	 */
	@Parameter(defaultValue = "${project.build.directory}/dependencies.txt", property = "outputFile", required = true)
	private File outputFile;

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	public void execute() throws MojoExecutionException {

		List<Dependency> dependencies = createListOfDependencies();
		createOuputFile(dependencies);
	}

	private List<Dependency> createListOfDependencies() {
		return project.getDependencies();
	}

	private void createOuputFile(List<Dependency> dependencies) throws MojoExecutionException {
		logDependencies(dependencies);
		if (outputFile != null) {
			assertOutputFileFolderExists();
			writeDependenciesToFile(dependencies);
		}
	}
	
	private void writeDependenciesToFile(List<Dependency> dependencies) throws MojoExecutionException {
		try (FileWriter w = new FileWriter(outputFile)) {
			w.write("Version 1.0" + "\n");
			w.write("Columns: GroupId:ArtifactId:Version:Classifier:Type;Scope" + "\n");

			for (Dependency dependency : dependencies) {
				w.write(convertDependencyToString(dependency) + "\n");
			}

		} catch (IOException e) {
			throw new MojoExecutionException("Error writing list of build artifacts into file " + outputFile.getAbsolutePath(), e);
		}
	}

	static String convertDependencyToString(Dependency dependency) {
		return defaultString(dependency.getGroupId())
				+ SEPARATOR + defaultString(dependency.getArtifactId())
				+ SEPARATOR + defaultString(dependency.getVersion())
				+ SEPARATOR + defaultString(dependency.getClassifier())
				+ SEPARATOR + defaultString(dependency.getType())
				+ ";" + defaultString(dependency.getScope())
				;
	}

	private void logDependencies(List<Dependency> dependencies) {
		for (Dependency dependency : dependencies) {
			getLog().info("Dependency: " + dependency);
		}
	}

	private void assertOutputFileFolderExists() {
		File outputDirectory = outputFile.getParentFile();

		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
	}
}
