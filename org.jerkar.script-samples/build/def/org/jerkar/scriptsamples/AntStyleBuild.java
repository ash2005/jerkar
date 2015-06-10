package org.jerkar.scriptsamples;

import java.io.File;

import org.jerkar.JkBuild;
import org.jerkar.JkClasspath;
import org.jerkar.JkJavaCompiler;
import org.jerkar.JkJavaProcess;
import org.jerkar.builtins.javabuild.JkManifest;
import org.jerkar.builtins.javabuild.testing.junit.JkUnit;
import org.jerkar.builtins.javabuild.testing.junit.JkUnit.JunitReportDetail;
import org.jerkar.file.JkFileTree;

/**
 * Equivalent to http://ant.apache.org/manual/tutorial-HelloWorldWithAnt.html
 * 
 * @author Jerome Angibaud
 */
public class AntStyleBuild extends JkBuild {
	
	File src = baseDir("src/main/java");
	File buildDir = baseDir("build/output");
	File classDir = new File(buildDir, "classes");
	File jarFile = new File(buildDir, "jar/" + this.baseDir().root().getName() + ".jar");
	String className = "my.mainClass";
	JkClasspath classpath = JkClasspath.of(baseDir().include("libs/**/*.jar"));
	File reportDir = new File(buildDir, "junitRreport");
	
	@Override
	public void doDefault() {
		clean();run();
		
	}
	
	public void compile() {
		JkJavaCompiler.ofOutput(classDir).withClasspath(classpath).andSourceDir(src).compile();
		JkFileTree.of(src).exclude("**/*.java").copyTo(classDir);
	}
	
	public void jar() {
		compile();
		JkManifest.empty().addMainClass("org.jerkar.scriptsamples.RunClass").writeToStandardLocation(classDir);
		JkFileTree.of(classDir).zip().to(jarFile);
	}	
	
	public void run() {
		jar();
		JkJavaProcess.of().withWorkingDir(jarFile.getParentFile())
			.andClasspath(classpath).runJarSync(jarFile);
	}
	
	public void cleanBuild() {
		clean();jar();
	}
	
	public void junit() {
		jar();
		JkUnit.ofFork(classpath.and(jarFile))
				.withClassesToTest(JkFileTree.of(classDir).include("**/*Test.class"))
				.withReportDir(reportDir)
				.withReport(JunitReportDetail.FULL)
				.run();
	}
	
	public static void main(String[] args) {
		new AntStyleBuild().doDefault();
	}
	
}
