package ch.frostnova.module1.service.impl;

import ch.frostnova.common.api.exception.ResourceNotFoundException;
import ch.frostnova.common.service.scope.TaskScope;
import ch.frostnova.module1.api.model.Note;
import ch.frostnova.module1.api.service.NoteService;
import ch.frostnova.module1.service.persistence.NoteEntity;
import ch.frostnova.module1.service.persistence.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the NoteService
 */
@Service
@Scope(value = TaskScope.NAME, proxyMode = ScopedProxyMode.INTERFACES)
@EnableTransactionManagement
@Transactional
@Validated
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Note get(String id) {
        return convert(load(id));
    }

    private NoteEntity load(String id) {
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
        return repository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
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

    @Override
    @Transactional(readOnly = true)
    public List<Note> find(@NotBlank String searchQuery) {
        // return max. 10 matches, ordered by latest created
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "createdOn");
        Specification<NoteEntity> fulltextSearch = NoteRepository.fulltextSearch(searchQuery);
        return repository.findAll(fulltextSearch, pageRequest)
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }
}
