package org.jerkar.tool;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.jerkar.api.tooling.JkCodeWriterForBuildClass;
import org.jerkar.api.utils.JkUtilsFile;

/**
 * Object that process scaffolding.
 */
public final class JkScaffolder {

    private final JkBuild build;

    private Object mainBuildclassWriter;

    private final List<Runnable> extraActions;

    JkScaffolder(JkBuild build) {
        super();
        this.build = build;
        this.mainBuildclassWriter = basicScaffoldedBuildClassCode();
        this.extraActions = new LinkedList<Runnable>();
    }

    /**
     * Runs the scaffolding.
     */
    public void run() {
        final File def = build.file(JkConstants.BUILD_DEF_DIR);
        def.mkdirs();
        final File buildClass = new File(def, "Build.java");
        JkUtilsFile.writeString(buildClass, mainBuildclassWriter.toString(), false);
        for (final Runnable runnable : extraActions) {
            runnable.run();
        }
    }

    private static JkCodeWriterForBuildClass basicScaffoldedBuildClassCode() {
        final JkCodeWriterForBuildClass codeWriter = new JkCodeWriterForBuildClass();
        codeWriter.extendedClass = "JkBuild";
        return codeWriter;
    }

    /**
     * Sets the the code writer to use to write build class code.
     * The #toString method of the specified code writer will be used to
     * generate code.
     * Generally we use an instance of {@link JkCodeWriterForBuildClass}
     */
    public JkScaffolder buildClassWriter(Object codeWriter) {
        this.mainBuildclassWriter = codeWriter;
        return this;
    }

    /**
     * Returns the build class code writer of this scaffolder.
     */
    @SuppressWarnings("unchecked")
    public <T> T buildClassCodeWriter() {
        return (T) this.mainBuildclassWriter;
    }

    /**
     * Adds an extra action to be processed while scaffolding.
     */
    public JkScaffolder extraAction(Runnable runnable) {
        this.extraActions.add(runnable);
        return this;
    }


}
