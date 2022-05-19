package ch.frostnova.spring.boot.platform.config;

import ch.frostnova.spring.boot.platform.service.TokenAuthenticator;
import ch.frostnova.spring.boot.platform.web.filter.BearerTokenAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring security config
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private TokenAuthenticator tokenAuthenticator;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.csrf().disable()
                .authorizeRequests()
                .antMatchers("/login/**",
                        "/info",
                        "/health",
                        "/metrics/**",
                        "/prometheus/**"
                ).permitAll()
                .antMatchers("/v3/api-docs/**",
                        "/api-docs/**",
                        "/swagger-ui/**"
                ).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new BearerTokenAuthenticationFilter(tokenAuthenticator, objectMapper), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    }

    @Override
    protected void configure(AuthenticationManagerBuilder authManager) {
        // prevent autoconfiguration
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        // prevent autoconfiguration
        return super.authenticationManagerBean();
    }
}
