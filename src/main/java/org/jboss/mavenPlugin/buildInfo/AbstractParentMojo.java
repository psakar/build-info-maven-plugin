package org.jboss.mavenPlugin.buildInfo;

import static org.codehaus.plexus.util.StringUtils.defaultString;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
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
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractParentMojo extends AbstractMojo {
	static final String FIELD_SEPARATOR = "\t";
	static final String SEPARATOR = ":";

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	protected MavenProject project;

	@Component(role = ModuleResultsCollector.class)
	protected ModuleResultsCollector collector;

	/**
	 * Location of the output file.
	 */
	@Parameter(property = "excludeModules", required = false)
	private String excludeModules;
	
	static String createProjectId(MavenProject project) {
		return project.getGroupId() + SEPARATOR + project.getArtifactId() + SEPARATOR + project.getVersion();
	}

	static String convertArtifactToId(Artifact artifact) {
		return defaultString(artifact.getGroupId())
				+ SEPARATOR + defaultString(artifact.getArtifactId())
				+ SEPARATOR + defaultString(artifact.getVersion())
				+ SEPARATOR + defaultString(artifact.getClassifier())
				+ SEPARATOR + defaultString(artifact.getType())
				;
	}

	static String createDependencyId(Dependency dependency) {
		return defaultString(dependency.getGroupId())
				+ SEPARATOR + defaultString(dependency.getArtifactId())
				+ SEPARATOR + defaultString(dependency.getVersion())
				+ SEPARATOR + defaultString(dependency.getClassifier())
				+ SEPARATOR + defaultString(dependency.getType())
				;
	}
	
	protected void assertOutputFileFolderExists(File outputFile) {
		File outputDirectory = outputFile.getParentFile();

		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
	}
	
	protected List<String> getExcludedModules() {
		debug("ExcludeModules " + excludeModules);
		List<String> list = new ArrayList<>();
		if (defaultString(excludeModules).trim().length() > 0) {
			String items[] = excludeModules.split(",");
			for (String item : items) {
				if (defaultString(item).trim().length() > 0) {
					list.add(item.trim());
				}
			}
		}
		debug("ExcludeModules list size " + list.size());
		return list;
	}

	protected void debug(String string) {
		System.out.println(string);
		System.err.println(string);
	}

	static class Result <T> {
		public final MavenProject project;
		public final T info;
		public Result(MavenProject project, T result) {
			this.project = project;
			this.info = result;
		}
		
	}
	
	static interface ResultWriter <Y, T extends Result<Y>> {
		public void write(T t) throws MojoExecutionException;
	}

	
	static class ArtifactsToFileWriter implements ResultWriter<List<Artifact>, Result<List<Artifact>>> {

		private final File outputFile;
			
		public ArtifactsToFileWriter(File outputFile) {
			this.outputFile = outputFile;
		}

		public void write(Result<List<Artifact>> result) throws MojoExecutionException {
			try (FileWriter w = new FileWriter(outputFile)) {
				w.write("Version" + FIELD_SEPARATOR + "1.0" + "\n");
				w.write("Project" + FIELD_SEPARATOR + createProjectId(result.project) + "\n");
				w.write("Build artifacts\n");
				w.write("GAV(GroupId:ArtifactId:Version:Classifier:Type)" + "\n");
				for (Artifact artifact : result.info) {
					w.write(convertArtifactToId(artifact) + "\n");
				}
			} catch (IOException e) {
				throw new MojoExecutionException("Error writing list of dependencies into file " + outputFile.getAbsolutePath(), e);
			}
		}
	}

	static class ArtifactsToLogWriter implements ResultWriter<List<Artifact>, Result<List<Artifact>>> {
		private final Log log;

		public ArtifactsToLogWriter(Log log) {
			this.log = log;
		}

		public void write(Result<List<Artifact>> result) throws MojoExecutionException {
			for (Artifact artifact : result.info) {
				log.info("Build artifact: " + convertArtifactToId(artifact));
			}
		}
	}
	
	
	static class DependenciesToFileWriter implements ResultWriter<List<Dependency>, Result<List<Dependency>>> {

		private final File outputFile;
			
		public DependenciesToFileWriter(File outputFile) {
			this.outputFile = outputFile;
		}

		public void write(Result<List<Dependency>> result) throws MojoExecutionException {
			try (FileWriter w = new FileWriter(outputFile)) {
				w.write("Version" + FIELD_SEPARATOR + "1.0" + "\n");
				w.write("Project" + FIELD_SEPARATOR + createProjectId(result.project) + "\n");
				w.write("Dependencies\n");
				w.write("GAV(GroupId:ArtifactId:Version:Classifier:Type)" + FIELD_SEPARATOR + "Scope" + "\n");
	
				for (Dependency dependency : result.info) {
					w.write(createDependencyId(dependency) + FIELD_SEPARATOR + defaultString(dependency.getScope()) + "\n");
				}
	
			} catch (IOException e) {
				throw new MojoExecutionException("Error writing list of build artifacts into file " + outputFile.getAbsolutePath(), e);
			}
		}
	}

	static class DependenciesToLogWriter implements ResultWriter<List<Dependency>, Result<List<Dependency>>> {
		private final Log log;

		public DependenciesToLogWriter(Log log) {
			this.log = log;
		}

		public void write(Result<List<Dependency>> result) throws MojoExecutionException {
			for (Dependency dependency : result.info) {
				log.info("Dependency: " + createDependencyId(dependency) + " " + defaultString(dependency.getScope()));
			}
		}
	}	

}
