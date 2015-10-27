package org.jboss.list_build_artifacts_maven_plugin;

import org.apache.maven.artifact.InvalidRepositoryException;

public class ClassA {
	
	public void test() throws Exception {
		throw new InvalidRepositoryException("test", "test");
	}

}
