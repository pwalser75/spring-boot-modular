package ch.frostnova.common.service.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.UUID;

@Component
@Scope(value = TaskScope.NAME, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TaskScopedComponent {

    /**
     * Unique id to distinguish different instances
     */
    private final String uuid = UUID.randomUUID().toString();

    private boolean postConstructed = false;
    private Runnable preDestroyCallback;

    public String getUuid() {
        return uuid;
    }

    @PostConstruct
    public void setup() {
        // prove post-construction by setting the postConstructed flag
        postConstructed = true;
    }

    @PreDestroy
    public void cleanup() {
        // prove pre-destroy by calling the callback
        if (preDestroyCallback != null) {
            preDestroyCallback.run();
        }
    }

    public boolean isPostConstructed() {
        return postConstructed;
    }

    public void setPreDestroyCallback(Runnable preDestroyCallback) {
        this.preDestroyCallback = preDestroyCallback;
    }
}
