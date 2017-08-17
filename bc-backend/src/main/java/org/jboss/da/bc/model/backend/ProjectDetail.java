package org.jboss.da.bc.model.backend;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import org.jboss.da.bc.model.BcError;

import org.jboss.da.model.rest.GAV;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ToString
public class ProjectDetail {

    private Optional<SCM> scm = Optional.empty();

    @Getter
    @Setter
    private Integer projectId; // required, non-first

    @Getter
    @Setter
    @NonNull
    private String buildScript = ""; // required

    @Getter
    @Setter
    private String name; // generated, modifiable

    @Getter
    @Setter
    private String description; // generated, modifiable

    @Getter
    @NonNull
    private final GAV gav; // generated, unmodifiable

    @Getter
    @Setter
    private Integer environmentId; // required

    @Getter
    @Setter
    private Integer bcId;

    @Getter
    @Setter
    private List<Integer> existingBCs;

    @Getter
    @Setter
    private Optional<String> internallyBuilt = Optional.empty(); // generated, unmodifiable

    @Getter
    @Setter
    private List<String> availableVersions;

    @Getter
    @Setter
    @NonNull
    private EnumSet<BcError> errors = EnumSet.noneOf(BcError.class);

    public void addError(BcError e) {
        errors.add(e);
    }

    public ProjectDetail(GAV gav) {
        this.gav = gav;
    }

    public boolean isUseExistingBc() {
        return bcId != null;
    }

    public boolean isBcExists() {
        return existingBCs == null ? false : !existingBCs.isEmpty();
    }

    public Optional<SCM> getSCM() {
        return scm;
    }

    public void setSCM(Optional<SCM> scm) {
        this.scm = scm;
    }

    public void setSCM(String url, String revision) {
        if (url == null && revision == null) {
            this.scm = Optional.empty();
        } else {
            this.scm = Optional.of(new SCM(url, revision));
        }
    }

    public static class SCM {

        @Getter
        @NonNull
        private final String url;

        @Getter
        @NonNull
        private final String revision;

        public SCM(String url, String revision) {
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("SCM url can't be null nor empty.");
            }
            if (revision == null || revision.isEmpty()) {
                throw new IllegalArgumentException("SCM revision can't be null nor empty.");
            }
            this.url = url;
            this.revision = revision;
        }

    }

}
