/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2019
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.presentation.server.sonom.flm.service.rest;

import java.io.InputStream;

import javax.enterprise.context.RequestScoped;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST API Documentation for <code>flm-algorithm</code>.
 */
@Path("/docs")
@RequestScoped
public class DocumentationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentationResource.class);
    private static final String REST_DOCUMENTATION = "/docs/eric-frequency-layer-manager.html";

    @Context
    private ServletContext context;

    @Context
    private UriInfo uriContext;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream restDocumentation() {
        LOGGER.info("GET request received at '{}'", uriContext.getAbsolutePath());
        LOGGER.debug("Retrieving REST documentation");
        return context.getResourceAsStream(REST_DOCUMENTATION);
    }
}
