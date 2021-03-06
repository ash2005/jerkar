package org.jerkar.samples;

import static org.jerkar.api.depmanagement.JkPopularModules.GUAVA;
import static org.jerkar.api.depmanagement.JkPopularModules.JUNIT;

import org.jerkar.api.depmanagement.JkDependencies;
import org.jerkar.api.depmanagement.JkMavenPublication;
import org.jerkar.api.depmanagement.JkMavenPublicationInfo;
import org.jerkar.api.depmanagement.JkVersion;
import org.jerkar.tool.builtins.javabuild.JkJavaBuild;

/**
 * This build demonstrate how to specified project metadata required to publish on 
 * Maven central ( see https://maven.apache.org/guides/mini/guide-central-repository-upload.html )
 * 
 * @author Jerome Angibaud
 */
public class OpenSourceJarBuild extends JkJavaBuild {
	
	@Override 
	public JkVersion version() {
		return JkVersion.ofName("1.3.1-SNAPSHOT");   
	}
	
	@Override
	protected JkDependencies dependencies() {
		return JkDependencies.builder()
			.on(GUAVA, "18.0") 
			.on(JUNIT, "4.11").scope(TEST)
		.build();
	}
	
	@Override  
	protected JkMavenPublication mavenPublication() {
		return super.mavenPublication().with(
			JkMavenPublicationInfo   // Information required to publish on Maven central
				.of("my project", "my description", "https://github.com/jerkar/samples")
				.withScm("https://github.com/jerkar/sample.git")
				.andApache2License()
				.andGitHubDeveloper("djeang", "dgeangdev@yahoo.fr")
			);
	}
	
}
