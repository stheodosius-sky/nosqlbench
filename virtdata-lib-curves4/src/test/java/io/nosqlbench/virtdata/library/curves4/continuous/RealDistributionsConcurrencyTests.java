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

package io.nosqlbench.virtdata.library.curves4.continuous;

import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

public class RealDistributionsConcurrencyTests {
    private final static Logger logger = LogManager.getLogger(RealDistributionsConcurrencyTests.class);

    @Test
    public void testConcurrentBinomialHashValues() {
        testConcurrentRealHashDistValues(
                "Normal(10.0,2.0)/100 threads/1000 iterations",
                100,
                1000,
                "Normal(10.0,2.0)");
    }

    private void testConcurrentRealHashDistValues(
            String description,
            int threads,
            int iterations,
            String mapperSpec) {

        DataMapper<Double> mapper = VirtData.getMapper(mapperSpec, double.class);
        double[] values = new double[iterations];
        for (int index = 0; index < iterations; index++) {
            values[index] = mapper.get(index);
        }

        ExecutorService pool = Executors.newFixedThreadPool(threads);

        List<Future<double[]>> futures = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            futures.add(pool.submit(new RealDistributionCallable(t, iterations, mapperSpec, pool)));
        }
        try {
            Thread.sleep(1000);
            synchronized (pool) {
                pool.notifyAll();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        List<double[]> results = new ArrayList<>();
//        long[][] results = new long[threads][iterations];

        for (int i = 0; i < futures.size(); i++) {
            try {
                results.add(futures.get(i).get());
                logger.trace(description + ": got results for thread " + i);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        pool.shutdown();

        for (int vthread = 0; vthread < threads; vthread++) {
            assertThat(results.get(vthread)).isEqualTo(values);
            logger.debug(description + ": verified values for thread " + vthread);
        }


    }

    private static class RealDistributionCallable implements Callable<double[]> {

        private final Object signal;
        private final int slot;
        private final String mapperSpec;
        private final int size;

        public RealDistributionCallable(int slot, int size, String mapperSpec, Object signal) {
            this.slot = slot;
            this.size = size;
            this.mapperSpec = mapperSpec;
            this.signal = signal;
        }

        @Override
        public double[] call() throws Exception {
            double[] output = new double[size];
            DataMapper<Double> mapper = VirtData.getMapper(mapperSpec, double.class);
            logger.trace(() -> "resolved:" + mapper);

            synchronized (signal) {
                signal.wait(10000);
            }

            for (int i = 0; i < output.length; i++) {
                output[i] = mapper.get(i);
                if ((i % 100) == 0) {
                    logger.trace("wrote t:" + slot + ", iter:" + i + ", val:" + output[i]);
                }
            }
            return output;
        }
    }

}
