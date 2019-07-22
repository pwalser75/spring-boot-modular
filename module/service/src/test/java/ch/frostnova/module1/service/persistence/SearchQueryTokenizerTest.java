package ch.frostnova.module1.service.persistence;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link SearchQueryTokenizer}
 *
 * @author pwalser
 */
public class SearchQueryTokenizerTest {

    @Test
    public void shouldParseNull() {

        List<String> tokens = SearchQueryTokenizer.tokenize(null);

        assertThat(tokens, is(notNullValue()));
        assertThat(tokens, empty());
    }

    @Test
    public void shouldParseEmpty() {

        List<String> tokens = SearchQueryTokenizer.tokenize("");

        assertThat(tokens, is(notNullValue()));
        assertThat(tokens, empty());
    }

    @Test
    public void shouldParseWhitespacesOnly() {

        List<String> tokens = SearchQueryTokenizer.tokenize("   ");

        assertThat(tokens, is(notNullValue()));
        assertThat(tokens, empty());
    }

    @Test
    public void shouldParseSeparatorsOnly() {

        List<String> tokens = SearchQueryTokenizer.tokenize(" ,, , ;  ");

        assertThat(tokens, is(notNullValue()));
        assertThat(tokens, empty());
    }

    @Test
    public void shouldTokenizeSpacesSeparated() {

        List<String> tokens = SearchQueryTokenizer.tokenize("Foo #Test 123   ");

        assertThat(tokens, is(notNullValue()));
        assertThat(tokens, contains("Foo", "#Test", "123"));
    }

    @Test
    public void shouldTokenizeCommaSeparated() {

        List<String> tokens = SearchQueryTokenizer.tokenize("  Foo, #Test,123  ,");

        assertThat(tokens, is(notNullValue()));
        assertThat(tokens, contains("Foo", "#Test", "123"));
    }

    @Test
    public void shouldTokenizeSemicolonSeparated() {

        List<String> tokens = SearchQueryTokenizer.tokenize("  Foo; #Test;123  ;");

        assertThat(tokens, is(notNullValue()));
        assertThat(tokens, contains("Foo", "#Test", "123"));
    }

    @Test
    public void shouldTokenizeMixedSeparated() {

        List<String> tokens = SearchQueryTokenizer.tokenize("  Foo  #Test,;,123  ;456!");

        assertThat(tokens, is(notNullValue()));
        assertThat(tokens, contains("Foo", "#Test", "123", "456!"));
    }

    @Test
    public void shouldAllowSingleQuotes() {

        List<String> tokens = SearchQueryTokenizer.tokenize("\'New York\', US");

        assertThat(tokens, is(notNullValue()));
        assertThat(tokens, contains("New York", "US"));
    }

    @Test
    public void shouldAllowDoubleQuotes() {

        List<String> tokens = SearchQueryTokenizer.tokenize("\"New York\", US");

        assertThat(tokens, is(notNullValue()));
        assertThat(tokens, contains("New York", "US"));
    }

    @Test
    public void shouldAllowNestedQuotes() {

        List<String> tokens = SearchQueryTokenizer.tokenize("\"What's up\", Doc");

        assertThat(tokens, is(notNullValue()));
        assertThat(tokens, contains("What's up", "Doc"));
    }

    @Test
    public void shouldIgnoreEmptyQuotedValues() {

        List<String> tokens = SearchQueryTokenizer.tokenize("\"\", \" \"");

        assertThat(tokens, is(notNullValue()));
        assertThat(tokens, contains(" "));
    }

    @Test
    public void shouldBeLenientToUnterminatedQuoting() {

        List<String> tokens = SearchQueryTokenizer.tokenize("\"What's up, Doc");

        assertThat(tokens, is(notNullValue()));
        assertThat(tokens, contains("What's up, Doc"));
    }
}
