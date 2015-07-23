package org.jboss.da.rest.listings.api;

import org.jboss.da.rest.listings.api.model.ContainsResponse;
import org.jboss.da.rest.listings.api.model.RestArtifact;
import org.jboss.da.rest.listings.api.model.SuccessResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import java.util.Collection;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@Path("/listings")
public interface Artifacts {

    @POST
    @Path("/white")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SuccessResponse addWhiteArtifact(RestArtifact artifact);

    @POST
    @Path("/black")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SuccessResponse addBlackArtifact(RestArtifact artifact);

    @GET
    @Path("/white")
    @Produces(MediaType.APPLICATION_JSON)
    ContainsResponse isWhiteArtifactPresent(@QueryParam("groupid") String groupId,
            @QueryParam("artifactid") String artifactId,
            @QueryParam("version") String version);

    @GET
    @Path("/black")
    @Produces(MediaType.APPLICATION_JSON)
    ContainsResponse isBlackArtifactPresent(@QueryParam("groupid") String groupId,
            @QueryParam("artifactid") String artifactId,
            @QueryParam("version") String version);

    @GET
    @Path("/whitelist")
    @Produces(MediaType.APPLICATION_JSON)
    Collection<RestArtifact> getAllWhiteArtifacts();

    @GET
    @Path("/blacklist")
    @Produces(MediaType.APPLICATION_JSON)
    Collection<RestArtifact> getAllBlackArtifacts();
    
    @DELETE
    @Path("/white")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SuccessResponse removeWhiteArtifact(RestArtifact artifact);

    @DELETE
    @Path("/black")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SuccessResponse removeBlackArtifact(RestArtifact artifact);
}
