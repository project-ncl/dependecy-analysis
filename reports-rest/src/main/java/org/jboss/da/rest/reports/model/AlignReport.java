package org.jboss.da.rest.reports.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@NoArgsConstructor
public class AlignReport {

    @Getter
    @NonNull
    private Set<RestGA2RestGAV2VersionProducts> internallyBuilt = new HashSet<>();

    @Getter
    @NonNull
    private Set<RestGA2RestGAV2VersionProducts> builtInDifferentVersion = new HashSet<>();

    @Getter
    @NonNull
    private Set<RestGA2GAVs> notBuilt = new HashSet<>();

    @Getter
    @NonNull
    private Set<RestGA2GAVs> blacklisted = new HashSet<>();

}
