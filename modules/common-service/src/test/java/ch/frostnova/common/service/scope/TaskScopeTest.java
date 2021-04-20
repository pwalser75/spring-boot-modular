package ch.frostnova.common.service.scope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TaskScopeConfig.class, TaskScopedComponent.class})
public class TaskScopeTest {

    @Autowired
    private TaskScopedComponent taskScopedComponent;

    @BeforeEach
    @AfterEach
    public void prepareScopeInactive() {
        if (TaskScope.isActive()) {
            TaskScope.destroy();
        }
        assertThat(TaskScope.isActive()).isFalse();
    }

    @Test
    public void testScopeNotActive() {
        assertThat(TaskScope.isActive()).isFalse();
        assertThat(TaskScope.currentConversationId()).isNull();
        assertThatThrownBy(() -> taskScopedComponent.getUuid()).isInstanceOf(BeanCreationException.class);
    }

    @Test
    public void testScopeActive() {
        TaskScope.init();
        try {
            assertThat(TaskScope.isActive()).isTrue();
            assertThat(TaskScope.currentConversationId()).isNotNull();

            assertThat(taskScopedComponent).isNotNull();
            assertThat(taskScopedComponent.getUuid()).isNotNull();

            System.out.println(taskScopedComponent.getUuid());
        } finally {
            TaskScope.destroy();
        }
    }

    @Test
    public void testScopeDoubleInit() {
        TaskScope.init();
        assertThatThrownBy(() -> TaskScope.init()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testDestroyNotActive() {
        assertThat(TaskScope.isActive()).isFalse();
        assertThatThrownBy(() -> TaskScope.destroy()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testScopeComponentLifecycle() {
        assertThat(TaskScope.isActive()).isFalse();
        TaskScope.init();
        assertThat(TaskScope.isActive()).isTrue();

        AtomicInteger callbackCount = new AtomicInteger();
        try {
            assertThat(taskScopedComponent.isPostConstructed()).isTrue();
            taskScopedComponent.setPreDestroyCallback(() -> callbackCount.incrementAndGet());
            assertThat(callbackCount.get()).isEqualTo(0);

        } finally {
            assertThat(TaskScope.isActive()).isTrue();
            TaskScope.destroy();
            assertThat(TaskScope.isActive()).isFalse();
            assertThat(callbackCount.get()).isEqualTo(1);
        }
    }
}
