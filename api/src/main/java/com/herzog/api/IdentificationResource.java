package com.herzog.api;

import com.google.inject.Inject;
import com.herzog.api.photo.store.Photo;
import com.herzog.api.photo.store.PhotoStore;
import com.herzog.api.service.S3Service;
import io.dropwizard.jersey.params.IntParam;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
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
			@Context final HttpServletRequest request,
			@HeaderParam(HttpHeaders.CONTENT_LENGTH) final long contentLength,
			@QueryParam("fileId") long fileId,
			final InputStream fileInputStream
	) throws Throwable {

		try {
			// todo: figure out a better way to determine the file type/extension, hard-coding
			// todo: the .jpg extension like this is what jokers fucking do and jokers fuck other people up as well as images.
			s3Service.saveFile(fileInputStream, contentLength, String.valueOf(fileId) + ".jpg");
		} catch (final Exception ex) {
			log.error("reading file and saving to S3 failed", ex);
		} finally {
			if (fileInputStream != null) try { fileInputStream.close(); } catch(final IOException io) {}
		}

		return Response.created(UriBuilder.fromResource(IdentificationResource.class).build())
				.header("total-bytes", contentLength)
				.header("fileId", fileId)
				.build();
	}

}
