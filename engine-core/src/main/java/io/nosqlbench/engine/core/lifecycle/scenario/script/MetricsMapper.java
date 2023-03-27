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
package io.nosqlbench.engine.core.lifecycle.scenario.script;

import com.codahale.metrics.*;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.engine.core.lifecycle.activity.ActivityTypeLoader;
import io.nosqlbench.engine.core.lifecycle.scenario.script.bindings.PolyglotMetricRegistryBindings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Find the metrics associated with an activity type by instantiating the activity in idle mode.
 */
public class MetricsMapper {
    private final static Logger logger = LogManager.getLogger(MetricsMapper.class);
    private static final Set<Class<?>> metricsElements = new HashSet<>() {{
        add(Meter.class);
        add(Counter.class);
        add(Timer.class);
        add(Histogram.class);
        add(Gauge.class);
        add(Snapshot.class);
    }};
    private static final Predicate<Method> isSimpleGetter = method ->
            method.getName().startsWith("get")
                    && method.getParameterCount() == 0
                    && !method.getName().equals("getClass");

    private static final Function<Method, String> getPropertyName = method ->
    {
        String mName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
        return mName;
    };

    public static String metricsDetail(String activitySpec) {

        //StringBuilder metricsDetail = new StringBuilder();
        List<String> metricsDetails = new ArrayList<>();

        ActivityDef activityDef = ActivityDef.parseActivityDef(activitySpec);
        logger.info(() -> "introspecting metric names for " + activitySpec);

        Optional<ActivityType> activityType = new ActivityTypeLoader().load(activityDef);

        if (!activityType.isPresent()) {
            throw new RuntimeException("Activity type '" + activityDef.getActivityType() + "' does not exist in this runtime.");
        }
        Activity activity = activityType.get().getAssembledActivity(activityDef, new HashMap<>());
        PolyglotMetricRegistryBindings nashornMetricRegistryBindings = new PolyglotMetricRegistryBindings(ActivityMetrics.getMetricRegistry());
        activity.initActivity();
        activity.getInputDispenserDelegate().getInput(0);
        activity.getActionDispenserDelegate().getAction(0);
        activity.getMotorDispenserDelegate().getMotor(activityDef, 0);

        Map<String, Metric> metricMap = nashornMetricRegistryBindings.getMetrics();

//        Map<String, Map<String,String>> details = new LinkedHashMap<>();

        for (Map.Entry<String, Metric> metricEntry : metricMap.entrySet()) {
            String metricName = metricEntry.getKey();
            Metric metricValue = metricEntry.getValue();

            Map<String, String> getterSummary = getGetterSummary(metricValue);
//            details.put(metricName,getterSummary);

            List<String> methodDetails = getterSummary.entrySet().stream().map(
                    es -> metricName + es.getKey() + "  " + es.getValue()
            ).collect(Collectors.toList());
            methodDetails.sort(String::compareTo);
            String getterText = methodDetails.stream().collect(Collectors.joining("\n"));
            metricsDetails.add(metricName + "\n" + getterText);
        }
//        return details;

        return metricsDetails.stream().collect(Collectors.joining("\n"));
    }

    private static Map<String, String> getGetterSummary(Object o) {
        return getGetterSummary(new HashMap<>(), "", o.getClass());
    }

    private static Map<String, String> getGetterSummary(Map<String, String> accumulator, String name, Class<?> objectType) {
        Arrays.stream(objectType.getMethods())
                .filter(isSimpleGetter)
                .forEach(m -> {
                    if (m.getReturnType().isPrimitive()) {
                        accumulator.put(name + "." + getPropertyName.apply(m), m.getReturnType().getSimpleName());
                    } else {
                        String fullName = name + "." + getPropertyName.apply(m);
                        getGetterSummary(accumulator, fullName, m.getReturnType());
                    }
                });
        return accumulator;
    }

}
