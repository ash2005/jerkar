package org.jerkar.distrib.all;

import java.io.File;

import org.jerkar.CoreBuild;
import org.jerkar.JkBuildDependencySupport;
import org.jerkar.JkDoc;
import org.jerkar.JkFileTree;
import org.jerkar.JkFileTreeSet;
import org.jerkar.JkLog;
import org.jerkar.JkProject;
import org.jerkar.JkZipper;
import org.jerkar.builtins.javabuild.JkJavadocMaker;
import org.jerkar.plugins.jacoco.PluginsJacocoBuild;
import org.jerkar.plugins.sonar.PluginsSonarBuild;


public class DistribAllBuild extends JkBuildDependencySupport {
	
	@JkProject("../org.jerkar.plugins-sonar")
	PluginsSonarBuild pluginsSonar;
	
	@JkProject("../org.jerkar.plugins-jacoco")
	PluginsJacocoBuild pluginsJacoco;
	
	@JkDoc("Construct a distrib assuming all dependent sub projects are already built.")
	public void distrib() {
		
		JkLog.startln("Creating distribution file");
		
		JkLog.info("Copy core distribution localy.");
		CoreBuild core = pluginsJacoco.core;  // The core project is got by transitivity
		File distDir = this.ouputDir("dist");
		JkFileTree dist = JkFileTree.of(distDir).importDirContent(core.distribFolder);
				
		JkLog.info("Add plugins to the distribution");
		JkFileTree ext = dist.sub("libs/ext").importFiles(pluginsSonar.packer().jarFile(), pluginsJacoco.packer().jarFile());
		JkFileTree sourceDir = dist.sub("libs-sources");
		sourceDir.importFiles(pluginsSonar.packer().jarSourceFile(), pluginsJacoco.packer().jarSourceFile());
		
		JkLog.info("Add plugins to the fat jar");
		File fat = dist.file(core.packer().fatJarFile().getName());
		JkZipper.of().merge(ext.include("**/*.jar")).appendTo(fat);
		
		JkLog.info("Create a fat source jar");
		File fatSource = sourceDir.file("org.jerkar.core-fat-sources.jar");
		JkZipper.of().merge(sourceDir.include("**.jar", "**.zip")
			.exclude(fatSource.getName())).to(fatSource);
		
		JkLog.info("Create a fat javadoc");
		JkFileTreeSet sources = this.pluginsJacoco.core.sourceDirs().and(this.pluginsJacoco.sourceDirs())
				.and(this.pluginsSonar.sourceDirs());
		File javadocAllDir = this.ouputDir("javadoc-all");
		File javadocAllFile = dist.file("libs-javadoc/org.jerkar.core-fat-javadoc.jar");
		JkJavadocMaker.of(sources, javadocAllDir, javadocAllFile).process();
		
		JkLog.info("Pack all");
		dist.zip().to(ouputDir("jerkar-distrib.zip"));
		
		JkLog.done();
	}
	
	@JkDoc("End to end method to construct a distrib.")
	public void doDefault() {
		super.doDefault();
		multiProjectDependencies().invokeDoDefaultMethodOnAllSubProjects();
		distrib();
	} 
	
	
}