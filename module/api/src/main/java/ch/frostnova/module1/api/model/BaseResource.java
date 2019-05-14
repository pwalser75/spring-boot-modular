package ch.frostnova.module1.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.PastOrPresent;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

import static io.swagger.annotations.ApiModelProperty.AccessMode.READ_ONLY;

public class BaseResource implements Serializable {

    protected final String EXAMPLE_ID = "12345";
    protected final String EXAMPLE_ZONED_DATE_TIME = "2019-08-07T16:54:32+01:00";

    @ApiModelProperty(notes = "identifier (generated)", accessMode = READ_ONLY, example = EXAMPLE_ID)
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty(notes = "creation date (generated)", accessMode = READ_ONLY, example = EXAMPLE_ZONED_DATE_TIME)
    @JsonProperty("creationDate")
    @PastOrPresent
    private ZonedDateTime creationDate;

    @ApiModelProperty(notes = "last modification date (generated)", accessMode = READ_ONLY, example = EXAMPLE_ZONED_DATE_TIME)
    @JsonProperty("modificationDate")
    @PastOrPresent
    private ZonedDateTime modificationDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public ZonedDateTime getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(ZonedDateTime modificationDate) {
        this.modificationDate = modificationDate;
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
