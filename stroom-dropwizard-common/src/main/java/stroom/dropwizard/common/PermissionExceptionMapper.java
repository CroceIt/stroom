package stroom.dropwizard.common;

import stroom.util.shared.PermissionException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class PermissionExceptionMapper implements ExceptionMapper<PermissionException> {
    @Override
    public Response toResponse(PermissionException exception) {
        return Response
                .status(Response.Status.FORBIDDEN)
                .build();
    }
}
