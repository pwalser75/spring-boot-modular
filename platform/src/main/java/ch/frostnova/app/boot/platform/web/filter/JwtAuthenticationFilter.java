package ch.frostnova.app.boot.platform.web.filter;

import ch.frostnova.app.boot.platform.service.JWTVerificationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final static Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JWTVerificationService jwtVerificationService;

    public JwtAuthenticationFilter(JWTVerificationService jwtVerificationService) {
        this.jwtVerificationService = jwtVerificationService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = authenticate(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);

    }

    private Authentication authenticate(HttpServletRequest request) throws AuthenticationException {
        try {
            final String requestTokenHeader = request.getHeader("Authorization");
            if (requestTokenHeader == null) {
                // Unauthenticated request
                return null;
            }
            if (!requestTokenHeader.startsWith("Bearer ")) {
                throw new BadCredentialsException("Expected bearer token in Authorization header");
            }
            String jwt = requestTokenHeader.substring(7);
            logger.debug("JWT: {}", jwt);
            Jws<Claims> claims = jwtVerificationService.verify(jwt);
            logger.debug("Authenticated as: {}", claims.getBody());

            List<?> scopes = claims.getBody().get("scope", List.class);
            Set<SimpleGrantedAuthority> grantedAuthorities = scopes.stream()
                    .map(String::valueOf)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());

            return new PreAuthenticatedAuthenticationToken(claims.getBody().getSubject(), claims, grantedAuthorities);

        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
