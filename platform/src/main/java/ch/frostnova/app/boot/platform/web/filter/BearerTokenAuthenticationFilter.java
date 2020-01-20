package ch.frostnova.app.boot.platform.web.filter;

import ch.frostnova.app.boot.platform.model.UserInfo;
import ch.frostnova.app.boot.platform.service.TokenAuthenticator;
import io.jsonwebtoken.ExpiredJwtException;
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
import java.util.Set;
import java.util.stream.Collectors;

public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private final static Logger logger = LoggerFactory.getLogger(BearerTokenAuthenticationFilter.class);

    private final TokenAuthenticator tokenAuthenticator;

    public BearerTokenAuthenticationFilter(TokenAuthenticator tokenAuthenticator) {
        this.tokenAuthenticator = tokenAuthenticator;
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
            String token = requestTokenHeader.substring(7);

            UserInfo authenticated = tokenAuthenticator.authenticate(token);
            Set<SimpleGrantedAuthority> grantedAuthorities = authenticated.getRoles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());

            return new PreAuthenticatedAuthenticationToken(authenticated.getLogin(), null, grantedAuthorities);


        } catch (AuthenticationException ex) {
            throw ex;
        } catch (ExpiredJwtException ex) {
            throw new BadCredentialsException(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
