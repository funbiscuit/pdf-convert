package com.funbiscuit.pdfconvert;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IntRange {

    private final List<UnitRange> suppliers;

    private IntRange(String range) {
        suppliers = Arrays.stream(range.split(","))
                .map(UnitRange::new)
                .collect(Collectors.toList());
        if (suppliers.isEmpty()) {
            throw new IllegalArgumentException("Invalid range: '" + range + "'");
        }
    }

    public static IntRange of(String range) {
        return new IntRange(range);
    }

    public List<Integer> getValues() {
        // check that end is not used
        if (suppliers.stream().anyMatch(UnitRange::isEndRequired)) {
            throw new IllegalArgumentException("End value is required for range evaluation");
        }

        return getValues(0);
    }

    public List<Integer> getValues(int endValue) {
        return suppliers.stream()
                .map(r -> r.eval(endValue))
                .flatMap(List::stream)
                .sorted()
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Range from simple expression like 1 or 5 or 1:5
     */
    private static class UnitRange {
        private final Function<Integer, List<Integer>> rangeSupplier;

        /**
         * Whether this range requires valid end value when calling eval function
         */
        @Getter
        private final boolean endRequired;

        UnitRange(String str) {
            if (str.isEmpty()) {
                throw new IllegalArgumentException("Can't convert empty string to range");
            }
            str = str.toLowerCase();

            // any number, or "end", or "end-X"
            Pattern patternSingle = Pattern.compile("^ *(?:(-?\\d+)|end(-\\d+)?) *$");
            // X:Y or X:S:Y where X and S are any number and Y is any number or "end", or "end-Z"
            Pattern patternRange = Pattern.compile("^ *(-?\\d+)(:\\d+)?:(?:(-?\\d+)|end(-\\d+)?) *$");

            Matcher m = patternSingle.matcher(str);

            if (m.find()) {
                endRequired = m.group(1) == null;
                if (endRequired) {
                    int offset = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
                    rangeSupplier = e -> List.of(e + offset);
                } else {
                    int value = Integer.parseInt(m.group(1));
                    rangeSupplier = e -> List.of(value);
                }
                return;
            }

            m = patternRange.matcher(str);
            if (!m.find()) {
                throw new IllegalArgumentException("Invalid range: '" + str + "'");
            }

            int start = Integer.parseInt(m.group(1));
            int step = m.group(2) != null ? Integer.parseInt(m.group(2).substring(1)) : 1;
            int endOffset = m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;

            endRequired = m.group(3) == null;

            if (endRequired) {
                rangeSupplier = end -> IntStream
                        .iterate(start, i -> i <= end + endOffset, i -> i + step)
                        .boxed().collect(Collectors.toList());
            } else {
                int end = Integer.parseInt(m.group(3));

                rangeSupplier = e -> IntStream
                        .iterate(start, i -> i <= end, i -> i + step)
                        .boxed().collect(Collectors.toList());
            }
        }

        List<Integer> eval(int end) {
            return rangeSupplier.apply(end);
        }
    }
}
