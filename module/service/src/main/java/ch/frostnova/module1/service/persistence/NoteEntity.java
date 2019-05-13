package ch.frostnova.module1.service.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

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
