package dart.henson.plugin;

import org.junit.Test;

import java.util.List;

import dart.henson.plugin.Combinator.Dimension;
import dart.henson.plugin.Combinator.Tuple;

import static dart.henson.plugin.DimensionListTypeSafeMatcher.isSame;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CombinatorTest {

    @Test
    public void testCombine_when_oneElement() {
        //GIVEN
        List<String> elements = asList("red");

        //WHEN
        List<Dimension<String>> result = new Combinator<String>().combine(elements);

        //TEST
        assertThat(result.size(), is(2));
        assertThat(result, isSame(asList(
                new Dimension<>(new Tuple<String>()),
                new Dimension<>(new Tuple<String>("red"))
        )));
    }

    @Test
    public void testCombine_when_2Elements() {
        //GIVEN
        List<String> elements = asList("small", "red");

        //WHEN
        List<Dimension<String>> result = new Combinator<String>().combine(elements);

        //TEST
        assertThat(result.size(), is(3));
        assertThat(result, isSame(asList(
                new Dimension<>(new Tuple<String>()),
                new Dimension<>(new Tuple<>("red"), new Tuple<>("small")),
                new Dimension<>(new Tuple<>("red", "small"))
        )));
    }

    @Test
    public void testCombine_when_3Elements() {
        //GIVEN
        List<String> elements = asList("small", "red", "debug");

        //WHEN
        List<Dimension<String>> result = new Combinator<String>().combine(elements);

        //TEST
        assertThat(result.size(), is(4));
        assertThat(result, isSame(asList(
                new Dimension<>(new Tuple<String>()),
                new Dimension<>(new Tuple<>("red"), new Tuple<>("small"), new Tuple<>("debug")),
                new Dimension<>(new Tuple<>("red", "small"), new Tuple<>("red", "debug"), new Tuple<>("small", "debug")),
                new Dimension<>(new Tuple<>("red", "small", "debug"))
        )));
    }
}