/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityconfig;

import io.nosqlbench.engine.api.activityconfig.yaml.OpDef;
import io.nosqlbench.nb.api.config.params.Element;
import io.nosqlbench.nb.api.config.params.NBParams;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.core.templates.BindPoint;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Allow for uniform statement anchor parsing, using the <pre>?anchor</pre>
 * and <pre>{anchor}</pre> anchoring conventions. This type also includes
 * all of the properties from the enclosed StmtDef, in addition to a couple of
 * helpers. It should allow programmers to project this type directly from an
 * existing {@link OpDef} as a substitute.
 */
public class ParsedStmt {

    private final OpDef opDef;
    private final ParsedTemplate parsed;

    /**
     * Construct a new ParsedStatement from the provided stmtDef and anchor token.
     *
     * @param opDef An existing statement def as read from the YAML API.
     */
    public ParsedStmt(OpDef opDef, Function<String, String>... transforms) {
        this.opDef = opDef;
        String transformed = getStmt();
        for (Function<String, String> transform : transforms) {
            transformed = transform.apply(transformed);
        }
        parsed = new ParsedTemplate(transformed, opDef.getBindings());
    }

    public ParsedStmt orError() {
        if (hasError()) {
            throw new RuntimeException("Unable to parse statement: " + this);
        }
        return this;
    }

    public String toString() {
        return parsed.toString();
    }

    /**
     * @return true if the parsed statement is not usable.
     */
    public boolean hasError() {
        return parsed.hasError();
    }


    /**
     * Returns a list of binding names which were referenced
     * in either <pre>{anchor}</pre> or <pre>?anchor</pre> form,
     * but which were not present in the provided bindings map.
     * If any binding names are present in the returned set, then
     * this binding will not be usable.
     *
     * @return A list of binding names which were referenced but not defined*
     */
    public Set<String> getMissingBindings() {
        return parsed.getMissing();
    }

    /**
     * Return the statement that can be used as-is by any driver specific version.
     * This uses the anchor token as provided to yield a version of the statement
     * which contains positional anchors, but no named bindings.
     * @param tokenMapper A function which maps the anchor name to the needed form
     *                    in the callers driver context
     * @return A driver or usage-specific format of the statement, with anchors
     */
    public String getPositionalStatement(Function<String,String> tokenMapper) {
        return parsed.getPositionalStatement(tokenMapper);
    }

    /**
     * @return the statement name from the enclosed {@link OpDef}
     */
    public String getName() {
        return opDef.getName();
    }

    /**
     * @return the raw statement from the enclosed {@link OpDef}
     */
    public String getStmt() {
        if (opDef.getOp() instanceof CharSequence) {
            return opDef.getOp().toString();
        } else {
            throw new BasicError("Tried to access op type '" + opDef.getOp().getClass().getSimpleName() + " as a string statement");
        }
    }

    /**
     * @return the tags from the enclosed {@link OpDef}
     */
    public Map<String, String> getTags() {
        return opDef.getTags();
    }

    /**
     * @return the bindings from the enclosed {@link OpDef}
     */
    public Map<String, String> getBindings() {
        return opDef.getBindings();
    }

    /**
     * @return a params reader from the enclosed {@link OpDef} params map
     */
    public Element getParamReader() {
        return NBParams.one(opDef.getParams());
    }

    public List<BindPoint> getBindPoints() {
        return parsed.getBindPoints();
    }

}
