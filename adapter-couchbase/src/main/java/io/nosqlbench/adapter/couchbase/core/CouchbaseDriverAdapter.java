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

package io.nosqlbench.adapter.couchbase.core;

import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.nb.annotations.Service;

import java.util.function.Function;

@Service(value = DriverAdapter.class, selector = "couchbase")
public class CouchbaseDriverAdapter extends BaseDriverAdapter<Op, CouchbaseSpace> {

    @Override
    public OpMapper<Op> getOpMapper() {
        DriverSpaceCache<? extends CouchbaseSpace> spaceCache = getSpaceCache();
        return new CouchbaseOpMapper(this, spaceCache);
    }

    @Override
    public Function<String, ? extends CouchbaseSpace> getSpaceInitializer(NBConfiguration cfg) {
        return s -> new CouchbaseSpace(s, cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return super.getConfigModel().add(CouchbaseSpace.getConfigModel());
    }

}
