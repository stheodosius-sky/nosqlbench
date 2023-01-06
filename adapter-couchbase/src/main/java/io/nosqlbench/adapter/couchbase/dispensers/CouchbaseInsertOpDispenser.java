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
import io.nosqlbench.adapter.couchbase.ops.CouchbaseInsertOp;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

import static io.nosqlbench.adapter.couchbase.utils.OpUtils.createContentFunc;

public class CouchbaseInsertOpDispenser extends BaseOpDispenser<Op, CouchbaseSpace> {

    private final Bucket bucket;
    private final String scope;
    private final String collection;
    private final LongFunction<String> idFunc;
    private final LongFunction<? extends Object> contentFunc;

    public CouchbaseInsertOpDispenser(DriverAdapter adapter, Bucket bucket, ParsedOp op) {
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
        return op.getAsRequiredFunction("insert", String.class);
    }

    @Override
    public Op apply(long cycle) {
        return new CouchbaseInsertOp(
            this.bucket,
            this.scope,
            this.collection,
            this.idFunc.apply(cycle),
            this.contentFunc.apply(cycle)
        );
    }
}
