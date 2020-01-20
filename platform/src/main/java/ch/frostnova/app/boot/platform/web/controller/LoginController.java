package ch.frostnova.app.boot.platform.web.controller;

import ch.frostnova.app.boot.platform.service.JWTSigningService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @ApiOperation(value = "Issue a JWT for the given tenant/user and claims", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok")
    })
    @GetMapping(path = "/{tenant}/{user}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String login(@ApiParam(value = "Tenant id, required")
                        @PathVariable("tenant") @NotBlank String tenant,
                        @ApiParam(value = "User id (subject), required")
                        @PathVariable("user") @NotBlank String login,
                        @ApiParam(value = "Set of granted roles (optional)")
                        @RequestParam(value = "roles", required = false) Set<String> roles,
                        @ApiParam(value = "Valid from (optional, defaults to now)")
                        @RequestParam(value = "valid-from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<OffsetDateTime> validFrom,
                        @ApiParam(value = "Validity (duration, optional (default: 1h) in ?d?h?m?s?ms format, e.g. 1h23m56s")
                        @RequestParam(value = "duration", required = false, defaultValue = "1h") Duration duration,
                        HttpServletRequest request) {

        Map<String, Object> additionalClaims = new HashMap<>();

        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> reserved = Set.of("roles", "valid-from", "duration");
        parameterMap.forEach((k, v) -> {
            if (!reserved.contains(k)) {
                additionalClaims.put(k, Arrays.stream(v).collect(Collectors.toList()));
            }
        });


        return jwtSigningService.createJWT(tenant, login, roles, additionalClaims, validFrom.orElse(OffsetDateTime.now()), duration);
    }
}
