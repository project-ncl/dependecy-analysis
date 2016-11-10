package org.jboss.da.bc.model.backend;

import lombok.Getter;
import lombok.Setter;

import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.model.api.SCMLocator;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public abstract class GeneratorEntity {

    @Getter
    @Setter
    int id;

    @Getter
    @Setter
    String bcSetName;

    @Getter
    @Setter
    protected String pomPath;

    @Getter
    @Setter
    protected String authToken;

    @Getter
    @Setter
    ProjectHiearchy toplevelBc;

    protected GeneratorEntity(SCMLocator scm, int id, GAV gav) {
        ProjectDetail pd = new ProjectDetail(gav);
        if (scm.isInternal()) {
            pd.setScmUrl(scm.getScmUrl());
            pd.setScmRevision(scm.getRevision());
        } else {
            pd.setExternalScmUrl(scm.getScmUrl());
            pd.setExternalScmRevision(scm.getRevision());
        }

        this.id = id;
        this.pomPath = scm.getPomPath();
        this.toplevelBc = new ProjectHiearchy(pd, true);
    }

    public ProjectDetail getToplevelProject() {
        return toplevelBc.getProject();
    }

    @FunctionalInterface
    public interface EntityConstructor<T extends GeneratorEntity> {

        T construct(SCMLocator scm, int id, GAV gav);
    }
}
