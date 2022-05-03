package ch.frostnova.spring.boot.platform.web.controller;

import ch.frostnova.spring.boot.platform.model.UserInfo;
import ch.frostnova.spring.boot.platform.service.JWTSigningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@ConditionalOnBean(JWTSigningService.class)
@Schema(name = "Login Controller - use for testing only, never in production")
@RequestMapping(path = "login")
@CrossOrigin(origins = "*",
        allowedHeaders = "origin, content-type, accept, authorization",
        methods = {RequestMethod.GET},
        maxAge = 1209600)
public class LoginController {

    @Autowired
    private JWTSigningService jwtSigningService;

    @Operation(summary = "Issue a JWT for the given tenant/user and claims")
    @ApiResponse(responseCode = "200", description = "ok")
    @GetMapping(path = "/{tenant}/{user}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String login(@Parameter(description = "Tenant id, required", example = "test-tenant")
                        @PathVariable("tenant") @NotBlank String tenant,
                        @Parameter(description = "User id (subject), required", example = "USER01")
                        @PathVariable("user") @NotBlank String login,
                        @Parameter(description = "Set of granted roles (optional)")
                        @RequestParam(value = "roles", required = false) Set<String> roles,
                        @Parameter(description = "Valid from, in ISO date time format, e.g. 2020-01-01T12:34:56+01:00 (optional, defaults to now)")
                        @RequestParam(value = "valid-from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime validFrom,
                        @Parameter(description = "Validity (duration, optional (default: 1h) in ?d?h?m?s?ms format, e.g. 5d, or 5m30s, or 1h23m56s", example = "1h")
                        @RequestParam(value = "duration", required = false, defaultValue = "1h") Duration duration,
                        HttpServletRequest request) {

        Map<String, String> additionalClaims = new HashMap<>();

        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> reserved = Set.of("roles", "valid-from", "duration");
        parameterMap.forEach((k, v) -> {
            if (!reserved.contains(k)) {
                additionalClaims.put(k, Arrays.stream(v).map(String::valueOf).collect(Collectors.joining(",")));
            }
        });

        UserInfo userInfo = UserInfo.aUserInfo().tenant(tenant).login(login).roles(roles).additionalClaims(additionalClaims).build();

        return jwtSigningService.createJWT(userInfo, Optional.ofNullable(validFrom).orElse(OffsetDateTime.now()), duration);
    }
}
