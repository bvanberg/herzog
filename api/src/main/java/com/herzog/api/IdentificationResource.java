package com.herzog.api;

import com.google.inject.Inject;
import com.herzog.api.photo.store.Photo;
import com.herzog.api.photo.store.PhotoStore;
import com.herzog.api.service.S3Service;
import io.dropwizard.jersey.params.IntParam;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

@Path("/identification")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class IdentificationResource {

    private final PhotoStore photoStore;
    private final S3Service s3Service;

    @Inject
    public IdentificationResource(final S3Service s3Service) {
        this.s3Service = s3Service;
        photoStore = PhotoStore.builder().build();
    }

//    @GET
//    @Path("photos")
//    public PhotoList fetch() {
//        final Collection<Photo> notifications = photoStore.getPhotoList();
//        if (notifications != null) {
//            final PhotoList photoList = PhotoList.builder().photos(notifications).build();
//            return photoList;
//        }
//        throw new WebApplicationException(Response.Status.NOT_FOUND);
//    }

    @GET
    @Path("photos")
    public PhotoList fetch(
            @QueryParam("page") @DefaultValue("1") IntParam page,
            @QueryParam("pageSize") @DefaultValue("10") IntParam pageSize
    ) {
        final Collection<Photo> notifications = photoStore.getPhotoPage(page.get(), pageSize.get());

        if (notifications != null) {
            return PhotoList.builder().photos(notifications).build();
        }

        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @POST
    @Consumes("binary/octet-stream")
    public Response putFile(
            @Context HttpServletRequest request,
            @QueryParam("fileId") long fileId,
            InputStream fileInputStream
    ) throws Throwable {

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
        while (bytesRead != -1) {
            bytes += bytesRead;
            log.info("Bytes read: {}, Total bytes read: {}", bytesRead, bytes);
            bytesRead = fileInputStream.read(buffer);
        }
        fileInputStream.close();
        return bytes;
    }
}
