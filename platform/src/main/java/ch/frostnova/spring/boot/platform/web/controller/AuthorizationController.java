package ch.frostnova.spring.boot.platform.web.controller;

import ch.frostnova.spring.boot.platform.model.UserInfo;
import ch.frostnova.spring.boot.platform.service.AuthorizationService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(value = "Authorization controller")
@RequestMapping(path = "authorization")
@CrossOrigin(origins = "*",
        allowedHeaders = "origin, content-type, accept, authorization",
        allowCredentials = "true",
        methods = {RequestMethod.GET},
        maxAge = 1209600)
public class AuthorizationController {

    @Autowired
    private AuthorizationService authorizationService;

    @ApiOperation(value = "Get authentication info for the current user", response = UserInfo.class)
    @ApiImplicitParam(name = "Authorization", value = "Authorization header, e.g. \"Bearer {access-token}\"", required = true, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public UserInfo getAuthorization() {
        return authorizationService.getUserInfo();
    }
}
