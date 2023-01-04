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

package io.nosqlbench.adapter.couchbase.exceptions;

import io.nosqlbench.adapter.couchbase.core.CouchbaseOpTypes;

public class CouchbaseOpFailedException extends RuntimeException {
    private final CouchbaseOpTypes opType;

    public CouchbaseOpFailedException(CouchbaseOpTypes opType, Throwable cause) {
        super(cause);
        this.opType = opType;
    }

    @Override
    public String getMessage() {
        return "Error executing `" + opType + "` operation in Couchbase. Response, status=" + getCause().getMessage() +
            ": " + super.getMessage();
    }
}
