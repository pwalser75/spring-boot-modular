package ch.frostnova.project.common.service.scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;
import java.util.UUID;

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
        String requestId = UUID.randomUUID().toString();
        TaskScope.init();
        String conversationId = TaskScope.currentConversationId();
        logger.debug("Task scope created for request: {}", conversationId);
        chain.doFilter(request, response);
        TaskScope.destroy();
        logger.debug("Task scope destroyed for request: {}", conversationId);

    }
}
