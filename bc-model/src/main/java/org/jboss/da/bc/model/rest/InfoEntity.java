package org.jboss.da.bc.model.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@EqualsAndHashCode
public class InfoEntity {

    @Getter
    @Setter
    protected int id;

    @Getter
    @Setter
    protected String pomPath;

    @Getter
    @Setter
    protected BuildConfiguration topLevelBc;

    @Getter
    @Setter
    protected String bcSetName;

    @Getter
    @Setter
    @JsonProperty(required = false)
    protected String securityToken;

}
