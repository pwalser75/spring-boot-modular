package ch.frostnova.app.boot.platform.web.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
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
        tomcat.addConnectorCustomizers(http2ProtocolCustomizer());
        return tomcat;
    }

    private static TomcatConnectorCustomizer http2ProtocolCustomizer() {
        // TODO: Startup error:
        // The upgrade handler [org.apache.coyote.http2.Http2Protocol] for [h2] only supports upgrade via ALPN but has been configured for the ["https-jsse-nio-8443"] connector that does not support ALPN.
        return connector -> connector.addUpgradeProtocol(new Http2Protocol());
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
}
