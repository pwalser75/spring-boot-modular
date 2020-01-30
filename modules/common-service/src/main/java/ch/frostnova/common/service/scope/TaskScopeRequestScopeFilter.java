package ch.frostnova.common.service.scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

/**
 * Servlet filter which activates the task scope on request level.
 *
 * @author pwalser
 * @since 2019-11-03
 */
@Component
@Order(1)
public class TaskScopeRequestScopeFilter implements Filter {


    private final static Logger logger = LoggerFactory.getLogger(TaskScopeRequestScopeFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
            IOException, ServletException {

        if (TaskScope.isActive()) {
            chain.doFilter(request, response);
            return;
        }
        TaskScope.init();
        final String conversationId = TaskScope.currentConversationId();
        try {
            logger.debug("Task scope created for request: {}", conversationId);
            chain.doFilter(request, response);
        } finally {
            TaskScope.destroy();
            logger.debug("Task scope destroyed for request: {}", conversationId);
        }

    }
}
