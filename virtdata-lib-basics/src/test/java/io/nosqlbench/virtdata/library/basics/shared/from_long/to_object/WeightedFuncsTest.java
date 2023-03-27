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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_object;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.FixedValues;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class WeightedFuncsTest {
    private final static Logger logger = LogManager.getLogger(WeightedFuncsTest.class);

    @Test
    public void testFuncSelectionDistribution() {
        WeightedFuncs f = new WeightedFuncs(
                1.0d, new FixedValues(0L),
                1.0d, new FixedValues(1L),
                1.0d, new FixedValues(2L),
                1.0d, new FixedValues(3L),
                1.0d, new FixedValues(4L),
                1.0d, new FixedValues(5L),
                1.0d, new FixedValues(6L),
                1.0d, new FixedValues(7L),
                1.0d, new FixedValues(8L),
                1.0d, new FixedValues(9L)
        );
        long[] results = new long[10];

        for (int i = 0; i < 1000000; i++) {
            Object o = f.apply(i);
            int v = ((Long) o).intValue();
            results[v]++;
        }
        logger.debug(() -> Arrays.toString(results));

    }

}
