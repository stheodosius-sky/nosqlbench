///*
// * Copyright (c) 2022 nosqlbench
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package io.nosqlbench.adapter.couchbase.ops;
//
//import com.couchbase.client.core.error.CouchbaseException;
//import com.couchbase.client.java.Bucket;
//import com.couchbase.client.java.Collection;
//import com.couchbase.client.java.kv.MutateInOptions;
//import com.couchbase.client.java.kv.MutateInResult;
//import io.nosqlbench.adapter.couchbase.core.CouchbaseOpTypes;
//import io.nosqlbench.adapter.couchbase.exceptions.CouchbaseOpFailedException;
//import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.CycleOp;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//
//import static com.couchbase.client.java.kv.MutateInOptions.mutateInOptions;
//
//public class CouchbaseUpdateOp implements CycleOp<MutateInResult> {
//
//    private static final Logger logger = LoggerFactory.getLogger(CouchbaseUpdateOp.class);
//
//    private final Bucket bucket;
//    private String scope;
//    private String collection;
//    private String id;
//    private String content;
//
//    public CouchbaseUpdateOp(Bucket bucket, String scope, String collection, String id, String content) {
//        this.bucket = bucket;
//        this.scope = scope;
//        this.collection = collection;
//        this.id = id;
//        this.content = content;
//    }
//
//    @Override
//    public MutateInResult apply(long cycle) {
//        Collection collection = this.bucket.scope(this.scope)
//            .collection(this.collection);
//
//        MutateInOptions options = mutateInOptions();
//        try {
//            MutateInResult result = collection.mutateIn(this.id, List.of(), options);
//            return result;
//        } catch (CouchbaseException ex) {
//            throw new CouchbaseOpFailedException(CouchbaseOpTypes.upsert, ex); // todo(simon): switch the optype to update
//        }
//    }
//
//}
