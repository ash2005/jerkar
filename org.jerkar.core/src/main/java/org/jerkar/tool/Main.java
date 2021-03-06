package org.jerkar.tool;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.jerkar.api.system.JkLog;
import org.jerkar.api.utils.JkUtilsFile;
import org.jerkar.api.utils.JkUtilsIO;
import org.jerkar.api.utils.JkUtilsString;
import org.jerkar.api.utils.JkUtilsTime;

/**
 * Main class for launching Jerkar from command line.
 * 
 * @author Jerome Angibaud
 */
final class Main {

    public static void main(String[] args) {
        final long start = System.nanoTime();
        displayIntro();
        final JkInit init = JkInit.of(args);
        init.displayInfo();

        final File workingDir = JkUtilsFile.workingDir();
        final Project project = new Project(workingDir);
        JkLog.nextLine();
        try {
            project.execute(init);
            final int lenght = printAscii(false, "success.ascii");
            System.out.println(JkUtilsString.repeat(" ", lenght) + "Total build time : "
                    + JkUtilsTime.durationInSeconds(start) + " seconds.");
        } catch (final RuntimeException e) {
            System.err.println();
            e.printStackTrace(System.err);
            final int lenght = printAscii(true, "failed.ascii");
            System.err.println(JkUtilsString.repeat(" ", lenght) + "Total build time : "
                    + JkUtilsTime.durationInSeconds(start) + " seconds.");
            System.exit(1);
        }
    }

    private static int printAscii(boolean error, String fileName) {
        final InputStream inputStream = Main.class.getResourceAsStream(fileName);
        final List<String> lines = JkUtilsIO.readAsLines(inputStream);
        int i = 0;
        for (final String line : lines) {
            if (i < line.length()) {
                i = line.length();
            }
            if (error) {
                System.err.println(line);
            } else {
                System.out.println(line);
            }
        }
        return i;
    }

    private static void displayIntro() {
        final int lenght = printAscii(false, "jerkar.ascii");
        JkLog.info(JkUtilsString.repeat(" ", lenght) + "The 100% Java build tool.");
        JkLog.nextLine();
    }

    private Main() {
    }

}
