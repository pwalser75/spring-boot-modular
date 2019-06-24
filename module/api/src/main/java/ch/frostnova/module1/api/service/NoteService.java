package ch.frostnova.module1.api.service;

import ch.frostnova.module1.api.model.Note;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Note service contract
 */
public interface NoteService {

    Note get(String id);

    Note save(@NotNull @Valid Note note);

    List<Note> list();

    void delete(String id);

}
