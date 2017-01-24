package com.cosmoport.core.api;

import com.cosmoport.core.persistence.TestPersistenceService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/test")
@Singleton
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
//@GZIP
public class TestResource {
    private final TestPersistenceService testPersistenceService;

    @Inject
    public TestResource(TestPersistenceService userPersistenceService) {
        this.testPersistenceService = userPersistenceService;
    }

    @GET
    @Path("/")
    public Response get() {
        return Response
                .status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Max-Age", "1209600")
                .entity(testPersistenceService.getAll())
                .build();
    }
}
