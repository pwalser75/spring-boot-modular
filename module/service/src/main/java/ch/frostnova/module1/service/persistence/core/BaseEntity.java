package ch.frostnova.module1.service.persistence.core;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Base Entity
 */
@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue(generator = "generated-id")
    @GenericGenerator(name = "generated-id", strategy = "ch.frostnova.module1.service.persistence.core.IdGenerator")
    @Column(name = "ID", unique = true, nullable = false)
    private String id;

    @CreatedDate
    @Column(name = "CREATED_ON", nullable = false)
    private OffsetDateTime createdOn;

    @Column(name = "UPDATED_ON", nullable = false)
    private OffsetDateTime lastUpdatedOn;

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

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public OffsetDateTime getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(OffsetDateTime lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public boolean isPersistent() {
        return id != null;
    }

    @PrePersist
    private void createAudit() {
        lastUpdatedOn = createdOn = now();
    }

    @PreUpdate
    private void updateAuditDates() {
        lastUpdatedOn = now();
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
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
