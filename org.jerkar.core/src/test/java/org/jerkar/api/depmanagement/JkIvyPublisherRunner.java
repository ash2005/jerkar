package org.jerkar.api.depmanagement;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import org.jerkar.api.system.JkLog;
import org.jerkar.api.utils.JkUtilsFile;


public class JkIvyPublisherRunner {

	public static void main(String[] args) {
		JkLog.verbose(true);
		//JkUtilsTool.loadUserSystemProperties();
		testPublishIvy();
		testPublishMaven();
	}

	public static void testPublishIvy() {
		final IvyPublisher jkIvyResolver = IvyPublisher.of(ivyRepos(), new File("build/output/test-out"));
		final JkVersionedModule versionedModule = JkVersionedModule.of(JkModuleId.of("mygroup", "mymodule"), JkVersion.ofName("myVersion"));
		final JkIvyPublication ivyPublication = JkIvyPublication.of(sampleJarfile(), JkScopedDependencyTest.COMPILE, JkScopedDependencyTest.TEST);
		final JkDependencies deps = JkDependencies.builder()
				.on("org.springframework", "spring-jdbc", "3.0.+").scope(JkScopedDependencyTest.COMPILE).build();
		jkIvyResolver.publishIvy(versionedModule, ivyPublication,deps, null, null, new Date());
	}

	public static void testPublishMaven() {
		final IvyPublisher jkIvyResolver = IvyPublisher.of(mavenRepos(), new File("build/output/test-out"));
		final JkVersionedModule versionedModule = JkVersionedModule.of(JkModuleId.of("mygroup2", "mymodule2"), JkVersion.ofName("0.0.1"));
		final JkMavenPublication publication = JkMavenPublication.of("mymodule2", sampleJarfile()).and(sampleJarSourcefile(), "source");
		final JkDependencies deps = JkDependencies.builder()
				.on("org.springframeworko", "spring-jdbc", "2.0.+").scope(JkScopedDependencyTest.COMPILE).build();
		jkIvyResolver.publishMaven(versionedModule, publication, deps);
	}


	private static File sampleJarfile() {
		final URL url = JkIvyPublisherRunner.class.getResource("myArtifactSample.jar");
		try {
			return new File(url.toURI().getPath());
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private static File sampleJarSourcefile() {
		final URL url = JkIvyPublisherRunner.class.getResource("myArtifactSample-source.jar");
		try {
			return new File(url.toURI().getPath());
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private static JkPublishRepos ivyRepos() {
		final File baseDir = new File(JkUtilsFile.workingDir(), "build/output/testIvyRepo");
		baseDir.mkdir();
		return JkPublishRepos.ivy(baseDir);
	}

	private static JkPublishRepos mavenRepos() {
		final File baseDir = new File(JkUtilsFile.workingDir(), "build/output/mavenRepo");
		baseDir.mkdir();
		return JkPublishRepos.maven(baseDir);
	}


}