package com.itransition.jira.plugin.customfields.typeahead;

import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/data")
public class TypeaheadFieldRest {

    private static final TypeaheadFieldCache CACHE = new TypeaheadFieldCache();

    @GET
    @Path("/typeahead/{elementId}")
    public Response getAvailableValuesForField2(@PathParam("elementId") final String elementId, @Context final HttpServletRequest request)
            throws IOException {
        final String fieldNumberId = elementId.substring(12);
        final StringBuilder availableValues = CACHE.getAvailableElements(fieldNumberId);
        return Response.ok(new ObjectMapper().writeValueAsString(availableValues)).build();
    }

}
