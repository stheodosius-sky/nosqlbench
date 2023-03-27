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

co_cycle_delay_bursty = {
    "alias": "co_cycle_delay_bursty",
    "driver": "diag",
    "cycles": "0..1000000",
    "threads": "10",
    "cyclerate": "1000,1.5",
    "op" : '{"log":{"level":"info","modulo":1000},"diagrate":{"diagrate":"500"}}'
};

print('starting activity co_cycle_delay_bursty');
scenario.start(co_cycle_delay_bursty);
for (i = 0; i < 5; i++) {
    scenario.waitMillis(1000);
    if (!scenario.isRunningActivity('co_cycle_delay_bursty')) {
        print("scenario exited prematurely, aborting.");
        break;
    }
    print("backlogging, cycles=" + metrics.co_cycle_delay_bursty.cycles.servicetime.count +
        " waittime=" + metrics.co_cycle_delay_bursty.cycles.waittime.value +
        " diagrate=" + activities.co_cycle_delay_bursty.diagrate +
        " cyclerate=" + activities.co_cycle_delay_bursty.cyclerate
    );
}
print('step1 metrics.waittime=' + metrics.co_cycle_delay_bursty.cycles.waittime.value);
activities.co_cycle_delay_bursty.diagrate = "10000";

for (i = 0; i < 10; i++) {
    if (!scenario.isRunningActivity('co_cycle_delay_bursty')) {
        print("scenario exited prematurely, aborting.");
        break;
    }
    print("recovering, cycles=" + metrics.co_cycle_delay_bursty.cycles.servicetime.count +
        " waittime=" + metrics.co_cycle_delay_bursty.cycles.waittime.value +
        " diagrate=" + activities.co_cycle_delay_bursty.diagrate +
        " cyclerate=" + activities.co_cycle_delay_bursty.cyclerate
    );

    scenario.waitMillis(1000);
    if (metrics.co_cycle_delay_bursty.cycles.waittime.value < 50000000) {
        print("waittime trended back down as expected, exiting on iteration " + i);
        break;
    }
}
//scenario.awaitActivity("co_cycle_delay");
print('step2 metrics.waittime=' + metrics.co_cycle_delay_bursty.cycles.waittime.value);
scenario.stop(co_cycle_delay_bursty);
print("stopped activity co_cycle_delay_bursty");
