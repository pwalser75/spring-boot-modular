package ch.frostnova.spring.boot.platform.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Tomcat settings: enforce HTTPS, redirect from HTTP port to HTTPS.
 */
@Configuration
public class TomcatConfig {

    @Value("${http.server.port:#{null}}")
    private Integer serverPortHttp;

    @Value("${server.port}")
    private int serverPortHttps;

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatFactory();
        createHttpConnector().ifPresent(tomcat::addAdditionalTomcatConnectors);
        return tomcat;
    }

    private Optional<Connector> createHttpConnector() {

        return Optional.ofNullable(serverPortHttp).map(httpPort -> {
            Connector connector = new Connector(Http11NioProtocol.class.getName());
            connector.setScheme("http");
            connector.setSecure(false);
            connector.setPort(serverPortHttp);
            connector.setRedirectPort(serverPortHttps);
            return connector;
        });
    }

    static class TomcatFactory extends TomcatServletWebServerFactory {
        @Override
        protected void postProcessContext(Context context) {
            SecurityConstraint securityConstraint = new SecurityConstraint();
            securityConstraint.setUserConstraint("CONFIDENTIAL");
            SecurityCollection collection = new SecurityCollection();
            collection.addPattern("/*");
            securityConstraint.addCollection(collection);
            context.addConstraint(securityConstraint);
        }
    }
}
