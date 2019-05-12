package ch.frostnova.module1.web.controller;

import ch.frostnova.module1.api.model.Note;
import ch.frostnova.module1.api.service.NoteService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Notes web service endpoint <p>
 * Full local path: <a href="https://localhost/api/notes">https://localhost/api/notes</a>
 */
@RestController
@RequestMapping(path = "api/notes")
@CrossOrigin(origins = "*",
        allowedHeaders = "origin, content-type, accept, authorization",
        allowCredentials = "true",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.HEAD},
        maxAge = 1209600)
@Api(value = "Notes resource", produces = "application/json", description = "Endpoints for reading/writing notes")
public class NotesController {

    @Autowired
    private NoteService noteService;

    /**
     * List notes
     *
     * @return list of notes (never null)
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Lists all notes", response = Note.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok")
    })
    public List<Note> list() {
        return noteService.list();
    }

    /**
     * Get a record by id. If the record was not found, a NoSuchElementException will be thrown (resulting in a 404 NOT FOUND).
     *
     * @param id id of the record
     * @return record
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Get a specific note", response = Note.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok"),
            @ApiResponse(code = 404, message = "not found")
    })
    public Note get(@ApiParam(value = "ID of the note to fetch", required = true) @PathVariable("id") long id) {
        Note result = noteService.get(id);
        if (result != null) {
            return result;
        }
        throw new NoSuchElementException();
    }

    /**
     * Create a new record (or updates an existing record, when the id is set).
     *
     * @param note record to create
     * @return created record
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new note", response = Note.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok"),
            @ApiResponse(code = 400, message = "bad request")
    })
    public Note create(@RequestBody Note note) {
        note.setId(null);
        return noteService.save(note);
    }

    /**
     * Update a record
     *
     * @param id   id of the record to update
     * @param note new data to set
     */
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Update an existing note", response = Note.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "bad request"),
            @ApiResponse(code = 404, message = "not found")
    })
    public void update(@ApiParam(value = "ID of the note to update", required = true) @PathVariable("id") long id, @RequestBody Note note) {
        note.setId(id);
        noteService.save(note);
    }

    /**
     * Delete a record
     *
     * @param id id of the record
     */
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Delete a note", response = Note.class)
    public void delete(@ApiParam(value = "ID of the note to delete", required = true) @PathVariable("id") long id) {
        noteService.delete(id);
    }
}
