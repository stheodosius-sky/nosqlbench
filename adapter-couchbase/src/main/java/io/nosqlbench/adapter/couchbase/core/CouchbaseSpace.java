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
import com.couchbase.client.java.ClusterOptions;
import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;


public class CouchbaseSpace implements NBNamedElement, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(CouchbaseSpace.class);

    private final String name;
    private final Cluster cluster;
    private final Bucket bucket;

    public CouchbaseSpace(String name, NBConfiguration cfg) {
        this.name = name;
        String connectionString = cfg.get("connection", String.class);
        String username = cfg.get("username", String.class);
        String password = cfg.get("password", String.class);
        this.cluster = createCouchbaseClient(connectionString, username, password);
        String bucket = cfg.get("bucket", String.class);
        this.bucket = initBucket(this.cluster, bucket);
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(CouchbaseSpace.class)
            .add(Param.required("connection", String.class)
                .setDescription("The connection string for your Couchbase cluster"))
            .add(Param.required("username", String.class)
                .setDescription("The username to use when authenticating with the cluster"))
            .add(Param.required("password", String.class)
                .setDescription("The password to use when authenticating with the cluster"))
            .add(Param.required("bucket", String.class)
                .setDescription("The target bucket"))
            .asReadOnly();
    }

    @Override
    public String getName() {
        return name;
    }

    public Cluster createCouchbaseClient(String connectionString, String username, String password) {
        return Cluster.connect(connectionString,
            ClusterOptions.clusterOptions(username, password).environment(env -> {
                // Sets a pre-configured profile called "wan-development" to help avoid latency issues
                // when accessing Capella from a different Wide Area Network
                // or Availability Zone (e.g. your laptop).
                // todo(simon): allow end users to choose their own profile
                env.applyProfile("wan-development");
            }));
    }

    private Bucket initBucket(Cluster cluster, String bucketName) {
        Bucket bucket = cluster.bucket(bucketName);
        cluster.waitUntilReady(Duration.ofSeconds(10));
        return bucket;
    }

    public Cluster getCluster() {
        return this.cluster;
    }

    public Bucket getBucket() {
        return bucket;
    }

    @Override
    public void close() throws Exception {
        if (this.cluster != null) {
            try {
                this.cluster.disconnect();
            } catch (Exception ex) {
                // todo(simon): find the root cause of this double close behaviour
                logger.warn("timed out attempting to close the connection couchbase - this can occur because nosqlbench closes this class twice?");
            }
        }
    }
}
