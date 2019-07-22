package ch.frostnova.module1.service.persistence;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.persistence.criteria.Predicate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring JPA repository for Notes
 */
public interface NoteRepository extends JpaRepository<NoteEntity, String>, JpaSpecificationExecutor<NoteEntity> {

    static Specification<NoteEntity> fulltextSearch(String queryString) {
        final List<String> tokens = SearchQueryTokenizer.tokenize(queryString)
                .stream()
                .filter(s -> s.trim().length() > 0)
                .collect(Collectors.toList());
        return (root, query, cb) -> {
            if (tokens.isEmpty()) {
                return cb.disjunction();
            }
            List<Predicate> predicates = new LinkedList<>();
            for (String token : tokens) {
                predicates.add(cb.like(cb.lower(root.get("text")), NoteRepository.contains(token)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    static String contains(String s) {
        return "%" + NoteRepository.escapeWildcards(s).toLowerCase() + "%";
    }

    static String escapeWildcards(String s) {
        return s.replace("%", "%%").replace("_", "__");
    }
}
