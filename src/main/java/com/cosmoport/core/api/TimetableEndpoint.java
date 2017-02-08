package com.cosmoport.core.api;

import com.cosmoport.core.dto.TimetableDto;
import com.cosmoport.core.persistence.TimetablePersistenceService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.resteasy.annotations.GZIP;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/timetable")
@Singleton
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@GZIP
public final class TimetableEndpoint {
    private final TimetablePersistenceService timetablePersistenceService;

    @Inject
    public TimetableEndpoint(TimetablePersistenceService timetablePersistenceService) {
        this.timetablePersistenceService = timetablePersistenceService;
    }

    @GET
    @Path("/")
    public List<TimetableDto> get() {
        return timetablePersistenceService.getAll();
    }
}
