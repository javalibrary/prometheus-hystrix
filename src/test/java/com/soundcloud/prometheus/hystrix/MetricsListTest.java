package com.soundcloud.prometheus.hystrix;

import com.netflix.hystrix.Hystrix;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author Alexander Schwartz 2017
 */
public class MetricsListTest {

    @After
    public void teardown() {
        Hystrix.reset();
        CollectorRegistry.defaultRegistry.clear();
    }

    @Test
    public void shouldWriteNiceMetricsOutput() throws IOException {
        // given
        HystrixPrometheusMetricsPublisher.builder().shouldExportDeprecatedMetrics(false).buildAndRegister();
        TestHystrixCommand command = new TestHystrixCommand("any");

        // when
        command.execute();

        // then
        try (Writer writer = new FileWriter("target/sample.txt")) {
            TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
            writer.flush();
        }
    }

    @Test
    public void shouldHaveExponentialBuckets() throws IOException {
        // given
        HystrixPrometheusMetricsPublisher.builder().withExponentialBuckets().buildAndRegister();
        TestHystrixCommand command = new TestHystrixCommand("any");

        // when
        command.execute();

        // then
        StringWriter writer = new StringWriter();
        try {
            TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
            writer.flush();
        } finally {
            writer.close();
        }
        String result = writer.toString();
        Assertions.assertThat(result).contains("le=\"0.001\"");
        Assertions.assertThat(result).contains("le=\"2.5169093494697568\"");
    }

}