package ch.frostnova.project.common.service.persistence;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Base Entity
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    @Id
    @GeneratedValue(generator = "generated-id")
    @GenericGenerator(name = "generated-id", strategy = "ch.frostnova.project.common.service.persistence.IdGenerator")
    @Column(name = "ID", unique = true, nullable = false)
    private String id;

    @CreatedDate
    @Column(name = "CREATED_ON", nullable = false)
    private OffsetDateTime createdOn;

    @CreatedBy
    @Column(name = "CREATED_BY", length = 64, nullable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "UPDATED_ON", nullable = false)
    private OffsetDateTime lastUpdatedOn;

    @LastModifiedBy
    @Column(name = "UPDATED_BY", length = 64, nullable = false)
    private String lastUpdatedBy;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public OffsetDateTime getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    public long getVersion() {
        return version;
    }

    public boolean isPersistent() {
        return id != null;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (id == null) {
            return false;
        }
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass().getName(), id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + id;
    }
}
