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

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.query.QueryMetrics;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import io.nosqlbench.adapter.couchbase.core.CouchbaseOpTypes;
import io.nosqlbench.adapter.couchbase.exceptions.CouchbaseOpFailedException;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.CycleOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;

public class CouchbaseQueryOp implements CycleOp<QueryResult> {

    private static final Logger logger = LoggerFactory.getLogger(CouchbaseQueryOp.class);

    private final Cluster cluster;
    private final String query;
    private long resultSize;

    // https://docs.couchbase.com/server/current/learn/services-and-indexes/services/query-service.html
    public CouchbaseQueryOp(Cluster cluster, String query) {
        this.cluster = cluster;
        this.query = query;
    }

    @Override
    public QueryResult apply(long cycle) {
        QueryOptions options = queryOptions();
        try {
            QueryResult result = cluster.query(query, options);
            Optional<QueryMetrics> metrics = result.metaData().metrics();
            metrics.ifPresent(m -> this.resultSize = m.resultCount());
            return result;
        } catch (CouchbaseException ex) {
            throw new CouchbaseOpFailedException(CouchbaseOpTypes.query, ex);
        }
    }

    @Override
    public long getResultSize() {
        return resultSize;
    }

}
