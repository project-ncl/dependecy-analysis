package org.jboss.da.reports.api;

import org.jboss.da.model.rest.GAV;

import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
public class BuiltReportModule {

    public BuiltReportModule(GAV gav) {
        this.groupId = gav.getGroupId();
        this.artifactId = gav.getArtifactId();
        this.version = gav.getVersion();
    }

    @Getter
    private final String groupId;

    @Getter
    private final String artifactId;

    @Getter
    private final String version;

    @Getter
    @Setter
    private String builtVersion;

    @Getter
    @Setter
    private List<String> availableVersions = Collections.emptyList();

}
