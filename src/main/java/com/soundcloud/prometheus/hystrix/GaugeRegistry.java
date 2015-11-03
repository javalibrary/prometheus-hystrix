/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.soundcloud.prometheus.hystrix;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

import java.util.concurrent.ConcurrentHashMap;

public class GaugeRegistry {

    private final ConcurrentHashMap<String, Gauge> gauges = new ConcurrentHashMap<String, Gauge>();

    private final CollectorRegistry registry;
    private final String namespace;

    public GaugeRegistry(CollectorRegistry registry, String namespace) {
        this.namespace = namespace;
        this.registry = registry;
    }

    /**
     * Register a Gauge for the given subsystem and metric, and return the metricName to be used to lookup the Gauge.
     *
     * @see #getGauge(String)
     */
    public String registerGauge(String subsystem, String metric, String documentation, String... labelNames) {
        String metricName = subsystem + ":" + metric;
        Gauge gauge = Gauge.build()
                .namespace(namespace)
                .subsystem(subsystem)
                .name(metric)
                .help(documentation)
                .labelNames(labelNames)
                .create();
        Gauge existing = gauges.putIfAbsent(metricName, gauge);
        if (existing == null) {
            registry.register(gauge);
        }
        return metricName;
    }

    /**
     * Returns a Gauge for the given metricName.
     *
     * @see #registerGauge(String, String, String, String...)
     */
    public Gauge getGauge(String metricName) {
        return gauges.get(metricName);
    }
}