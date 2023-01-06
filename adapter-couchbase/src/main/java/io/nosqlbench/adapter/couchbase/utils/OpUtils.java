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
