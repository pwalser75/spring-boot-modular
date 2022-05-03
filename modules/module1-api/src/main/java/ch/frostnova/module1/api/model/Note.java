package ch.frostnova.module1.api.model;

import ch.frostnova.common.api.model.BaseResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO for Note
 */
public class Note extends BaseResource<String> {

    @NotBlank
    @Size(max = 2048)
    @Schema(name = "text", description = "text of the note, up to 2048 characters", example = "Si vis pacem para bellum", required = true)
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
