package ch.frostnova.project.common.service.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.UUID;

@Component
@Scope(value = TaskScope.NAME, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TaskScopedComponent {

    private final String uuid = UUID.randomUUID().toString();
    private boolean postConstructed = false;
    private Runnable preDestroyCallback;

    public String getUuid() {
        return uuid;
    }

    @PostConstruct
    public void setup() {
        System.out.println("@PostConstruct: " + uuid);
        postConstructed = true;
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("@PreDestroy: " + uuid);
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
