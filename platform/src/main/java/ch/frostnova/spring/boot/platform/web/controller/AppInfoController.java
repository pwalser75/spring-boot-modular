package ch.frostnova.spring.boot.platform.web.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * App Info controller
 */
@RestController
@ConditionalOnProperty("info.app.name")
@RequestMapping(path = "info")
@CrossOrigin(origins = "*",
        allowedHeaders = "origin, content-type, accept, authorization",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.HEAD},
        maxAge = 1209600)
public class AppInfoController {

    @Autowired(required = false)
    private MetricsEndpoint metricsEndpoint;

    @Value("${info.app.name}")
    private String appName;


    @Value("${info.app.description:}")
    private String appDescription;


    @Value("${info.app.version:}")
    private String appVersion;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "ok")
    public AppInfo info() {

        AppInfo appInfo = new AppInfo();
        appInfo.setName(appName);
        appInfo.setDescription(appDescription);
        appInfo.setVersion(appVersion);

        if (metricsEndpoint != null) {

            Function<String, Number> getMetricValue = key ->
                    Optional.ofNullable(metricsEndpoint.metric(key, Collections.emptyList()))
                            .map(MetricsEndpoint.MetricResponse::getMeasurements)
                            .map(Collection::stream)
                            .flatMap(Stream::findFirst)
                            .map(MetricsEndpoint.Sample::getValue)
                            .orElse(Double.NaN);

            appInfo.getCpu().setUsage(getMetricValue.apply("process.cpu.usage").doubleValue());
            appInfo.getMemory().setUsed(getMetricValue.apply("jvm.memory.used").longValue());
            appInfo.getMemory().setAllocated(getMetricValue.apply("jvm.memory.committed").longValue());
        }

        return appInfo;
    }
}
