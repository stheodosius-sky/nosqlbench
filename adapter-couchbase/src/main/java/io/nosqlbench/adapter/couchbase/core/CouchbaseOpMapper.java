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

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import io.nosqlbench.adapter.couchbase.dispensers.*;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class CouchbaseOpMapper implements OpMapper<Op> {
    private final static Logger logger = LogManager.getLogger(CouchbaseOpMapper.class);

    private final CouchbaseDriverAdapter adapter;
    private final DriverSpaceCache<? extends CouchbaseSpace> cache;

    public CouchbaseOpMapper(CouchbaseDriverAdapter adapter, DriverSpaceCache<? extends CouchbaseSpace> cache) {
        this.adapter = adapter;
        this.cache = cache;
    }

    @Override
    public OpDispenser<? extends Op> apply(ParsedOp op) {
        String space = op.getStaticConfigOr("space", "default");
        Cluster cluster = cache.get(space).getCluster();
        Bucket bucket = cache.get(space).getBucket();

        Optional<TypeAndTarget<CouchbaseOpTypes, String>> target = op.getOptionalTypeAndTargetEnum(CouchbaseOpTypes.class, String.class);

        if (target.isPresent()) {
            TypeAndTarget<CouchbaseOpTypes, String> targetData = target.get();
            return switch (targetData.enumId) {
                case query -> new CouchbaseQueryOpDispenser(adapter, cluster, op);
                case insert -> new CouchbaseInsertOpDispenser(adapter, bucket, op);
                case upsert -> new CouchbaseUpsertOpDispenser(adapter, bucket, op);
                case retrieve -> new CouchbaseRetrieveOpDispenser(adapter, bucket, op);
                case replace -> new CouchbaseReplaceOpDispenser(adapter, bucket, op);
                case remove -> new CouchbaseRemoveOpDispenser(adapter, bucket, op);
            };
        }
        return new CouchbaseQueryOpDispenser(adapter, cluster, op);
    }
}
