package ch.frostnova.app.boot.platform.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Servlet filter logging HTTP access (method, URI) with response code and execution time.<p>
 * Example output:
 * <br><pre><code>
 * 2020-12-31 12:32:16.611 - GET /info -&gt; 200 OK, 146.46 ms
 * 2020-12-31 12:32:44.769 - GET /info -&gt; 200 OK, 6.39 ms
 * 2020-12-31 12:32:44.887 - GET /health -&gt; 200 OK, 49.44 ms
 * 2020-12-31 12:32:44.992 - GET /metrics/process.uptime -&gt; 200 OK, 38.37 ms
 * 2020-12-31 12:32:45.317 - POST /api/notes -&gt; 201 CREATED, 270.38 ms
 * 2020-12-31 12:32:45.404 - GET /api/notes/1 -&gt; 200 OK, 32.93 ms
 * 2020-12-31 12:32:45.729 - GET /api/notes -&gt; 200 OK, 266.04 ms
 * 2020-12-31 12:32:45.837 - PUT /api/notes/1 -&gt; 204 NO_CONTENT, 22.62 ms
 * 2020-12-31 12:32:45.916 - GET /api/notes/1 -&gt; 200 OK, 14.32 ms
 * 2020-12-31 12:32:46.034 - DELETE /api/notes/1 -&gt; 204 NO_CONTENT, 70.01 ms
 * 2020-12-31 12:32:46.114 - GET /api/notes/1 -&gt; 404 NOT_FOUND, 15.62 ms
 * </code></pre>
 */
@Component
@Order(1)
public class AccessLogFilter implements Filter {

    private final static double NS_TO_MS_FACTOR = 1d / 1000000;

    private final static Logger logger = LoggerFactory.getLogger(AccessLogFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        long startTime = System.nanoTime();

        chain.doFilter(request, response);

        long durationNs = System.nanoTime() - startTime;

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {

            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            String method = httpServletRequest.getMethod();
            String uri = httpServletRequest.getRequestURI();
            int responseStatusCode = httpServletResponse.getStatus();
            String responseStatus = Optional.ofNullable(HttpStatus.resolve(responseStatusCode)).map(HttpStatus::name).orElse("unknown");

            AccessLogFilter.logger.info("{} {} -> {} {}, {} ms", method, uri, responseStatusCode, responseStatus, AccessLogFilter.formatDuration(durationNs));
        }
    }

    private static String formatDuration(long durationNs) {
        return BigDecimal.valueOf(durationNs * AccessLogFilter.NS_TO_MS_FACTOR).setScale(2, RoundingMode.HALF_UP).toString();
    }

}
