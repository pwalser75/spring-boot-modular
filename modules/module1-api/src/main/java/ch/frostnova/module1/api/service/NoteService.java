package ch.frostnova.module1.api.service;

import ch.frostnova.common.api.exception.ResourceNotFoundException;
import ch.frostnova.module1.api.model.Note;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Note service contract
 */
public interface NoteService {

    /**
     * Get a note by id, or throw a {@link ResourceNotFoundException} when no such note exists.
     *
     * @param id id, required
     * @return note
     */
    Note get(String id);

    /**
     * Save (create or update) a note. When the note id is set, and no such note exists for update,
     * a {@link ResourceNotFoundException} is thrown.
     *
     * @param note note to create or update
     * @return saved note
     */
    Note save(@NotNull @Valid Note note);

    /**
     * List all notes.
     *
     * @return notes
     */
    List<Note> list();

    /**
     * Delete a note by id. or does nothing when no such note exists (considered already deleted).
     *
     * @param id id, required
     */
    void delete(String id);

    /**
     * Find all notes matching the given search query.
     *
     * @param searchQuery search query
     * @return list of matching notes
     */
    List<Note> find(@NotBlank String searchQuery);

}
