package ch.frostnova.common.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.PastOrPresent;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

import static io.swagger.annotations.ApiModelProperty.AccessMode.READ_ONLY;

@JsonPropertyOrder({"id", "createdOn", "updatedOn"})
public class BaseResource<ID> implements Serializable {

    private final String EXAMPLE_ID = "12345";
    private final String EXAMPLE_OFFSET_DATE_TIME = "2019-08-07T16:54:32+01:00";

    @ApiModelProperty(name = "id", notes = "identifier (generated)", accessMode = READ_ONLY, example = EXAMPLE_ID)
    @JsonProperty("id")
    private ID id;

    @ApiModelProperty(name = "created", position = 1, notes = "creation date (generated)", accessMode = READ_ONLY, example = EXAMPLE_OFFSET_DATE_TIME)
    @JsonProperty("created")
    @PastOrPresent
    private OffsetDateTime created;

    @ApiModelProperty(name = "updated", position = 2, notes = "last modification date (generated)", accessMode = READ_ONLY, example = EXAMPLE_OFFSET_DATE_TIME)
    @JsonProperty("updated")
    @PastOrPresent
    private OffsetDateTime updated;

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }

    /**
     * Two resource instances are considered equal when they have the <b>same exact type</b> and have the <b>same id</b>,
     * unless that id is null (new resource), in which case they are considered equal when they are the <b>same instance</b>.
     *
     * @param o other object, may be null
     * @return equals
     */
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
        BaseResource that = (BaseResource) o;
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
