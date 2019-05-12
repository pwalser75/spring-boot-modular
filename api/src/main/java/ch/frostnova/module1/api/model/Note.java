package ch.frostnova.module1.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO for Note
 */
@ApiModel("Note")
public class Note extends BaseResource {

    @NotNull
    @NotBlank
    @Size(max = 2048)
    @ApiModelProperty(notes = "text of the note, up to 2048 characters")
    @JsonProperty("text")
    private String text;

    public Note() {
    }

    public Note(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
