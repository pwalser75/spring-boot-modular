package ch.frostnova.module1.persistence;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Note Entity
 */
@Entity
@Table(name = "note")
public class NoteEntity extends BaseEntity {

    @Column(name = "text", length = 2048, nullable = false)
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
