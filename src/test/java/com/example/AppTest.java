package com.example;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AppTest {
    App calculator = new App();

    @Test
    public void testAddition() {
        assertEquals(5, calculator.add(2, 3));
    }

    @Test
    public void testSubtraction() {
        assertEquals(2, calculator.subtract(5, 3));
    }

    @Test
    public void testMultiplication() {
        assertEquals(10, calculator.multiply(2, 5));
    }

    @Test
    public void testDivision() {
        assertEquals(3, calculator.divide(6, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDivisionByZero() {
        calculator.divide(5, 0);
    }
}
