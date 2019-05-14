package ch.frostnova.module1.api.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.validation.*;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tests for Note model
 */
public class NoteTest {

    @Before
    public void init() {
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
        note.setCreationDate(ZonedDateTime.now().plusMinutes(1));
        note.setModificationDate(ZonedDateTime.now().plusDays(1));
        validate(note, "creationDate", "modificationDate");
    }

    @Test
    public void testSerialize() throws Exception {

        Note note = new Note();
        note.setText("Hello World");
        note.setId(12345L);
        note.setCreationDate(ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusDays(1));
        note.setModificationDate(ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusHours(1));

        String json = NoteTest.objectMapper().writeValueAsString(note);
        System.out.println(json);

        Note restored = NoteTest.objectMapper().readValue(json, Note.class);
        Assert.assertEquals(note.getText(), restored.getText());
        Assert.assertEquals(note.getId(), restored.getId());
        Assert.assertTrue(note.getCreationDate().isEqual(restored.getCreationDate()));
        Assert.assertTrue(note.getModificationDate().isEqual(restored.getModificationDate()));
    }

    private void validate(Object obj, String... expectedErrorPropertyPaths) {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> errors = validator.validate(obj);
        Set<String> errorProperties = errors.stream().map(ConstraintViolation::getPropertyPath).map(Path::toString).collect(Collectors.toSet());

        errors.forEach(e -> System.out.println("- " + e.getPropertyPath() + ": " + e.getMessage()));

        Stream.of(expectedErrorPropertyPaths).forEach(property ->
                Assert.assertTrue("expected validation error in " + property, errorProperties.contains(property)));
        Assert.assertEquals(expectedErrorPropertyPaths.length, errorProperties.size());
    }

    private static ObjectMapper objectMapper() {
        return new ObjectMapper()
                .setAnnotationIntrospector(new JacksonAnnotationIntrospector())
                .registerModule(new JavaTimeModule())
                .setDateFormat(new StdDateFormat())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.CLOSE_CLOSEABLE.WRITE_DATES_AS_TIMESTAMPS);
    }
}
