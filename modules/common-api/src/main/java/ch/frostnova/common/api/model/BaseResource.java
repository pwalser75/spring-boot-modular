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
public class BaseResource implements Serializable {

    private final String EXAMPLE_ID = "12345";
    private final String EXAMPLE_OFFSET_DATE_TIME = "2019-08-07T16:54:32+01:00";

    @ApiModelProperty(notes = "identifier (generated)", accessMode = READ_ONLY, example = EXAMPLE_ID)
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(position = 1, notes = "creation date (generated)", accessMode = READ_ONLY, example = EXAMPLE_OFFSET_DATE_TIME)
    @JsonProperty("created")
    @PastOrPresent
    private OffsetDateTime created;

    @ApiModelProperty(position = 2, notes = "last modification date (generated)", accessMode = READ_ONLY, example = EXAMPLE_OFFSET_DATE_TIME)
    @JsonProperty("updated")
    @PastOrPresent
    private OffsetDateTime updated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
