package ch.frostnova.app.boot.platform.web.controller;

import ch.frostnova.app.boot.platform.service.JWTSigningService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

@RestController
@ConditionalOnBean(JWTSigningService.class)
@Api(value = "Login Controller - use for testing only, never in production")
@RequestMapping(path = "login")
@CrossOrigin(origins = "*",
        allowedHeaders = "origin, content-type, accept, authorization",
        allowCredentials = "true",
        methods = {RequestMethod.GET},
        maxAge = 1209600)
public class LoginController {

    @Autowired
    private JWTSigningService jwtSigningService;

    private Duration duration = Duration.of(1, ChronoUnit.DAYS);


    @ApiOperation(value = "Issue a JWT for the given tenant/user and claims", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok")
    })
    @GetMapping(path = "/{tenant}/{user}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String login(@ApiParam(value = "Tenant id, required") @PathVariable("tenant") @NotBlank String tenant,
                        @ApiParam(value = "User id (subject), required") @PathVariable("user") @NotBlank String login,
                        @ApiParam(value = "Set of granted roles (optional)") @RequestParam(value = "roles", required = false) Set<String> roles) {

        Map<String, Object> additionalClaims = null;

        return jwtSigningService.createJWT(tenant, login, roles, additionalClaims, duration);
    }
}
