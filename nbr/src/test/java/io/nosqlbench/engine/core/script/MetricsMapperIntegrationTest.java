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

package io.nosqlbench.engine.core.script;

import io.nosqlbench.engine.core.lifecycle.scenario.script.MetricsMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricsMapperIntegrationTest {

    @Test
    public void testDiagMetrics() {
        String diagMetrics = MetricsMapper.metricsDetail("driver=diag;alias=foo;cycles=1;op=noop");
        assertThat(diagMetrics).contains("metrics.foo.result.fiveMinuteRate");
    }

}
