package org.jboss.mavenPlugin.buildInfo;

import static org.codehaus.plexus.util.StringUtils.defaultString;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;

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
import org.apache.maven.project.MavenProject;

@Mojo(name = "list-build-artifacts", defaultPhase = LifecyclePhase.VERIFY)
public class ListBuildArtifactsMojo extends AbstractMojo {
	private static final String SEPARATOR = ":";
	/**
	 * Location of the output file.
	 */
	@Parameter(defaultValue = "${project.build.directory}/build_artifacts.txt", property = "outputFile", required = false)
	private File outputFile;

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	public void execute() throws MojoExecutionException {
		createOuputFile(createListOfBuildArtifacts());
	}

	private List<Artifact> createListOfBuildArtifacts() {
		List<Artifact> artifacts = new ArrayList<>();
		artifacts.add(project.getArtifact());
		artifacts.addAll(project.getAttachedArtifacts());
		return artifacts;
	}


	private void createOuputFile(List<Artifact> artifacts) throws MojoExecutionException {
		logBuildArtifacts(artifacts);
		if (outputFile != null) {
			assertOutputFileFolderExists();
			writeArtifactsToFile(artifacts);
		}
	}
	
	private void writeArtifactsToFile(List<Artifact> artifacts) throws MojoExecutionException {
		try (FileWriter w = new FileWriter(outputFile)) {
			w.write("Version 1.0" + "\n");
			w.write("Columns: GroupId;ArtifactId;Version;Classifier;Type" + "\n");

			for (Artifact artifact : artifacts) {
				w.write(convertArtifactToString(artifact) + "\n");
			}

		} catch (IOException e) {
			throw new MojoExecutionException("Error writing list of dependencies into file " + outputFile.getAbsolutePath(), e);
		}
	}

	static String convertArtifactToString(Artifact artifact) {
		return defaultString(artifact.getGroupId())
				+ SEPARATOR + defaultString(artifact.getArtifactId())
				+ SEPARATOR + defaultString(artifact.getVersion())
				+ SEPARATOR + defaultString(artifact.getClassifier())
				+ SEPARATOR + defaultString(artifact.getType())
				;
	}
	
	private void logBuildArtifacts(List<Artifact> artifacts) {
		for (Artifact artifact : artifacts) {
			getLog().info("Build artifact: " + artifact);
		}
	}

	private void assertOutputFileFolderExists() {
		File outputDirectory = outputFile.getParentFile();

		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
	}
}
