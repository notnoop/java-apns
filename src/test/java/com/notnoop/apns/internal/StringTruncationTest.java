/*
 * Copyright © 2010 MBTE Sweden AB.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of Mahmood Ali. nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.notnoop.apns.internal;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Marcus Better
 */
@RunWith(Parameterized.class)
public class StringTruncationTest
{
    String s;
    int max;
    int truncatedWidth;
    
    @Parameters
    public static Collection<Object[]> inputStrings()
    {
        return Arrays.asList(new Object[][] {
                {"", 0, 0}, {"", 1, 0},
                // single-byte characters
                {"abcd", 0, 0}, {"abcd", 1, 1}, {"abcd", 2, 2}, {"abcd", 3, 3}, {"abcd", 4, 4}, {"abcd", 5, 4},
                // two-byte characters
                {"a\u0080b", 0, 0}, {"a\u0080b", 1, 1}, {"a\u0080b", 2, 1}, {"a\u0080b", 3, 2}, {"a\u0080b", 4, 3}, {"a\u0080b", 5, 3},
                // three-byte characters
                {"a\u0800b", 0, 0}, {"a\u0800b", 1, 1}, {"a\u0800b", 2, 1}, {"a\u0800b", 3, 1}, {"a\u0800b", 4, 2},
                {"a\u0800b", 5, 3}, {"a\u0800b", 6, 3}
        });
    }

    public StringTruncationTest(String s, int max, int truncatedWidth)
    {
        this.s = s;
        this.max = max;
        this.truncatedWidth = truncatedWidth;
    }
    
    @Test
    public void truncate() throws UnsupportedEncodingException
    {
        byte[] b = s.getBytes("UTF-8");
        int n = Utilities.truncateUTF8(b, max);
        assert new String(b, 0, n, "UTF-8").equals(s.substring(0, truncatedWidth));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void negativeLength()
    {
        Utilities.truncateUTF8(new byte[0], -1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void invalidUtf8()
    {
        Utilities.truncateUTF8(new byte[] { -0x80, -0x80, -0x80, -0x80 }, 2);
    }
}
