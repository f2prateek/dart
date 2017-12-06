package dart.henson.plugin;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;

import dart.henson.plugin.Combinator.Dimension;
import dart.henson.plugin.Combinator.Tuple;

import static java.lang.String.format;

public class DimensionListTypeSafeMatcher extends TypeSafeMatcher<List<Dimension<String>>> {
    private final List<Dimension<String>> expected;
    String mismatch;

    public DimensionListTypeSafeMatcher(List<Dimension<String>> expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(List<Dimension<String>> actual) {
        mismatch = assertMatch(expected, actual);
        return mismatch == null;
    }

    @Override
    protected void describeMismatchSafely(List<Dimension<String>> actual, Description mismatchDescription) {
        mismatchDescription
                .appendText("The actual result: ")
                .appendValue(actual)
                .appendText("didn't match the expected result: ")
                .appendValue(expected)
                .appendText("The mistmatch comes from:")
                .appendValue(mismatch);
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(expected);
    }

    public static TypeSafeMatcher<List<Dimension<String>>> isSame(List<Dimension<String>> expected) {
        return new DimensionListTypeSafeMatcher(expected);
    }

    private static String assertMatch(List<Dimension<String>> expected, List<Dimension<String>> actual) {
        if (expected.size() != actual.size()) {
            return format("Expected %s has a different size than actual=%s\n", expected, actual);
        }

        for (int indexDimension = 0; indexDimension < expected.size(); indexDimension++) {
            String mismatch = assertMatch(expected.get(indexDimension), actual.get(indexDimension));
            if (mismatch != null) {
                format("Dimensions are different: expected=%s actual=%s\n", expected, actual);
            }
        }
        return null;
    }

    private static String assertMatch(Dimension<String> expected, Dimension<String> actual) {
        if (expected.size() != actual.size()) {
            return format("Expected %s has a different size than actual=%s\n", expected, actual);
        }

        StringBuilder mismatchSet = new StringBuilder();
        for (Tuple<String> expectedTuple : expected) {
            boolean matchTuple = true;
            for (Tuple<String> actualTuple : actual) {
                String mismatch = assertMatch(expectedTuple, actualTuple);
                if (mismatch != null) {
                    matchTuple = false;
                    mismatchSet.append(mismatch);
                }
                matchTuple = matchTuple || (mismatch != null);
            }
            if (!matchTuple) {
                return mismatchSet.toString();
            }
        }

        for (Tuple<String> actualTuple : actual) {
            boolean matchTuple = true;
            for (Tuple<String> expectedTuple : expected) {
                String mismatch = assertMatch(expectedTuple, actualTuple);
                if (mismatch != null) {
                    matchTuple = false;
                    mismatchSet.append(mismatch);
                }
                matchTuple = matchTuple || (mismatch != null);
            }
            if (!matchTuple) {
                return mismatchSet.toString();
            }
        }

        return null;
    }

    private static String assertMatch(Tuple<String> expected, Tuple<String> actual) {
        if (expected.size() != actual.size()) {
            return format("Expected %s has a different size than actual=%s\n", expected, actual);
        }

        for (String actualString : actual) {
            if (!expected.contains(actualString)) {
                return format("Expected %s doesn't contain actual=%s\n", expected, actualString);
            }
        }

        for (String expectedString : expected) {
            if (!actual.contains(expectedString)) {
                return format("Actual %s doesn't contain expected=%s\n", actual, expectedString);
            }
        }


        return null;
    }
}
