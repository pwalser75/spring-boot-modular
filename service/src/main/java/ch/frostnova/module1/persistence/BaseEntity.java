package ch.frostnova.module1.persistence;

import ch.frostnova.module1.api.model.BaseResource;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base Entity
 */
@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "updated_on",  nullable = false)
    private LocalDateTime lastModifiedDate;

    @Version
    @Column(name = "vesion", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
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
        creationDate = lastModifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    private void updateAuditDates() {
        lastModifiedDate = LocalDateTime.now();
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
