package ch.frostnova.module1.web.controller;

import ch.frostnova.module1.api.model.Note;
import ch.frostnova.module1.api.service.NoteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
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
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.HEAD},
        maxAge = 1209600)
@Api(value = "Notes resource", produces = "application/json")
public class NotesController {

    @Autowired
    private NoteService noteService;

    /**
     * Lists all notes, or find by query (fulltext search)
     *
     * @param query optional query text (space or comma-separated tokens, single/double quoting supported
     * @return list of notes (never null)
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lists all notes, or find by query (fulltext search)", response = Note.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok")
    })
    public List<Note> list(@ApiParam(value = "optional query text (space or comma-separated tokens, single/double quoting supported")
                           @RequestParam(value = "query", required = false) @NotBlank String query) {
        if (query != null) {
            return noteService.find(query);
        }
        return noteService.list();
    }

    /**
     * Get a record by id. If the record was not found, a NoSuchElementException will be thrown (resulting in a 404 NOT FOUND).
     *
     * @param id id of the record
     * @return record
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a specific note", response = Note.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok"),
            @ApiResponse(code = 404, message = "not found")
    })
    public Note get(@ApiParam(value = "ID of the note to fetch", required = true) @PathVariable("id") String id) {
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
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new note", response = Note.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok"),
            @ApiResponse(code = 400, message = "bad request")
    })
    public Note create(@ApiParam(value = "Note to create", required = true) @RequestBody Note note) {
        note.setId(null);
        return noteService.save(note);
    }

    /**
     * Update a record
     *
     * @param id   id of the record to update
     * @param note new data to set
     */
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Update an existing note", response = Note.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "bad request"),
            @ApiResponse(code = 404, message = "not found")
    })
    public void update(@ApiParam(value = "ID of the note to update", required = true) @PathVariable("id") String id,
                       @ApiParam(value = "Note data to update", required = true) @RequestBody Note note) {
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
    public void delete(@ApiParam(value = "ID of the note to delete", required = true) @PathVariable("id") String id) {
        noteService.delete(id);
    }
}
