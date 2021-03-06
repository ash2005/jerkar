## Jerkar Runtime
----

This section details what happens behind the cover when Jerkar is run.

### Launching Java Process
 
Jerkar is a pure Java application requiring __JDK 6 or above__. __JDK__ is required and __JRE__ is not sufficient.
Indeed Jerkar uses the JDK tools to compile java source files located under _[PROJECT DIR]/build/def_.

To ease launching Java process in command line, Jerkar provides native scripts ( _jerkar.bat_ for __Windows__ and _jerkar_ for __Unix__ ).
These scripts do the following :

1. __Find the java executable path__ : If a `JAVA_HOME` environment variable is defined then it takes its value as `java` path. Otherwise it takes the `java` executable defined in the _PATH_ of your OS.
2. __Get java execution option__ : If an environment variable `JERKAR_OPTS` exists then its value is passed to the `java` command line parameters, otherwise default `-Xmx512m -XX:MaxPermSize=512m` is passed.
3. __Set Jerkar classpath__ in the following order :
	* all jar and zip files found under _[WORKING DIR]/build/boot_
	* all jar and zip files found under _[JERKAR HOME]/libs/ext_
	* the _[JERKAR_HOME]/org.jerkar.core.jar_ file 
4. __Run the `org.jerkar.tool.Main` class__ passing the command line argument as is. So if you have typed `jerkar myArg1 myArg2` the `myArg1 myArg2` will be passed as Java command-line arguments.

#### Embedded Mode
Note that ___[JERKAR_HOME]/org.jerkar.core.jar___ comes after ___[WORKING_DIR]/build/libs/build/*___ in the classpath.
This means that if a version of Jerkar (org.jerkar.core.jar) is in this directory, the build will be processed with this instance of Jerkar and not with the one located in in _[JERKAR HOME]_.

This is called the __Embedded__ mode. It guarantees that your project will build regardless of Jerkar version installed on the host machine. 
This mode allows to build your project even if Jerkar is not installed on the host machine. just execute `java -cp build/libs/build/* org.jerkar.tool.Main` instead of `jerkar`.

### Jerkar Execution

The `org.jerkar.tool.Main#main` is the entry point of Jerkar. This is the method you invoke to launch or debug a Jerkar build within your IDE.

It processes as follow :

1. Parse the command line.
2. Populate system properties and Jerkar options from configuration files and command line (see <strong>build configuration</strong>).
3. Pre-process and compile java source files located under under _[PROJECT DIR]/build/def_ (see <strong>Build Definition Compilation</strong>). 
4. Instantiate the build class (see <strong>Build Class Instantiation</strong>)
5. Inject options in build instance fields (see <strong>Build Configuration</strong>).
6. Instantiate and configure plugins specified in command line arguments (see <strong>Mention Plugins in the Command Line</strong>).
7. Invoke methods specified in command line arguments : methods are executed in the order they appear on the command line.

#### Command Line

Jerkar parse the command line and process each arguments according its pattern :

* Argument starts with `@` : This is an module import clause, the following will be used for adding a module to build class compile & run classpath. For example if the command line contains `@com.google.guava:guava:18.0`, the build class will be compiled with Guava in its classpath and Guava will be also present in the classpath when the build class will be executed. 

* Argument starts with `-` : This is an option declaration. The following is expected to be formated as _optionName=optionValue_. For example, `-repo.build.url=http://my.repo.milestone/' will inject 'http://my.repo.milestone/' in the 'repo.build.url' Jerkar option.

* in the other cases, argument is considered as a method name to invoke on the build class instance.


#### Build class Compilation
Jerkar compiles build class files prior to execute it. Build class files are expected to be in _[PROJECT DIR]/build/def_. If this directory does not exist or does not contains java sources, the compilation is skipped.
Compilation outputs class files in _[PROJECT DIR]/build/output/def-bin_ directory and uses classpath containing :

* Java libraries located in _[PROJECT DIR]/build/libs/build_.
* Java libraries located in _[JERKAR HOME]/libs/ext_ (not in embedded mode).

You can augment the classpath with :

* Java libraries hosted on a Maven or Ivy repositories
* Java libraries located on file system.
* Build definition (java sources) of other projects

Information about extra lib to add to classpath are located in the build classes, inside `@JkImport` and `@JkProject` annotation.
This information is read by parsing java **source** files, prior they are compiled.

##### Libraries Located on Maven/Ivy Repository 
To add libraries from Maven/Ivy repository you need to annotate the build definition with `@JkImport`. This annotation takes an array of String as its default parameter so you can specify several dependencies.
The mentioned dependencies are resolved transitively. 

``` 
@JkImport(`{"commons-httpclient:commons-httpclient:3.1", "com.google.guava:guava:18.0"})
public class HttpClientTaskBuild extends JkJavaBuild {`
...
```

Url of the maven/ivy repositories is given by `repo.build.url` Jerkar option. If this option is not set, then it takes the url given by `repo.download.url` option. If the last is nor present as well, it falls back in Maven Central.
If this repository needs credentials, you need to supply it through Jerkar options `repo.build.username` and `repo.build.password`.

Note that you can define several `repo.build.url` by separating then with coma (as `repo.build.url=http://my.repo1, http://my.repo2.snapshot`).
 
As for other repo, If the download repository is an Ivy repo, you have to prefix url with `ivy:` so for example you'll get `repo.build.url=ivy:file://my.ivy/repo`.

##### Libraries on File System
To add library from file system you need to annotate the build definition with `@JkImport`. This annotation takes an array of String as argument so you can specify several dependencies.
The mentioned dependencies are not resolved transitively. 
The expected value is a Ant include pattern applied to the project root directory.


``` 
@JkImport({"commons-httpclient:commons-httpclient:3.1", "build/libs/compile/*.jar"})
public class HttpClientTaskBuild extends JkJavaBuild {`
...
```

This will include _commons-httpclient_ and its dependencies in the classpath along all jar file located in _[PROJECT DIR]/build/libs/compile_.

##### Build Definitions of other project
Your build definitions can depends on build definitions of other projects. It is typically the case for multi-project builds. 
This capability allows to share build elements in a static typed way as the build definitions files can consume classes coming from build definitions of other projects.

`@JkProject` is an annotation that applies on fields instance of `org.jerkar.tool.JkBuild` or its subclasses. This annotation contains the relative path of the consumed project.
If the project build definition sources contain some `@JkProject` annotations, build class of the consumed project are pre-processed and compiled recursively. 
Classes and classpath of the consumed project are added to the build definition classpath of the consumer project.

```
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
		...
```

#### Build Class Instantiation
Once build class compiled. Jerkar instantiate the build it.
Build class is specified by the `buildClass` option if present. If not, it is the first class implementing `org.jerkar.tool.JkBuild`. 
If no class implementing `org.jerkar.tool.JkBuild` is found then the `org.jerkar.tool.builtins.javabuild.JkJavaBuild` is instantiated.

The class scanning processes classes in alphabetic order then sub-package in deep first. This mean that class `MyBuid` will be scanned prior `apackage.ABuild`, and `aa.bb.MyClass` will be scanned prior `ab.OtherClass`.

The `buildClass` option can mention a simple name class (class name omitting its package). If no class matches the  specified `buildClass` then an exception is thrown.

The `org.jerkar.tool.JkBuild` constructor instantiate fields annotated with `@JkProject`. If a project build appears many time in the annotated project tree, a single instance is created then shared.

### Setting paths

#### Specify Jerkar user home

Jerkar uses user directory to store user-specific configuration and cache files, in this document we refer to this directory using [Jerkar User Home].
By default the this directory is located at _[User Home]/.jerkar_ (_[User Home]_ being the path given by `System.getProperty("user.home");`.
You can override this setting by defining the `JERKAR_USER_HOME` environment variable.
You can programatically get this location in your build definition using `JkLocator.jerkarUserHome()`. 

#### Specify the local repository cache

Jerkar uses [Apache Ivy](http://ant.apache.org/ivy/) under the hood to handle module dependencies. Ivy downloads and stores locally artifacts consumed by projects.
By default the location is _[JERKAR USER HOME]/repo-cache_ but you can redefine it by defining the `JERKAR_REPO` environment variable.
You can programatically get this location in your build definition using `JkLocator.jerkarRepositoryCache()`.

#### See the effective paths

The Jerkar logs displays the effective path at the very start of the process :

```
 _______           _                 
(_______)         | |                
     _ _____  ____| |  _ _____  ____ 
 _  | | ___ |/ ___) |_/ |____ |/ ___)
| |_| | ____| |   |  _ (/ ___ | |    
 \___/|_____)_|   |_| \_)_____|_|
                                     The 100% Java build tool.

Java Home : C:\UserTemp\I19451\software\jdk1.6.0_24\jre
Java Version : 1.6.0_24, Sun Microsystems Inc.
Jerkar Home : C:\software\jerkar
Jerkar User Home : C:\users\djeang\.jerkar
Jerkar Repository Cache : C:\users\djeang\.jerkar\cache\repo
...
```
 
  
