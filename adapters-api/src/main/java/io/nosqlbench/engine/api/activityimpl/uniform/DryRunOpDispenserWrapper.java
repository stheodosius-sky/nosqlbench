/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;

public class DryRunOpDispenserWrapper extends BaseOpDispenser<Op, Object> {

    private final OpDispenser<? extends Op> realDispenser;

    public DryRunOpDispenserWrapper(DriverAdapter<Op,Object> adapter, ParsedOp pop, OpDispenser<? extends Op> realDispenser) {
        super(adapter, pop);
        this.realDispenser = realDispenser;
    }
    @Override
    public DryRunOp apply(long cycle) {
        Op op = realDispenser.apply(cycle);
        return new DryRunOp(op);
    }
}
