package com.herzog.api;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

@Path("/identification")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class IdentificationResource {
    private final String template;
    private final String defaultName;
    private final AtomicLong counter;

    public IdentificationResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    public Identification sayHello(@QueryParam("name") Optional<String> name) {
        final String value = String.format(template, name.or(defaultName));
        return new Identification(counter.incrementAndGet(), value);
    }

    @POST
    @Consumes("application/octet-stream")
    public Response putFile(@Context HttpServletRequest request,
                            @QueryParam("fileId") long fileId,
                            InputStream fileInputStream) throws Throwable {
        long bytes = getBytes(fileInputStream);
        return Response.created(UriBuilder.fromResource(IdentificationResource.class).build())
                .header("total-bytes", bytes)
                .header("fileId", fileId)
                .build();
    }

    private long getBytes(InputStream fileInputStream) throws IOException {
        final byte[] buffer = new byte[1024];
        long bytes = 0;
        int bytesRead = fileInputStream.read(buffer);
        while(bytesRead != -1) {
            bytes += bytesRead;
            log.info("Bytes read: {}, Total bytes read: {}", bytesRead, bytes);
            bytesRead = fileInputStream.read(buffer);
        }
        fileInputStream.close();
        return bytes;
    }
}
