package io.nosqlbench.activitytype.cockroachdb;

import io.nosqlbench.activitytype.jdbc.api.JDBCActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(ActivityType.class)
public class CockroachActivityType implements ActivityType<CockroachActivity> {
    @Override
    public String getName() {
        return "cockroachdb";
    }

    @Override
    public ActionDispenser getActionDispenser(CockroachActivity activity) {
        return new JDBCActionDispenser(activity);
    }

    @Override
    public CockroachActivity getActivity(ActivityDef activityDef) {
        return new CockroachActivity(activityDef);
    }
}
