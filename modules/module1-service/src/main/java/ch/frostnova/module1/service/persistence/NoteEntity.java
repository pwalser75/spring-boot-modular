package ch.frostnova.module1.service.persistence;

import ch.frostnova.project.common.service.persistence.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Note Entity
 */
@Entity
@Table(name = "NOTE")
public class NoteEntity extends BaseEntity {

    @Column(name = "TEXT", length = 2048, nullable = false)
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
