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

package io.nosqlbench.adapter.couchbase.ops;

import com.couchbase.client.core.error.DocumentExistsException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.kv.InsertOptions;
import com.couchbase.client.java.kv.MutationResult;
import io.nosqlbench.adapter.couchbase.exceptions.CouchbaseOpFailedException;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.CycleOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.couchbase.client.java.kv.InsertOptions.insertOptions;
import static io.nosqlbench.adapter.couchbase.core.CouchbaseOpTypes.insert;

public class CouchbaseInsertOp implements CycleOp<MutationResult> {

    private static final Logger logger = LoggerFactory.getLogger(CouchbaseInsertOp.class);

    private final Bucket bucket;
    private String scope;
    private String collection;
    private String id;
    private String content;

    public CouchbaseInsertOp(Bucket bucket, String scope, String collection, String id, String content) {
        this.bucket = bucket;
        this.scope = scope;
        this.collection = collection;
        this.id = id;
        this.content = content;
    }

    @Override
    public MutationResult apply(long cycle) {
        Collection collection = this.bucket.scope(this.scope)
            .collection(this.collection);

        InsertOptions options = insertOptions();
//            .expiry(Duration.ofMillis())
//            .durability(DurabilityLevel.valueOf()); // todo(simon): allow these options to be configured in the activity
        try {
            MutationResult result = collection.insert(this.id, this.content, options);
            return result;
        } catch (DocumentExistsException ex) {
            throw new CouchbaseOpFailedException(insert, ex);
        }
    }

}
