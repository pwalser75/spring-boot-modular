package ch.frostnova.spring.boot.platform.web.controller;

import ch.frostnova.spring.boot.platform.model.UserInfo;
import ch.frostnova.spring.boot.platform.service.AuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Schema(name = "Authorization controller")
@RequestMapping(path = "authorization")
@CrossOrigin(origins = "*", allowedHeaders = "origin, content-type, accept, authorization", methods = {RequestMethod.GET}, maxAge = 1209600)
public class AuthorizationController {

    @Autowired
    private AuthorizationService authorizationService;

    @Operation(summary = "Get authentication info for the current user")
    @Parameter(name = "Authorization", description = "Authorization header, e.g. \"Bearer {access-token}\"", required = true, in = ParameterIn.HEADER, example = "Bearer access_token")
    @ApiResponse(responseCode = "200", description = "ok")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public UserInfo getAuthorization() {
        return authorizationService.getUserInfo();
    }
}
