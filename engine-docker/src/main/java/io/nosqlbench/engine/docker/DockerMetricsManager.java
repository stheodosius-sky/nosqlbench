/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.docker;

/*
 *
 * @author Sebastián Estévez on 4/4/19.
 *
 */


import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.ContainerNetworkSettings;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

import static io.nosqlbench.engine.docker.RestHelper.post;

public class DockerMetricsManager {

    public static final String GRAFANA_TAG = "grafana_tag";
    public static final String PROM_TAG = "prom_tag";
    public static final String TSDB_RETENTION = "tsdb_days";
    public static final String GRAPHITE_SAMPLE_EXPIRY = "graphite_sample_expiry";
    public static final String GRAPHITE_CACHE_SIZE = "graphite_cache_size";
    public static final String GRAPHITE_LOG_LEVEL = "graphite_log_level";
    public static final String GRAPHITE_LOG_FORMAT = "graphite_log_format";

    private final DockerHelper dh;

    String userHome = System.getProperty("user.home");

    private final Logger logger = LogManager.getLogger(DockerMetricsManager.class);

    public DockerMetricsManager() {
        dh = new DockerHelper();
    }

    public void startMetrics(Map<String, String> options) {

        String ip = startGraphite(
                options.get(GRAPHITE_SAMPLE_EXPIRY),
                options.get(GRAPHITE_CACHE_SIZE),
                options.get(GRAPHITE_LOG_LEVEL),
                options.get(GRAPHITE_LOG_FORMAT)
            );

        startPrometheus(ip, options.get(PROM_TAG), options.get(TSDB_RETENTION));

        startGrafana(ip, options.get(GRAFANA_TAG));

    }

    private void startGrafana(String ip, String tag) {

        String GRAFANA_IMG = "grafana/grafana";
        tag = (tag == null || tag.isEmpty()) ? "latest" : tag;
        String name = "grafana";
        List<Integer> port = List.of(3000);

        boolean grafanaFilesExist = grafanaFilesExist();
        if (!grafanaFilesExist) {
            setupGrafanaFiles(ip);
        }

        List<String> volumeDescList = List.of(
            userHome + "/.nosqlbench/grafana:/var/lib/grafana:rw"
            //cwd+"/docker-metrics/grafana:/grafana",
            //cwd+"/docker-metrics/grafana/datasources:/etc/grafana/provisioning/datasources",
            //cwd+"/docker-metrics/grafana/dashboardconf:/etc/grafana/provisioning/dashboards"
            //,cwd+"/docker-metrics/grafana/dashboards:/var/lib/grafana/dashboards:ro"
        );
        List<String> envList = Arrays.asList(
                "GF_SECURITY_ADMIN_PASSWORD=admin",
                "GF_AUTH_ANONYMOUS_ENABLED=\"true\""
//                , "GF_SNAPSHOTS_EXTERNAL_SNAPSHOT_URL=https://assethub.datastax.com:3001",
//                "GF_SNAPSHOTS_EXTERNAL_SNAPSHOT_NAME=\"Upload to DataStax\""
        );

        String reload = null;
        List<String> linkNames = new ArrayList();
        linkNames.add("prom");
        String containerId = dh.startDocker(GRAFANA_IMG, tag, name, port, volumeDescList, envList, null, reload, linkNames);
        if (containerId == null) {
            return;
        }

        dh.pollLog(containerId, new LogCallback());

        logger.info("grafana container started, http listening");

        if (!grafanaFilesExist) {
            configureGrafana();
        }
    }

    private void startPrometheus(String ip, String tag, String retention) {

        logger.info("preparing to start docker metrics");
        String PROMETHEUS_IMG = "prom/prometheus";
        String name = "prom";
        List<Integer> port = List.of(9090);

        if (!promFilesExist()) {
            setupPromFiles(ip);
        }

        List<String> volumeDescList = Arrays.asList(
            //cwd+"/docker-metrics/prometheus:/prometheus",
            userHome + "/.nosqlbench/prometheus-conf:/etc/prometheus",
                userHome + "/.nosqlbench/prometheus:/prometheus"
                //"./prometheus/tg_dse.json:/etc/prometheus/tg_dse.json"
        );

        List<String> envList = null;

        List<String> cmdList = Arrays.asList(
                "--config.file=/etc/prometheus/prometheus.yml",
                "--storage.tsdb.path=/prometheus",
                "--storage.tsdb.retention=" + retention,
                "--web.enable-lifecycle"

        );

        String reload = "http://localhost:9090/-/reload";
        List<String> linkNames = new ArrayList();
        linkNames.add("graphite-exporter");
        dh.startDocker(PROMETHEUS_IMG, tag, name, port, volumeDescList, envList, cmdList, reload, linkNames);

        logger.info("prometheus started and listening");
    }

    private String startGraphite(
        String graphite_sample_expiry,
        String graphite_cache_size,
        String graphite_log_level,
        String graphite_log_format
    ) {

        logger.info("preparing to start graphite exporter container...");

        //docker run -d -p 9108:9108 -p 9109:9109 -p 9109:9109/udp prom/graphite-exporter
        String GRAPHITE_EXPORTER_IMG = "prom/graphite-exporter";
        String tag = "latest";
        String name = "graphite-exporter";
        //TODO: look into UDP
        List<Integer> port = Arrays.asList(9108, 9109);
        List<String> volumeDescList = new ArrayList<String>();

        setupGraphiteFiles(volumeDescList);

        List<String> envList = List.of();

        String reload = null;
        List<String> linkNames = new ArrayList();

        List<String> cmdOpts = Arrays.asList(
            "--graphite.mapping-config=/tmp/graphite_mapping.conf",
            "--graphite.sample-expiry="+graphite_sample_expiry,
            "--graphite.cache-size="+graphite_cache_size,
            "--log.level="+graphite_log_level,
            "--log.format="+graphite_log_format
        );

        dh.startDocker(GRAPHITE_EXPORTER_IMG, tag, name, port, volumeDescList, envList, cmdOpts, reload, linkNames);

        logger.info("graphite exporter container started");

        logger.info("searching for graphite exporter container ip");

        ContainerNetworkSettings settings = dh.searchContainer(name, null, tag).getNetworkSettings();
        Map<String, ContainerNetwork> networks = settings.getNetworks();
        String ip = null;
        for (String key : networks.keySet()) {
            ContainerNetwork network = networks.get(key);
            ip = network.getIpAddress();
        }

        return ip;
    }

    private void setupGraphiteFiles(List<String> volumeDescList) {
        String exporterConfig = NBIO.readCharBuffer("docker/graphite/graphite_mapping.conf").toString();

        File nosqlbenchdir = new File(userHome, "/.nosqlbench/");
        mkdir(nosqlbenchdir);

        File graphiteExporterDir = new File(userHome, "/.nosqlbench/graphite-exporter");
        mkdir(graphiteExporterDir);

        Path mappingPath = Path.of(userHome, ".nosqlbench", "graphite-exporter", "graphite_mapping.conf");

        if (!Files.exists(mappingPath)) {
            try {
                Files.writeString(mappingPath, exporterConfig);
            } catch (IOException e) {
                throw new RuntimeException("Error writing initial graphite mapping config in " + mappingPath, e);
            }
        }

        volumeDescList.add(mappingPath + ":/tmp/graphite_mapping.conf");

    }

    private void setupPromFiles(String ip) {
        String datasource = NBIO.readCharBuffer("docker/prometheus/prometheus.yml").toString();

        if (ip == null) {
            throw new DockerInitError("IP for graphite container not found");
        }

        datasource = datasource.replace("!!!GRAPHITE_IP!!!", "graphite-exporter");

        File nosqlbenchDir = new File(userHome, "/.nosqlbench/");
        mkdir(nosqlbenchDir);


        File prometheusDir = new File(userHome, "/.nosqlbench/prometheus");
        mkdir(prometheusDir);

        File promConfDir = new File(userHome, "/.nosqlbench/prometheus-conf");
        mkdir(promConfDir);

        Path prometheusDirPath = Paths.get(userHome, "/.nosqlbench" +
                "/prometheus");

        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);

        try {
            Files.setPosixFilePermissions(prometheusDirPath, perms);
        } catch (IOException e) {
            logger.error("failed to set permissions on prom backup " +
                    "directory " + userHome + "/.nosqlbench/prometheus)");
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try (PrintWriter out = new PrintWriter(
                new FileWriter(userHome + "/.nosqlbench/prometheus-conf" +
                        "/prometheus.yml", false))) {
            out.println(datasource);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("error writing prometheus yaml file to ~/.prometheus");
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("creating file in ~/.prometheus");
            throw new RuntimeException(e);
        }
    }

    private void mkdir(File dir) {
        if (dir.exists()) {
            return;
        }
        if (!dir.mkdir()) {
            if (!dir.canWrite()) {
                throw new DockerInitError("no write access to " + dir.getPath());
            }
            if (!dir.canRead()) {
                throw new DockerInitError("no read access to " + dir.getPath());
            }
            throw new DockerInitError("Could not create directory " + dir.getPath() + ". Fix directory permissions to run --docker-metrics");
        }
    }

    private boolean grafanaFilesExist() {
        File nosqlbenchDir = new File(userHome, "/.nosqlbench/");
        boolean exists = nosqlbenchDir.exists();
        if (exists) {
            File grafana = new File(userHome, "/.nosqlbench/grafana");
            exists = grafana.exists();
        }
        return exists;
    }

    private boolean promFilesExist() {
        File nosqlbenchDir = new File(userHome, "/.nosqlbench/");
        boolean exists = nosqlbenchDir.exists();
        if (exists) {
            File prom = new File(userHome, "/.nosqlbench/grafana");
            exists = prom.exists();
        }
        return exists;
    }

    private void setupGrafanaFiles(String ip) {

        File grafanaDir = new File(userHome, "/.nosqlbench/grafana");
        mkdir(grafanaDir);

        Path grafanaDirPath = Paths.get(userHome, "/.nosqlbench/grafana");

        Set<PosixFilePermission> perms = new HashSet<>();

        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);

        try {
            Files.setPosixFilePermissions(grafanaDirPath, perms);
        } catch (IOException e) {
            logger.error("failed to set permissions on grafana directory " +
                    "directory " + userHome + "/.nosqlbench/grafana)");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    private void configureGrafana() {
        List<Content<?>> dashboardContent = NBIO.all().searchPrefixes("docker/dashboards").extensionSet(".json").list();

        for (Content<?> content : dashboardContent) {
            String dashboardData = content.asString();
            post(
                    "http://localhost:3000/api/dashboards/db",
                    () -> dashboardData,
                    true,
                    "load dashboard from " + content.asPath().toString()
            );

        }

        List<Content<?>> datasources = NBIO.all().searchPrefixes("docker/datasources").extensionSet(".yaml").list();

        for (Content<?> datasource : datasources) {
            String datasourceContent = datasource.asString();
            post(
                    "http://localhost:3000/api/datasources",
                    () -> datasourceContent,
                    true,
                    "configure data source from " + datasource.asPath().toString());
        }

       logger.warn("default grafana creds are admin/admin");
    }


    public void stopMetrics() {
        //TODO: maybe implement
    }

    private class LogCallback extends ResultCallbackTemplate<LogContainerResultCallback, Frame> {
        @Override
        public void onNext(Frame item) {
            if (item.toString().contains("HTTP Server Listen")) {
                try {
                    close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
