package com.herzog.api;

import com.google.inject.Inject;
import com.herzog.api.command.PhotoMetadataCommand;
import com.herzog.api.photo.UniquePhotoKey;
import com.herzog.api.photo.store.Photo;
import com.herzog.api.photo.store.PhotoMetadata;
import com.herzog.api.photo.store.PhotoStore;
import com.herzog.api.s3.ItemUrl;
import com.herzog.api.s3.PresignedUrl;
import io.dropwizard.jersey.params.IntParam;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Collection;

@Path("/identification")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class IdentificationResource {

    private final PhotoStore photoStore;

    @Inject
    public IdentificationResource() {
        photoStore = PhotoStore.builder().build();
    }

    /**
     * Fetch and page photos endpoint. Added to support initial mockup of identification stream.
     *
     * @param page Page number to return.
     * @param pageSize Page size to return.
     * @return Paged results.
     */
    @GET
    @Path("photos")
    public PhotoList fetch(@QueryParam("page") @DefaultValue("1") IntParam page,
                           @QueryParam("pageSize") @DefaultValue("10") IntParam pageSize) {
        final Collection<Photo> notifications = photoStore.getPhotoPage(page.get(), pageSize.get());

        if (notifications != null) {
            return PhotoList.builder().photos(notifications).build();
        }

        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    /**
     * First step in submitting items. Returns the presigned url for uploading the item along with the key for the item.
     *
     * @return Presigned URL response.
     */
    @GET
    @Path("photo/url")
    public ItemUrl getPresignedUrl() {
        final String key = UniquePhotoKey.get();
        return ItemUrl.builder()
                .presignedUrl(PresignedUrl.from(key).toString())
                .key(key)
                .build();
    }

    /**
     * Return metadata. TODO: Might be nice to return the metadata for a certain key.
     *
     * @return Photo metadata.
     */
    @GET
    @Path("photo/metadata")
    public PhotoMetadata getMetadata() {
        return PhotoMetadata.builder()
                .metadata("key1", "value1")
                .metadata("key2", "value2")
                .photoKeys("key1")
                .photoKeys("key2")
                .userId("user")
                .build();
    }

    /**
     * Second step in submitting items. Posts the item metadata which persists metadata associated with item artifacts.
     *
     * @param metadata Photo metadata payload.
     * @return Response.
     * @throws Throwable
     */
    @POST
    @Path("photo/metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putMetadata(@Valid final PhotoMetadata metadata) throws Throwable {
        final boolean success = new PhotoMetadataCommand(metadata).run();

        // TODO: flesh out this response. currently just reflect back the input.
        Response.ResponseBuilder response =
                Response.created(UriBuilder.fromResource(IdentificationResource.class).build());

        metadata.getMetadata().entrySet().stream()
                .forEach(e -> response.header(e.getKey(), e.getValue()));
        response.header("success", success);

        return response.build();
    }
}
