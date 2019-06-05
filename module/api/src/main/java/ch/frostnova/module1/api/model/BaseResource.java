package ch.frostnova.module1.api.model;

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

    protected final String EXAMPLE_ID = "12345";
    protected final String EXAMPLE_OFFSET_DATE_TIME = "2019-08-07T16:54:32+01:00";

    @ApiModelProperty(position = 0, notes = "identifier (generated)", accessMode = READ_ONLY, example = EXAMPLE_ID)
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty(position = 1, notes = "creation date (generated)", accessMode = READ_ONLY, example = EXAMPLE_OFFSET_DATE_TIME)
    @JsonProperty("createdOn")
    @PastOrPresent
    private OffsetDateTime createdOn;

    @ApiModelProperty(position = 2, notes = "last modification date (generated)", accessMode = READ_ONLY, example = EXAMPLE_OFFSET_DATE_TIME)
    @JsonProperty("updatedOn")
    @PastOrPresent
    private OffsetDateTime updatedOn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public OffsetDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(OffsetDateTime updatedOn) {
        this.updatedOn = updatedOn;
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
