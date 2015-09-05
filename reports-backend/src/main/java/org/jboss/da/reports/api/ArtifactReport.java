package org.jboss.da.reports.api;

import org.jboss.da.communication.model.GAV;

import javax.xml.bind.annotation.XmlTransient;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Class, which represents one report for the top-level GAV
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@RequiredArgsConstructor
public class ArtifactReport {

    @Getter
    @NonNull
    @XmlTransient
    private GAV gav;

    @NonNull
    private final Set<String> availableVersions = new HashSet<>();

    @Getter
    @NonNull
    private Optional<String> bestMatchVersion = Optional.empty();

    @NonNull
    private final Set<ArtifactReport> dependencies = new HashSet<>();

    /**
     * Indicator if the artifact was blacklisted
     */
    @Getter
    @Setter
    private boolean blacklisted;

    /**
     * Indicator if the artifact was whiteListed
     */
    @Getter
    @Setter
    private boolean whiteListed;

    public void setBestMatchVersion(Optional<String> version) {
        if (version.isPresent()) {
            availableVersions.add(version.get());
        }
        bestMatchVersion = version;
    }

    public void addAvailableVersion(String version) {
        availableVersions.add(version);
    }

    public void addAvailableVersions(Collection<String> version) {
        availableVersions.addAll(version);
    }

    public void addDependency(ArtifactReport dependency) {
        dependencies.add(dependency);
    }

    public Set<String> getAvailableVersions() {
        return Collections.unmodifiableSet(availableVersions);
    }

    public Set<ArtifactReport> getDependencies() {
        return Collections.unmodifiableSet(dependencies);
    }

    public String getGroupId() {
        return gav.getGroupId();
    }

    public String getArtifactId() {
        return gav.getArtifactId();
    }

    public String getVersion() {
        return gav.getVersion();
    }

    /**
     * Returns true if this artifact and all the dependencies of this artifact have a GAV already in PNC/Brew.
     * @return true if this artifact and all the dependencies of this artifact have a GAV already in PNC/Brew.
     */
    public boolean isDependencyVersionSatisfied() {
        if (!bestMatchVersion.isPresent()) {
            return false;
        }
        return dependencies.stream().noneMatch((dependency) -> (!dependency.isDependencyVersionSatisfied()));
    }

    public int getNotBuiltDependencies() {
        return dependencies.stream().mapToInt((dependency) -> {
            int number = dependency.getNotBuiltDependencies();
            if(!dependency.bestMatchVersion.isPresent()){
                number ++;
            }
            return number;
        }).sum();
    }
}
