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

package io.nosqlbench.adapter.couchbase.utils;

import com.couchbase.client.core.error.InvalidArgumentException;
import com.couchbase.client.java.json.JsonObject;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class OpUtils {

    public static LongFunction<? extends Object> createContentFunc(ParsedOp op) {
        LongFunction<String> contentF = op.getAsRequiredFunction("content", String.class);
        String exampleValue = contentF.apply(0);
        return castContent(contentF, exampleValue);
    }

    private static LongFunction<?> castContent(LongFunction<String> contentF, String exampleValue) {
        try {
            JsonObject.fromJson(exampleValue);
            return l -> JsonObject.fromJson(contentF.apply(l));
        } catch (InvalidArgumentException ex) {
            return contentF;
        }
    }
}
