package org.jboss.da.reports.model.api;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import org.jboss.da.model.rest.validators.ScmUrl;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ToString
public class SCMLocator {

    @Getter
    @NonNull
    @ScmUrl
    private String scmUrl;

    @Getter
    @NonNull
    private String revision;

    @Getter
    @NonNull
    private String pomPath;

    @Getter
    @NonNull
    private List<String> repositories = Collections.emptyList();

    private SCMLocator() {
    }

    private SCMLocator(String scmUrl, String revision, String pomPath, List<String> repositories) {
        this.scmUrl = Objects.requireNonNull(scmUrl);
        this.revision = Objects.requireNonNull(revision);
        this.pomPath = Objects.requireNonNull(pomPath);
        if (repositories != null) {
            this.repositories = repositories;
        }
    }

    public static SCMLocator internal(String scmUrl, String revision, String pomPath) {
        return internal(scmUrl, revision, pomPath, null);
    }

    public static SCMLocator internal(String scmUrl, String revision, String pomPath, List<String> repositories) {
        SCMLocator ret = new SCMLocator(scmUrl, revision, pomPath, repositories);
        return ret;
    }

    public static SCMLocator generic(String scmUrl, String revision, String pomPath) {
        return generic(scmUrl, revision, pomPath, Collections.emptyList());
    }

    public static SCMLocator generic(String scmUrl, String revision, String pomPath, List<String> repositories) {
        SCMLocator ret = new SCMLocator(scmUrl, revision, pomPath, repositories);
        return ret;
    }
}
