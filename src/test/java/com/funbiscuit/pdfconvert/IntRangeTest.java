package com.funbiscuit.pdfconvert;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntRangeTest {
    @Test
    public void createFromSingle() {
        assertEquals(List.of(4), IntRange.of("4").getValues());
        assertEquals(List.of(7), IntRange.of("7").getValues());
        assertEquals(List.of(77), IntRange.of("77").getValues());
        assertEquals(List.of(0), IntRange.of("0").getValues());
        assertEquals(List.of(-123), IntRange.of("-123").getValues());
        assertEquals(List.of(12), IntRange.of("end").getValues(12));
        assertEquals(List.of(3), IntRange.of("end").getValues(3));
        assertEquals(List.of(3), IntRange.of("end-5").getValues(8));
    }

    @Test
    public void createFromRange() {
        assertEquals(List.of(4, 5, 6, 7), IntRange.of("4:7").getValues());
        assertEquals(List.of(-10, -9, -8), IntRange.of("-10:-8").getValues());
        assertEquals(List.of(-2, -1, 0, 1), IntRange.of("-2:1").getValues());
        assertEquals(List.of(2, 3, 4), IntRange.of("2:end").getValues(4));
        assertEquals(List.of(4, 6), IntRange.of("4:2:7").getValues());
        assertEquals(List.of(4, 6, 8), IntRange.of("4:2:8").getValues());
        assertEquals(List.of(1, 4, 7), IntRange.of("1:3:end").getValues(7));
        assertEquals(List.of(1, 4), IntRange.of("1:3:end-2").getValues(7));
        assertEquals(List.of(1, 4, 7), IntRange.of("1:3:end-2").getValues(9));
        assertEquals(List.of(), IntRange.of("1:3:0").getValues());
        assertEquals(List.of(), IntRange.of("5:end").getValues(4));
    }

    @Test
    public void createFromCombination() {
        assertEquals(List.of(1, 2, 4, 5, 6), IntRange.of("1,2,4:6").getValues());
        assertEquals(List.of(2, 5, 6, 8), IntRange.of("5,6,2:3:end").getValues(8));
    }

    @Test
    public void notProvidingEndValue() {
        assertThrows(IllegalArgumentException.class, () -> IntRange.of("1:end").getValues());
        assertDoesNotThrow(() -> IntRange.of("1:5").getValues());
    }

    @Test
    public void failToCreateFromInvalidString() {
        assertThrows(IllegalArgumentException.class, () -> IntRange.of(""));
        assertThrows(IllegalArgumentException.class, () -> IntRange.of("   "));
        assertThrows(IllegalArgumentException.class, () -> IntRange.of("1 2"));
        assertThrows(IllegalArgumentException.class, () -> IntRange.of("1:"));
        assertThrows(IllegalArgumentException.class, () -> IntRange.of("1:3:"));
        assertThrows(IllegalArgumentException.class, () -> IntRange.of("1:3:4 4"));
        assertThrows(IllegalArgumentException.class, () -> IntRange.of(","));
        assertThrows(IllegalArgumentException.class, () -> IntRange.of("::"));
        assertThrows(IllegalArgumentException.class, () -> IntRange.of("::"));
        assertThrows(IllegalArgumentException.class, () -> IntRange.of("1:end+5"));
    }
}
