/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

public class TextImageExtractTest {
    private final static Logger logger = LogManager.getLogger(TextImageExtractTest.class);

    /**
     * These tests are largely illustrative for those integrating this function into their
     * binding recipes.
     */
    @Test
    public void testCtors() {

        CharBufImage f1 = new CharBufImage(10);
        logger.debug(() -> "f1:" + f1.apply(1L));

        CharBufImage f2 = new CharBufImage("abc123",15);
        logger.debug(() -> "f2:" + f2.apply(1L));

        CharBufImage f3 = new CharBufImage("abcdef",10,3L,5);
        logger.debug(() -> "f3:" + f3.apply(1L));
    }

    @Test
    public void testComposedFromStringFunc() {
        NumberNameToString nnts = new NumberNameToString();
        CharBufImage cbi = new CharBufImage(nnts, 100, 20);
        logger.debug(() -> "cbi:" + cbi.apply(1L));
    }


}
