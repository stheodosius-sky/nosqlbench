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

package io.nosqlbench.adapter.couchbase.dispensers;

import com.couchbase.client.java.Bucket;
import io.nosqlbench.adapter.couchbase.core.CouchbaseSpace;
import io.nosqlbench.adapter.couchbase.ops.CouchbaseUpsertOp;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class CouchbaseUpsertOpDispenser extends BaseOpDispenser<Op, CouchbaseSpace> {

    private final Bucket bucket;
    private final String scope;
    private final String collection;
    private final LongFunction<String> idFunc;
    private final LongFunction<String> contentFunc;

    public CouchbaseUpsertOpDispenser(DriverAdapter adapter, Bucket bucket, ParsedOp op) {
        super(adapter, op);
        this.bucket = bucket;
        this.scope = getScope(op);
        this.collection = getCollection(op);
        this.idFunc = createIdFunc(op);
        this.contentFunc = createContentFunc(op);
    }

    private String getScope(ParsedOp op) {
        return op.getStaticValue("scope", String.class);
    }

    private String getCollection(ParsedOp op) {
        return op.getStaticValue("collection", String.class);
    }

    private LongFunction<String> createIdFunc(ParsedOp op) {
        return op.getAsRequiredFunction("upsert", String.class);
    }

    private LongFunction<String> createContentFunc(ParsedOp op) {
        return op.getAsRequiredFunction("content", String.class);
    }

    @Override
    public Op apply(long cycle) {
        return new CouchbaseUpsertOp(
            this.bucket,
            this.scope,
            this.collection,
            this.idFunc.apply(cycle),
            this.contentFunc.apply(cycle)
        );
    }
}
