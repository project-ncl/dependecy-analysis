package org.jboss.da.bc.backend.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jboss.da.bc.backend.api.BCSetGenerator;
import org.jboss.da.bc.backend.api.BcChecker;
import org.jboss.da.bc.backend.api.Finalizer;
import org.jboss.da.bc.backend.api.RepositoryCloner;
import org.jboss.da.bc.model.ProjectDetail;
import org.jboss.da.bc.model.ProjectHiearchy;
import org.jboss.da.communication.pnc.api.PNCConnector;
import org.jboss.da.communication.pnc.model.BuildConfiguration;
import org.jboss.da.communication.pnc.model.BuildConfigurationCreate;
import org.jboss.da.scm.api.SCMType;
import org.slf4j.Logger;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@Stateless
public class FinalizerImpl implements Finalizer {

    @Inject
    private Logger log;

    @Inject
    private PNCConnector pnc;

    @Inject
    private BCSetGenerator bcSetGenerator;

    @Inject
    private RepositoryCloner repoCloner;

    @Inject
    private BcChecker bcFinder;

    @Override
    public Integer createBCs(String name, String productVersion, ProjectHiearchy toplevelBc,
            String bcSetName) {
        try {
            Set<Integer> ids = create(toplevelBc);
            int productVersionId = bcSetGenerator.createProduct(name, productVersion);
            bcSetGenerator.createBCSet(bcSetName, productVersionId, new ArrayList(ids));
            return productVersionId;
        } catch (Exception ex) {
            log.error("Failed to finalize produc import process", ex);
        }
        return null;
    }

    private Set<Integer> create(ProjectHiearchy hiearchy) throws Exception {
        Set<Integer> deps = new HashSet<>();
        if (hiearchy.isSelected()) {

            for (ProjectHiearchy dep : hiearchy.getDependencies().orElseGet(Collections::emptySet)) {
                deps.addAll(create(dep));
            }

            ProjectDetail project = hiearchy.getProject();
            BuildConfigurationCreate bcc;
            if(project.isUseExistingBc()){
                Optional<BuildConfiguration> bc = bcFinder.lookupBcByScm(project.getScmUrl(), project.getScmRevision());
                bcc = bc.orElseThrow(() -> new IllegalStateException("useExistingBC is true, but there is no BC to use."));
            }else{
                bcc = toBC(project, deps);
                if(project.isCloneRepo()){
                    try{
                        String newScmUrl = repoCloner.cloneRepository(project.getScmUrl(), project.getScmRevision(), SCMType.GIT, "Repository of " + project.getGav());
                        bcc.setScmRepoURL(newScmUrl);
                    }catch(Exception ex){
                        log.error("Failed to clone repo.", ex);
                    }
                }
            }
            BuildConfiguration bc = pnc.createBuildConfiguration(bcc);
            deps.add(bc.getId());
        }
        return deps;
    }

    private BuildConfigurationCreate toBC(ProjectDetail project, Set<Integer> deps) {
        BuildConfigurationCreate bc = new BuildConfigurationCreate();
        bc.setBuildScript(project.getBuildScript());
        bc.setDependencyIds(new ArrayList<>(deps));
        bc.setDescription(project.getDescription());
        bc.setEnvironmentId(project.getEnvironmentId());
        bc.setName(project.getName());
        bc.setProjectId(project.getProjectId());
        bc.setScmRepoURL(project.getScmUrl());
        bc.setScmRevision(project.getScmRevision());
        return bc;
    }
}
