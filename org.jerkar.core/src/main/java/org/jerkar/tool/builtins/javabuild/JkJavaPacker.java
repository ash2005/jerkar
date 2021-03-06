package org.jerkar.tool.builtins.javabuild;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jerkar.api.crypto.pgp.JkPgp;
import org.jerkar.api.file.JkFileTree;
import org.jerkar.api.file.JkFileTreeSet;
import org.jerkar.api.file.JkZipper;
import org.jerkar.api.java.JkManifest;
import org.jerkar.api.system.JkLog;
import org.jerkar.api.utils.JkUtilsFile;
import org.jerkar.api.utils.JkUtilsIterable;
import org.jerkar.api.utils.JkUtilsObject;
import org.jerkar.api.utils.JkUtilsString;

/**
 * Jar maker for the {@link JkJavaBuild} template. This maker will get
 * information from supplied java builder to create relevant jars.
 *
 * @author Jerome Angibaud
 */
public class JkJavaPacker implements Cloneable {

    /**
     * Creates a {@link JkJavaPacker} for the specified build.
     */
    public static JkJavaPacker.Builder builder(JkJavaBuild build) {
        return JkJavaPacker.of(build).builder();
    }

    /**
     * Creates a {@link JkJavaPacker} for the specified java build.
     */
    public static JkJavaPacker of(JkJavaBuild build) {
        return new JkJavaPacker(build);
    }

    private final JkJavaBuild build;

    private boolean includeVersion = false;

    private boolean fullName = true;

    private final Set<String> checkSums;

    private boolean doJar = true;

    private boolean doTest = false;

    private boolean doSources = true;

    private boolean doFatJar = false;

    private boolean doJavadoc = false;

    private final String fatJarSuffix;

    private JkPgp pgp = null;

    private List<JkExtraPacking> extraActions = new LinkedList<JkExtraPacking>();

    private JkJavaPacker(JkJavaBuild build) {
        this.build = build;
        this.doFatJar = build.pack.fatJar;
        this.fatJarSuffix = JkUtilsObject.firstNonNull(build.pack.fatJarSuffix, "fat");
        this.doTest = build.pack.tests;
        if (build.pack.checksums == null) {
            this.checkSums = new HashSet<String>();
        } else {
            this.checkSums = JkUtilsIterable.setOf(JkUtilsString.split(
                    build.pack.checksums.toUpperCase(), ","));
        }
        if (build.pack.signWithPgp) {
            this.pgp = build.pgp();
        }
        this.doJavadoc = build.pack.javadoc;
    }

    /**
     * Returns the base name for all the generated archives. It generally the full name of the project but it
     * can include also the vesrion.
     */
    public String baseName() {
        final String name = fullName ? build.moduleId().fullName() : build.moduleId().name();
        if (includeVersion) {
            return name + "-" + build.effectiveVersion();
        }
        return name;
    }

    /**
     * Creates a builder for {@link JkJavaPacker}.
     */
    public Builder builder() {
        return new JkJavaPacker.Builder(this);
    }

    /**
     * The jar file that will be generated for the main artifact.
     */
    public File jarFile() {
        return build.ouputDir(baseName() + ".jar");
    }

    /**
     * The jar file that will be generated the jar for the specified classifier.
     */
    public File jarFile(String classifier) {
        return build.ouputDir(baseName() + "-" + classifier + ".jar");
    }

    /**
     * The jar containing the source files.
     */
    public File jarSourceFile() {
        return build.ouputDir(baseName() + "-sources.jar");
    }

    /**
     * The jar containing the test classes.
     */
    public File jarTestFile() {
        return build.ouputDir(baseName() + "-test.jar");
    }

    /**
     * The jar containing the test source files.
     */
    public File jarTestSourceFile() {
        return build.ouputDir(baseName() + "-test-sources.jar");
    }

    /**
     * The jar standing for the fat jar (aka uber jar)
     */
    public File fatJarFile() {
        return build.ouputDir(baseName() + "-" + fatJarSuffix + ".jar");
    }

    /**
     * The jar containing the javadoc
     */
    public File javadocFile() {
        return build.ouputDir(baseName() + "-javadoc.jar");
    }

    /**
     * Produces all the artifact files.
     */
    public void pack() {
        JkLog.startln("Packaging module");
        if (doJar && !JkUtilsFile.isEmpty(build.classDir(), false)) {
            final JkManifest manifest = build.jarManifest();
            if (!manifest.isEmpty()) {
                manifest.writeToStandardLocation(build.classDir());
            }
            JkFileTree.of(build.classDir()).zip().to(jarFile()).md5If(checkSums.contains("MD5"))
            .sha1If(checkSums.contains("SHA-1"));
        }
        final JkFileTreeSet sourceAndResources = build.sources().and(build.resources());
        if (doSources && sourceAndResources.countFiles(false) > 0) {
            build.sources().and(build.resources()).zip().to(jarSourceFile());
        }
        if (doTest && !build.tests.skip && build.testClassDir().exists()
                && !JkFileTree.of(build.testClassDir()).files(false).isEmpty()) {
            JkZipper.of(build.testClassDir()).to(jarTestFile());
        }
        if (doTest && doSources && !build.unitTestSources().files(false).isEmpty()) {
            build.unitTestSources().and(build.unitTestResources()).zip().to(jarTestSourceFile());
        }
        if (doFatJar) {
            JkFileTree.of(build.classDir()).zip().merge(build.depsFor(JkJavaBuild.RUNTIME))
            .to(fatJarFile()).md5If(checkSums.contains("MD5"))
            .sha1If(checkSums.contains("SHA-1"));
        }
        for (final JkExtraPacking action : this.extraActions) {
            action.process(build);
        }
        if (doJavadoc) {
            this.build.javadoc();
        }
        if (pgp != null) {
            JkLog.start("Sign artifacts");
            pgp.sign(jarFile(), jarSourceFile(), jarTestFile(), jarTestSourceFile(), fatJarFile(),
                    javadocFile());
            JkLog.done();
        }
        JkLog.done();
    }

    /**
     * JkExtraPacking action that will be processed by the {@link JkJavaBuild#pack} method.
     */
    public interface JkExtraPacking {

        /**
         * Method invoked by the {@link JkJavaBuild#pack} method.
         */
        public void process(JkJavaBuild build);

    }

    /**
     * A builder for {@link JkJavaBuild}
     */
    public static class Builder {

        private final JkJavaPacker packer;

        private Builder(JkJavaPacker packer) {
            this.packer = packer.clone();
        }

        /**
         * True to include the version in the file names.
         */
        public Builder includeVersion(boolean includeVersion) {
            packer.includeVersion = includeVersion;
            return this;
        }

        /**
         * True means that the name of the archives will include the groupId of
         * the artifact.
         */
        public Builder fullName(boolean fullName) {
            packer.fullName = fullName;
            return this;
        }

        /**
         * Generate MD-5 check sum for archives.
         */
        public Builder md5checksum(boolean checkSum) {
            if (checkSum) {
                packer.checkSums.add("MD5");
            } else {
                packer.checkSums.remove("MD5");
            }
            return this;
        }

        /**
         * Generate SHA1 check sum for archives.
         */
        public Builder sha1checksum(boolean checkSum) {
            if (checkSum) {
                packer.checkSums.add("SHA-1");
            } else {
                packer.checkSums.remove("SHA-1");
            }
            return this;
        }

        /**
         * Set <code>true</code> to generate a jar file containing both classes and resources.
         */
        public Builder doJar(boolean doJar) {
            packer.doJar = doJar;
            return this;
        }

        /**
         * Set <code>true</code> to generate a jar file containing both test classes and test sources.
         */
        public Builder doTest(Boolean doTest) {
            packer.doTest = doTest;
            return this;
        }

        /**
         * Set <code>true</code> to generate a jar file containing sources.
         */
        public Builder doSources(Boolean doSources) {
            packer.doSources = doSources;
            return this;
        }

        /**
         * Set <code>true</code> to generate a fat jar.
         */
        public Builder doFatJar(Boolean doFatJar) {
            packer.doFatJar = doFatJar;
            return this;
        }

        /**
         * Tells the packer to sign each produced element.
         */
        public Builder doSign(Boolean doSign, JkPgp pgp) {
            if (!doSign) {
                packer.pgp = null;
                return this;
            }
            packer.pgp = pgp;
            return this;
        }

        /**
         * Tells the packer to sign each produced element.
         */
        public Builder doSign(Boolean doSign, File secretRingKey, String secretKeyPassword) {
            return doSign(doSign, JkPgp.ofSecretRing(secretRingKey, secretKeyPassword));
        }

        /**
         * Add an extra action to the packer to be build.
         */
        public Builder extraAction(JkExtraPacking jkExtraPacking) {
            packer.extraActions.add(jkExtraPacking);
            return this;
        }

        /**
         * Builds the packer.
         */
        public JkJavaPacker build() {
            return packer.clone();
        }

    }

    @Override
    public JkJavaPacker clone() {
        JkJavaPacker clone;
        try {
            clone = (JkJavaPacker) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        clone.extraActions = new LinkedList<JkJavaPacker.JkExtraPacking>(this.extraActions);
        return clone;
    }

}
