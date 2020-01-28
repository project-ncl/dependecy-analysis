package org.jboss.da.reports.backend.api;

import org.jboss.da.communication.aprox.FindGAVDependencyException;

import org.apache.maven.scm.ScmException;
import org.jboss.da.common.CommunicationException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.model.rest.GAV;
import org.jboss.da.reports.model.api.SCMLocator;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface DependencyTreeGenerator {

    public GAVDependencyTree getDependencyTree(SCMLocator scml) throws ScmException, PomAnalysisException;

    public GAVDependencyTree getDependencyTree(GAV gav) throws CommunicationException, FindGAVDependencyException;

    public GAVDependencyTree getDependencyTree(String url, String revision, GAV gav) throws ScmException, PomAnalysisException;

    public GAVToplevelDependencies getToplevelDependencies(SCMLocator scml) throws ScmException, PomAnalysisException;

    public GAVToplevelDependencies getToplevelDependenciesFromModules(SCMLocator scml)
            throws ScmException, PomAnalysisException;

    public GAVToplevelDependencies getToplevelDependencies(GAV gav) throws CommunicationException, FindGAVDependencyException;

    public GAVToplevelDependencies getToplevelDependencies(String url, String revision, GAV gav)
            throws ScmException, PomAnalysisException;

}
