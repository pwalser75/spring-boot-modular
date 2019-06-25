package ch.frostnova.app.boot.platform.web.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * App Info controller
 */
@RestController
@RequestMapping(path = "app-info")
@CrossOrigin(origins = "*",
        allowedHeaders = "origin, content-type, accept, authorization",
        allowCredentials = "true",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.HEAD},
        maxAge = 1209600)
public class AppInfoController {

    @Autowired
    private MetricsEndpoint metricsEndpoint;

    @Autowired
    private InfoEndpoint infoEndpoint;

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok")
    })
    public AppInfo info() {

        AppInfo appInfo = new AppInfo();

        Map<String, Object> info = infoEndpoint.info();

        Object app = info.get("app");
        if (app instanceof Map) {
            Map appMap = (Map) app;
            Function<Object, String> getInfo = key -> appMap.getOrDefault(key, "unknown").toString();

            appInfo.setName(getInfo.apply("name"));
            appInfo.setDescription(getInfo.apply("description"));
            appInfo.setVersion(getInfo.apply("version"));
        }

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

        return appInfo;
    }
}
