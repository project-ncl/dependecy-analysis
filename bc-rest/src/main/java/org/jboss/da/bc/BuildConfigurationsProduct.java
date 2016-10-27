package org.jboss.da.bc;

import org.jboss.da.bc.facade.BuildConfigurationsFacade;
import org.jboss.da.bc.facade.BuildConfigurationsProductFacade;
import org.jboss.da.bc.model.rest.EntryEntity;
import org.jboss.da.bc.model.rest.ProductFinishResponse;
import org.jboss.da.bc.model.rest.ProductInfoEntity;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Optional;
import org.jboss.da.validation.Validation;

@Path("/build-configuration/generate/product")
@Api(value = "product")
public class BuildConfigurationsProduct extends BuildConfigurationsREST<ProductInfoEntity> {

    @Inject
    BuildConfigurationsProductFacade bcpf;

    @Inject
    Logger log;

    @Inject
    private Validation validation;

    @Override
    @ApiOperation(value = "Start initial analyse of product", response = ProductInfoEntity.class)
    public Response startAnalyse(EntryEntity entry) {
        Optional<Response> validationResponse = validation.validation(entry,
                "Starting initial analyse of product failed");
        return validationResponse.orElseGet(()->super.startAnalyse(entry));
    }

    @Override
    @POST
    // When annotations @Post, @Path and @Produces were on parent, the endpoint was not found (I dont know why, jbrazdil)
    @Path("/analyse-next-level")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Analyse next level of product dependencies",
            response = ProductInfoEntity.class)
    public Response analyseNextLevel(
            @ApiParam(value = "Detail information needed to create BCs") ProductInfoEntity bc) {
        Optional<Response> validationResponse = validation.validation(bc,
                "Analyse next level of product dependencies failed");
        return validationResponse.orElseGet(()->super.analyseNextLevel(bc));
    }

    @Override
    @POST
    // When annotations @Post, @Path and @Produces were on parent, the endpoint was not found (I dont know why, jbrazdil)
    @Path("/finish-process")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Finish analysis and create BCs", response = ProductFinishResponse.class)
    public Response finishAnalyse(@ApiParam(
            value = "Complete detail information needed to create BCs") ProductInfoEntity bc) {
        Optional<Response> validationResponse = validation.validation(bc,
                "Finish analysis and create BCs failed");
        return validationResponse.orElseGet(()->super.finishAnalyse(bc));
    }

    @Override
    protected BuildConfigurationsFacade<ProductInfoEntity> getFacade() {
        return bcpf;
    }

}
