package org.jboss.da.listings.api.service;

import java.util.List;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.listings.api.model.WhiteArtifact;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
public interface WhiteArtifactService extends ArtifactService<WhiteArtifact> {

    /**
     * Add artifact to whitelist.
     * The version must contain redhat suffix.
     * @throws IllegalArgumentException when version doesn't have redhat suffix.
     */
    @Override
    public STATUS addArtifact(String groupId, String artifactId, String version)
            throws IllegalArgumentException;

    /**
     * Checks if whitelist contains artifact with specific groupId, artifactId and version.
     * If the version have redhat suffix, find exact match. If the version doesn't have redhat 
     * suffix, converts the version to OSGi version and finds any redhat suffixed versions
     * in whitelist.
     * @return List of found artifacts.
     */
    public List<WhiteArtifact> getArtifacts(GAV gav);

    /**
     * Checks if whitelist contains artifact with specific groupId, artifactId and version.
     * If the version have redhat suffix, find exact match. If the version doesn't have redhat
     * suffix, converts the version to OSGi version and finds any redhat suffixed versions
     * in whitelist.
     * @return List of found artifacts.
     */
    public List<WhiteArtifact> getArtifacts(String groupId, String artifactId, String version);
}
