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

package io.nosqlbench.engine.core.metrics;

import com.codahale.metrics.Metric;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.*;

public class MetricMap implements ProxyObject {

    private final static Logger logger = LogManager.getLogger(MetricMap.class);
    private final String name;
    private final String parent_name;
    private final HashMap<String, Object> map = new HashMap<>();

    public MetricMap(String name, String parent) {
        this.name = name;
        this.parent_name = parent;
    }

    public MetricMap findOwner(String metricName) {
        String[] names = metricName.split("\\.");
        String[] pathTraversal = Arrays.copyOfRange(names, 0, names.length - 1);
        MetricMap owner = findPath(pathTraversal);
        return owner;
    }

    @Override
    public String toString() {
        return "MetricMap{" +
            "name='" + name + '\'' +
            ", map=" + map +
            (parent_name!=null ? ", parent=" + parent_name : "") +
            '}';
    }

    public MetricMap findPath(String... names) {
        MetricMap current = this;
        for (int i = 0; i < names.length; i++) {
            String edgeName = names[i];

            if (current.map.containsKey(edgeName)) {
                Object element = current.map.get(edgeName);
                if (element instanceof MetricMap) {
                    current = (MetricMap) element;
                    logger.trace(() -> "traversing edge:" + edgeName);
                } else {
                    String error = "edge exists at level:" + i;
                    logger.error(error);
                    throw new RuntimeException(error);
                }
            } else {
                MetricMap newMap = new MetricMap(edgeName,this.name);
                current.map.put(edgeName, newMap);
                current = newMap;
                logger.trace(() -> "adding edge:" + edgeName);
            }
        }
        return current;
    }

    public void add(String name, Metric metric) {
        MetricMap owner = findOwner(name);
        String leafName = name.substring(name.lastIndexOf(".")+1);
        owner.map.put(leafName,metric);
    }

    public void remove(String name) {
        map.remove(name);
    }

    public Object get(String key) {
        return map.get(key);
    }

    public Set<String> getKeys() {
        return map.keySet();
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public Object getMember(String key) {
        Object got = get(key);
        return got;
    }

    @Override
    public Object getMemberKeys() {
        ArrayList<String> keys = new ArrayList<>(getKeys());
        return keys;
    }

    @Override
    public boolean hasMember(String key) {
        boolean got = getKeys().contains(key);
        return got;
    }

    @Override
    public void putMember(String key, Value value) {
        throw new RuntimeException("Not allowed here");
    }
}
