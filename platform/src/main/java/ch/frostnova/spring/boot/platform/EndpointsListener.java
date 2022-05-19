package ch.frostnova.spring.boot.platform;

import org.hibernate.resource.beans.container.internal.NoSuchBeanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class EndpointsListener implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        try {
            RequestMappingHandlerMapping mappingHandlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
            logger.info("ENDPOINTS:");

            mappingHandlerMapping.getHandlerMethods().entrySet().stream()
                    .flatMap(it -> endpointInfos(it.getKey(), it.getValue()).stream())
                    .sorted(Comparator.comparing(EndpointInfo::getPath).thenComparing(endpoint -> methodOrder(endpoint.getMethod())))
                    .forEach(endpointInfo ->
                            logger.info("{} {}: {}", endpointInfo.getMethod(), endpointInfo.getPath(), endpointInfo.getHandlerMethod()));
        } catch (NoSuchBeanDefinitionException ex) {
            // No Endpoint information available - usually during tests
        }
    }

    private List<EndpointInfo> endpointInfos(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {

        return requestMappingInfo.getMethodsCondition().getMethods().stream().flatMap(method ->
                requestMappingInfo.getPathPatternsCondition().getPatterns().stream().map(pathPattern ->
                        new EndpointInfo(method.name(), pathPattern.getPatternString(), handlerMethod.toString())
                )
        ).collect(toList());
    }

    private static class EndpointInfo {

        private final String method;
        private final String path;
        private final String handlerMethod;

        public EndpointInfo(String method, String path, String handlerMethod) {
            this.method = method;
            this.path = path;
            this.handlerMethod = handlerMethod;
        }

        public String getMethod() {
            return method;
        }

        public String getPath() {
            return path;
        }

        public String getHandlerMethod() {
            return handlerMethod;
        }
    }

    private int methodOrder(String method) {
        if (method.equalsIgnoreCase("GET")) return 1;
        if (method.equalsIgnoreCase("POST")) return 2;
        if (method.equalsIgnoreCase("PUT")) return 3;
        if (method.equalsIgnoreCase("DELETE")) return 4;
        return 5;
    }
}