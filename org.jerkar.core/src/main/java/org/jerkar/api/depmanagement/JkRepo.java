package org.jerkar.api.depmanagement;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jerkar.api.system.JkLocator;
import org.jerkar.api.utils.JkUtilsFile;
import org.jerkar.api.utils.JkUtilsIterable;
import org.jerkar.api.utils.JkUtilsString;

public abstract class JkRepo implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final URL MAVEN_CENTRAL_URL = toUrl("http://repo1.maven.org/maven2");

    public static final URL MAVEN_OSSRH_DOWNLOAD_AND_DEPLOY_SNAPSHOT = toUrl("https://oss.sonatype.org/content/repositories/snapshots/");

    public static final URL MAVEN_OSSRH_DOWNLOAD_RELEASE = toUrl("https://oss.sonatype.org/content/repositories/releases/");

    public static final URL MAVEN_OSSRH_DEPLOY_RELEASE = toUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/");

    public static final URL MAVEN_OSSRH_PUBLIC_DOWNLOAD_RELEASE_AND_SNAPSHOT = toUrl("https://oss.sonatype.org/content/groups/public/");

    public static final URL JCENTERL_URL = toUrl("https://jcenter.bintray.com");

    public static JkRepo ofOptional(String url, String userName, String password) {
        if (JkUtilsString.isBlank(url)) {
            return null;
        }
        return of(url).withCredential(userName, password);
    }

    public static JkRepo firstNonNull(JkRepo... repos) {
        for (final JkRepo repo : repos) {
            if (repo != null) {
                return repo;
            }
        }
        throw new NullPointerException("All repository arguments are null.");
    }

    public static JkMavenRepository maven(String url) {
        return new JkMavenRepository(toUrl(url), null, null, null);
    }

    public static JkMavenRepository maven(URL url) {
        return new JkMavenRepository(url, null, null, null);
    }

    public static JkMavenRepository maven(File file) {
        return new JkMavenRepository(JkUtilsFile.toUrl(file), null, null, null);
    }

    public static JkRepo mavenCentral() {
        return maven(JkMavenRepository.MAVEN_CENTRAL_URL.toString());
    }

    /**
     * A maven repository located in Jekkar user Home. You can use it to
     * "deploy" locally.
     *
     * @return
     */
    public static JkRepo mavenLocal() {
        final File file = new File(JkLocator.jerkarUserHome(), "maven-publish-dir");
        return JkRepo.maven(file);
    }

    public static JkRepo mavenOssrhDownloadAndDeploySnapshot(String jiraId, String jiraPassword) {
        return maven(JkMavenRepository.MAVEN_OSSRH_DOWNLOAD_AND_DEPLOY_SNAPSHOT.toString())
                .withCredential(jiraId, jiraPassword)
                .withRealm("Sonatype Nexus Repository Manager");
    }

    public static JkRepo mavenOssrhDeployRelease(String jiraId, String jiraPassword) {
        return maven(JkMavenRepository.MAVEN_OSSRH_DEPLOY_RELEASE.toString()).withCredential(
                jiraId, jiraPassword).withRealm("Sonatype Nexus Repository Manager");
    }

    public static JkRepo mavenOssrhPublicDownload() {
        return maven(JkMavenRepository.MAVEN_OSSRH_PUBLIC_DOWNLOAD_RELEASE_AND_SNAPSHOT);
    }

    public static JkRepo mavenJCenter() {
        return maven(JkMavenRepository.JCENTERL_URL.toString());
    }

    public JkPublishRepo asPublishRepo() {
        return JkPublishRepo.of(this);
    }

    public JkPublishRepos asPublishRepos() {
        return JkPublishRepos.of(JkPublishRepo.of(this));
    }

    public JkPublishRepo asPublishSnapshotRepo() {
        return JkPublishRepo.ofSnapshot(this);
    }

    public JkRepos asRepos() {
        return JkRepos.of(this);
    }

    public JkPublishRepo asPublishReleaseRepo() {
        return JkPublishRepo.ofRelease(this);
    }

    public static JkRepo of(String url) {
        if (url.toLowerCase().startsWith("ivy:")) {
            return JkRepo.ivy(url.substring(4));
        }
        return JkRepo.maven(url);
    }

    public static JkRepo.JkIvyRepository ivy(URL url) {
        return new JkIvyRepository(url, null, null, null, null, null);
    }

    public static JkRepo.JkIvyRepository ivy(File file) {
        try {
            return ivy(file.toURI().toURL());
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static JkRepo.JkIvyRepository ivy(String urlOrDir) {
        if (urlOrDir.toLowerCase().startsWith("ivy:")) {
            return JkRepo.ivy(urlOrDir.substring(4));
        }
        return ivy(toUrl(urlOrDir));
    }

    private final URL url;

    private final String userName;

    private final String realm;

    private final String password;

    private JkRepo(URL url, String realm, String userName, String password) {
        this.url = url;
        this.realm = realm;
        this.userName = userName;
        this.password = password;
    }

    public final URL url() {
        return url;
    }

    public final String realm() {
        return realm;
    }

    public final String userName() {
        return userName;
    }

    public final String password() {
        return password;
    }

    public boolean hasCredentials() {
        return !JkUtilsString.isBlank(userName);
    }

    public final JkRepo withOptionalCredentials(String userName, String password) {
        if (JkUtilsString.isBlank(userName)) {
            return this;
        }
        return this.withCredential(userName, password);
    }

    public JkRepos and(JkRepo other) {
        return JkRepos.of(this, other);
    }

    public abstract JkRepo withRealm(String realm);

    public abstract JkRepo withCredential(String username, String password);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JkRepo other = (JkRepo) obj;
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + url + ")";
    }

    private static URL toUrl(String urlOrDir) {
        try {
            return new URL(urlOrDir);
        } catch (final MalformedURLException e) {
            final File file = new File(urlOrDir);
            if (file.isAbsolute()) {
                return JkUtilsFile.toUrl(file);
            } else {
                throw new IllegalArgumentException("<Malformed url " + urlOrDir, e);
            }
        }
    }

    public static final class JkMavenRepository extends JkRepo {

        private static final long serialVersionUID = 1L;

        private JkMavenRepository(URL url, String realm, String userName, String password) {
            super(url, realm, userName, password);
        }

        @Override
        public JkRepo withCredential(String username, String password) {
            return new JkMavenRepository(this.url(), this.realm(), username, password);
        }

        @Override
        public JkRepo withRealm(String realm) {
            return new JkMavenRepository(this.url(), realm, this.userName(), this.password());
        }

    }

    public static final class JkIvyRepository extends JkRepo {

        private static final long serialVersionUID = 1L;

        private final List<String> artifactPatterns;

        private final List<String> ivyPatterns;

        private static final String DEFAULT_IVY_ARTIFACT_PATTERN = "[organisation]/[module]/[type]s/[artifact]-[revision](-[type]).[ext]";

        private static final String DEFAULT_IVY_IVY_PATTERN = "[organisation]/[module]/ivy-[revision].xml";

        private JkIvyRepository(URL url, String realm, String username, String password,
                List<String> artifactPatterns, List<String> ivyPatterns) {
            super(url, realm, username, password);
            this.artifactPatterns = artifactPatterns;
            this.ivyPatterns = ivyPatterns;
        }

        public JkIvyRepository artifactPatterns(String... patterns) {
            return new JkIvyRepository(this.url(), this.realm(), this.userName(), this.password(),
                    Collections.unmodifiableList(Arrays.asList(patterns)), ivyPatterns);
        }

        public JkIvyRepository ivyPatterns(String... patterns) {
            return new JkIvyRepository(this.url(), this.realm(), this.userName(), this.password(),
                    artifactPatterns, Collections.unmodifiableList(Arrays.asList(patterns)));
        }

        public List<String> artifactPatterns() {
            if (this.artifactPatterns == null) {
                return JkUtilsIterable.listOf(DEFAULT_IVY_ARTIFACT_PATTERN);
            }
            return artifactPatterns;
        }

        public List<String> ivyPatterns() {
            if (this.ivyPatterns == null) {
                return JkUtilsIterable.listOf(DEFAULT_IVY_IVY_PATTERN);
            }
            return ivyPatterns;
        }

        @Override
        public JkRepo withCredential(String username, String password) {
            return new JkIvyRepository(this.url(), this.realm(), username, password,
                    this.artifactPatterns, this.ivyPatterns);
        }

        @Override
        public JkRepo withRealm(String realm) {
            return new JkIvyRepository(this.url(), realm, this.userName(), this.password(),
                    this.artifactPatterns, this.ivyPatterns);
        }

    }

}
