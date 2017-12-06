package dart.henson.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Combines the elements from multiple dimensions.
 * This class will deal with elements in a very abstract way,
 * the result will be used to create the appropriate source folders
 * and classpath during compilation of navigation variants.
 */

public class Combinator<T> {

    public List<Dimension<T>> combine(List<T> elements) {
        if (elements.size() == 1) {
            Tuple<T> emptyTuple = new Tuple<>();
            Dimension<T> dimension0 = new Dimension<>(emptyTuple);

            Tuple<T> tuple = new Tuple<>();
            tuple.add(elements.get(0));
            Dimension<T> dimension1 = new Dimension<T>(tuple);

            return asList(dimension0, dimension1);
        }

        T firstElement = elements.get(0);
        List<T> remainingElements = elements.subList(1, elements.size());
        List<Dimension<T>> subDimensionList = combine(remainingElements);
        List<Dimension<T>> newDimensionList = new ArrayList<>(subDimensionList.size()+1);

        Dimension<T> previousDimension = new Dimension<>();
        for (Dimension<T> subDimension : subDimensionList) {

            Dimension<T> nextDimension = new Dimension<>();
            for (Tuple<T> subTuple : subDimension) {
                previousDimension.add(copyOf(subTuple));

                Tuple<T> newTuple = copyOf(subTuple);
                newTuple.add(firstElement);
                nextDimension.add(newTuple);
            }
            newDimensionList.add(previousDimension);
            previousDimension = nextDimension;
        }
        newDimensionList.add(previousDimension);

        return newDimensionList;
    }

    public static <T> Tuple<T> copyOf(Tuple<T> tuple) {
        Tuple<T> newTuple = new Tuple<>();
        newTuple.addAll(tuple);
        return newTuple;
    }

    static class Tuple<T> extends ArrayList<T> {
        @SafeVarargs
        public Tuple(T... items) {
            for (T item : items) {
                add(item);
            }
        }
    }

    static class Dimension<T> extends ArrayList<Tuple<T>> {
        @SafeVarargs
        Dimension(Tuple<T>... tuples) {
            for (Tuple<T> tuple : tuples) {
                add(tuple);
            }
        }

    }
}


