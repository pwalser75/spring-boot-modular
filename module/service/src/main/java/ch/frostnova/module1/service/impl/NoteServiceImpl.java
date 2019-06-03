package ch.frostnova.module1.service.impl;

import ch.frostnova.module1.api.exception.ResourceNotFoundException;
import ch.frostnova.module1.api.model.Note;
import ch.frostnova.module1.api.service.NoteService;
import ch.frostnova.module1.service.persistence.NoteEntity;
import ch.frostnova.module1.service.persistence.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation of the NoteService
 */
@Service
@EnableTransactionManagement
@Transactional
@Validated
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Note get(long id) {
        return convert(load(id));
    }

    private NoteEntity load(long id) {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Note save(Note note) {

        NoteEntity entity = Optional.ofNullable(note.getId()).map(this::load).orElseGet(NoteEntity::new);
        entity = update(entity, note);
        return convert(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Note> list() {
        Stream<NoteEntity> stream = StreamSupport.stream(repository.findAll().spliterator(), false);
        return stream.map(this::convert).collect(Collectors.toList());
    }

    @Override
    public void delete(long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        }
    }

    private Note convert(NoteEntity entity) {
        if (entity == null) {
            return null;
        }
        Note dto = new Note();
        dto.setId(entity.getId());
        dto.setText(entity.getText());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedOn(entity.getLastUpdatedOn());
        return dto;
    }

    private NoteEntity update(NoteEntity entity, Note dto) {
        if (dto == null) {
            return null;
        }
        entity.setId(dto.getId());
        entity.setText(dto.getText());
        return entity;
    }
}
