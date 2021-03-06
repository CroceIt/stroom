package stroom.security.shared;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.fusesource.restygwt.client.DirectRestService;
import stroom.util.shared.ResourcePaths;
import stroom.util.shared.RestResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Api(value = "application permissions - /v1")
@Path("/permission/app" +  ResourcePaths.V1)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AppPermissionResource extends RestResource, DirectRestService {
    @GET
    @ApiOperation(
            value = "User and app permissions for the current session",
            response = UserAndPermissions.class)
    UserAndPermissions getUserAndPermissions();

    @POST
    @Path("fetchUserAppPermissions")
    @ApiOperation(
            value = "User and app permissions for the specified user",
            response = UserAndPermissions.class)
    UserAndPermissions fetchUserAppPermissions(@ApiParam("user") User user);

    @GET
    @Path("fetchAllPermissions")
    @ApiOperation(
            value = "Get all possible permissions",
            response = List.class)
    List<String> fetchAllPermissions();

    @POST
    @Path("changeUser")
    @ApiOperation(
            value = "User and app permissions for the current session",
            response = Boolean.class)
    Boolean changeUser(@ApiParam("changeUserRequest") ChangeUserRequest changeUserRequest);
}
