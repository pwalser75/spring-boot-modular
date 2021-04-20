package ch.frostnova.module1.api.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Note model
 */
public class NoteTest {

    @BeforeAll
    public static void init() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    public void testValidate() {

        Note note = new Note();
        note.setText("Hello World");

        // all ok
        validate(note);

        // text not empty
        note.setText(null);
        validate(note, "text");

        // text not blank
        note.setText("");
        validate(note, "text");

        // text length <= 2048 chars
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 2048; i++) {
            builder.append('#');
        }
        note.setText(builder.toString());
        validate(note);
        builder.append('#');
        note.setText(builder.toString());
        validate(note, "text");

        // dates not in the future
        note.setText("Hello World");
        note.setCreated(OffsetDateTime.now().plusMinutes(1));
        note.setUpdated(OffsetDateTime.now().plusDays(1));
        validate(note, "created", "updated");
    }

    @Test
    public void testSerialize() throws Exception {

        Note note = new Note();
        note.setText("Hello World");
        note.setId("ABC12345");
        note.setCreated(OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusDays(1));
        note.setUpdated(OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusHours(1));

        String json = NoteTest.objectMapper().writeValueAsString(note);
        System.out.println(json);

        Note restored = NoteTest.objectMapper().readValue(json, Note.class);
        assertThat(restored.getText()).isEqualTo(note.getText());
        assertThat(restored.getId()).isEqualTo(note.getId());
        assertThat(note.getCreated().isEqual(restored.getCreated())).isTrue();
        assertThat(note.getUpdated().isEqual(restored.getUpdated())).isTrue();
    }

    private static void validate(Object obj, String... expectedErrorPropertyPaths) {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> errors = validator.validate(obj);
        Set<String> errorProperties = errors.stream().map(ConstraintViolation::getPropertyPath).map(Path::toString).collect(Collectors.toSet());

        errors.forEach(e -> System.out.println("- " + e.getPropertyPath() + ": " + e.getMessage()));

        assertThat(errorProperties).containsExactlyInAnyOrder(expectedErrorPropertyPaths);
    }

    private static ObjectMapper objectMapper() {
        return new ObjectMapper()
                .setAnnotationIntrospector(new JacksonAnnotationIntrospector())
                .registerModule(new JavaTimeModule())
                .setDateFormat(new StdDateFormat())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
