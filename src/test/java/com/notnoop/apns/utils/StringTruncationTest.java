package com.notnoop.apns.utils;

import junit.framework.Assert;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import com.notnoop.apns.internal.Utilities;

// Test inspired by http://stackoverflow.com/questions/119328/how-do-i-truncate-a-java-string-to-fit-in-a-given-number-of-bytes-once-utf-8-enc
@RunWith(Theories.class)
public class StringTruncationTest {
    @DataPoints public static Object[][] dataPoints = {
            {"abcd", 0, 0},
            {"abcd", 1, 1},
            {"abcd", 2, 2},
            {"abcd", 3, 3},
            {"abcd", 4, 4},
            {"abcd", 5, 4},

            {"a\u0080b", 0, 0},
            {"a\u0080b", 1, 1},
            {"a\u0080b", 2, 1},
            {"a\u0080b", 3, 3},
            {"a\u0080b", 4, 4},
            {"a\u0080b", 5, 4},

            {"a\u0800b", 0, 0},
            {"a\u0800b", 1, 1},
            {"a\u0800b", 2, 1},
            {"a\u0800b", 3, 1},
            {"a\u0800b", 4, 4},
            {"a\u0800b", 5, 5},
            {"a\u0800b", 6, 5},
    };

    @DataPoints public static Object[][] surregatePairs = {
            {"\uD834\uDD1E", 0, 0},
            {"\uD834\uDD1E", 1, 0},
            {"\uD834\uDD1E", 2, 0},
            {"\uD834\uDD1E", 3, 0},
            {"\uD834\uDD1E", 4, 4},
            {"\uD834\uDD1E", 5, 4}
    };

    @Theory
    public void truncateUTF8Properly(Object[] p) {
        String str = (String)p[0];
        int maxBytes = (Integer)p[1];
        int expectedBytes = (Integer)p[2];

        String result = Utilities.truncateWhenUTF8(str, maxBytes);
        byte[] utf8 = Utilities.toUTF8Bytes(result);

        Assert.assertEquals(expectedBytes, utf8.length);
    }
}
