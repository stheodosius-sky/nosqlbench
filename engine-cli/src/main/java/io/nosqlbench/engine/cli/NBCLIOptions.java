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

package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.api.metrics.IndicatorMode;
import io.nosqlbench.api.engine.util.Unit;
import io.nosqlbench.engine.core.lifecycle.scenario.Scenario;
import io.nosqlbench.nb.annotations.Maturity;
import io.nosqlbench.api.system.NBEnvironment;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.api.logging.NBLogLevel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * No CLI parser lib is useful for command structures, it seems. So we have this instead, which is
 * good enough. If something better is needed later, this can be replaced.
 */
public class NBCLIOptions {

//    private final static Logger logger = LogManager.getLogger("OPTIONS");


    private final static String NB_STATE_DIR = "--statedir";
    private final static String NB_STATEDIR_PATHS = "$NBSTATEDIR:$PWD/.nosqlbench:$HOME/.nosqlbench";
    public static final String ARGS_FILE_DEFAULT = "$NBSTATEDIR/argsfile";
    private static final String INCLUDE = "--include";

    private final static String userHome = System.getProperty("user.home");


    private static final String METRICS_PREFIX = "--metrics-prefix";
    private static final String ANNOTATE_EVENTS = "--annotate";
    private static final String ANNOTATORS_CONFIG = "--annotators";

    // Enabled if the TERM env var is provided
    private final static String ANSI = "--ansi";

    private final static String DEFAULT_CHART_HDR_LOG_NAME = "hdrdata-for-chart.log";

    // Discovery
    private static final String HELP = "--help";
    private static final String LIST_COMMANDS = "--list-commands";
    private static final String LIST_METRICS = "--list-metrics";
    private static final String LIST_DRIVERS = "--list-drivers";
    private static final String LIST_ACTIVITY_TYPES = "--list-activity-types";
    private static final String LIST_SCRIPTS = "--list-scripts";
    private static final String LIST_WORKLOADS = "--list-workloads";
    private static final String LIST_SCENARIOS = "--list-scenarios";
    private static final String LIST_INPUT_TYPES = "--list-input-types";
    private static final String LIST_OUTPUT_TYPES = "--list-output-types";
    private static final String LIST_APPS = "--list-apps";
    private static final String VERSION_COORDS = "--version-coords";
    private static final String VERSION = "--version";
    private static final String SHOW_SCRIPT = "--show-script";
    private static final String COMPILE_SCRIPT = "--compile-script";
    private static final String SCRIPT_FILE = "--script-file";
    private static final String COPY = "--copy";
    private static final String SHOW_STACKTRACES = "--show-stacktraces";
    private static final String EXPERIMENTAL = "--experimental";
    private static final String MATURITY = "--maturity";

    // Execution
    private static final String EXPORT_CYCLE_LOG = "--export-cycle-log";
    private static final String IMPORT_CYCLE_LOG = "--import-cycle-log";
    private static final String HDR_DIGITS = "--hdr-digits";

    // Execution Options


    private static final String SESSION_NAME = "--session-name";
    private static final String LOGS_DIR = "--logs-dir";
    private static final String WORKSPACES_DIR = "--workspaces-dir";
    private static final String LOGS_MAX = "--logs-max";
    private static final String LOGS_LEVEL = "--logs-level";
    private static final String DASH_V_INFO = "-v";
    private static final String DASH_VV_DEBUG = "-vv";
    private static final String DASH_VVV_TRACE = "-vvv";
    private static final String REPORT_INTERVAL = "--report-interval";
    private static final String REPORT_GRAPHITE_TO = "--report-graphite-to";
    private static final String GRAPHITE_LOG_LEVEL = "--graphite-log-level";
    private static final String REPORT_CSV_TO = "--report-csv-to";
    private static final String REPORT_SUMMARY_TO = "--report-summary-to";
    private final static String REPORT_SUMMARY_TO_DEFAULT = "stdout:60,_LOGS_/_SESSION_.summary";
    private static final String PROGRESS = "--progress";
    private static final String WITH_LOGGING_PATTERN = "--with-logging-pattern";
    private static final String LOGGING_PATTERN = "--logging-pattern";
    private static final String CONSOLE_PATTERN = "--console-pattern";
    private static final String LOGFILE_PATTERN = "--logfile-pattern";
    private static final String LOG_HISTOGRAMS = "--log-histograms";
    private static final String LOG_HISTOSTATS = "--log-histostats";
    private static final String CLASSIC_HISTOGRAMS = "--classic-histograms";
    private final static String LOG_LEVEL_OVERRIDE = "--log-level-override";
    private final static String ENABLE_CHART = "--enable-chart";

    private final static String DOCKER_METRICS = "--docker-metrics";
    private final static String DOCKER_METRICS_AT = "--docker-metrics-at";
    private static final String DOCKER_GRAFANA_TAG = "--docker-grafana-tag";
    private static final String DOCKER_PROM_TAG = "--docker-prom-tag";
    private static final String DOCKER_PROM_RETENTION_DAYS = "--docker-prom-retention-days";

    private static final String GRAALJS_ENGINE = "--graaljs";

    private static final String DEFAULT_CONSOLE_PATTERN = "TERSE";
    private static final String DEFAULT_LOGFILE_PATTERN = "VERBOSE";

    //    private static final String DEFAULT_CONSOLE_LOGGING_PATTERN = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";


    private final List<Cmd> cmdList = new ArrayList<>();
    private int logsMax = 0;
    private boolean wantsVersionShort = false;
    private boolean wantsVersionCoords = false;
    private boolean wantsActivityHelp = false;
    private String wantsActivityHelpFor;
    private boolean wantsActivityTypes = false;
    private boolean wantsBasicHelp = false;
    private String reportGraphiteTo = null;
    private String reportCsvTo = null;
    private int reportInterval = 10;
    private String metricsPrefix = "nosqlbench";
    private String wantsMetricsForActivity;
    private String sessionName = "";
    private boolean showScript = false;
    private NBLogLevel consoleLevel = NBLogLevel.WARN;
    private final List<String> histoLoggerConfigs = new ArrayList<>();
    private final List<String> statsLoggerConfigs = new ArrayList<>();
    private final List<String> classicHistoConfigs = new ArrayList<>();
    private String progressSpec = "console:1m";
    private String logsDirectory = "logs";
    private String workspacesDirectory = "workspaces";
    private boolean wantsInputTypes = false;
    private boolean wantsMarkerTypes = false;
    private String[] rleDumpOptions = new String[0];
    private String[] cyclelogImportOptions = new String[0];
    private String consoleLoggingPattern = DEFAULT_CONSOLE_PATTERN;
    private String logfileLoggingPattern = DEFAULT_LOGFILE_PATTERN;
    private NBLogLevel logsLevel = NBLogLevel.INFO;
    private Map<String, String> logLevelsOverrides = new HashMap<>();
    private boolean enableChart = false;
    private boolean dockerMetrics = false;
    private boolean wantsListScenarios = false;
    private boolean wantsListScripts = false;
    private String wantsToCopyWorkload = null;
    private boolean wantsWorkloadsList = false;
    private final List<String> wantsToIncludePaths = new ArrayList<>();
    private Scenario.Engine engine = Scenario.Engine.Graalvm;
    private int hdr_digits = 3;
    private String docker_grafana_tag = "7.3.4";
    private String docker_prom_tag = "latest";
    private boolean showStackTraces = false;
    private boolean compileScript = false;
    private String scriptFile = null;
    private String[] annotateEvents = new String[]{"ALL"};
    private String dockerMetricsHost;
    private String annotatorsConfig = "";
    private String statedirs = NB_STATEDIR_PATHS;
    private Path statepath;
    private final List<String> statePathAccesses = new ArrayList<>();
    private final String hdrForChartFileName = DEFAULT_CHART_HDR_LOG_NAME;
    private String dockerPromRetentionDays = "3650d";
    private String reportSummaryTo = REPORT_SUMMARY_TO_DEFAULT;
    private boolean enableAnsi = System.getenv("TERM")!=null && !System.getenv("TERM").isEmpty();
    private Maturity minMaturity = Maturity.Unspecified;
    private String graphitelogLevel="info";
    private boolean wantsListCommands = false;
    private boolean wantsListApps = false;

    public boolean isWantsListApps() {
        return wantsListApps;
    }

    public boolean getWantsListCommands() {
        return wantsListCommands;
    }
    public String getAnnotatorsConfig() {
        return annotatorsConfig;
    }


    public String getChartHdrFileName() {
        return hdrForChartFileName;
    }

    public String getDockerPromRetentionDays() {
        return this.dockerPromRetentionDays;
    }

    public String getReportSummaryTo() {
        return reportSummaryTo;
    }

    public void setWantsStackTraces(boolean wantsStackTraces) {
        this.showStackTraces=wantsStackTraces;
    }

    public boolean isEnableAnsi() {
        return enableAnsi;
    }

    public String getLogfileLoggingPattern() {
        return logfileLoggingPattern;
    }

    public String getGraphiteLogLevel() {
        return this.graphitelogLevel;
    }

    public enum Mode {
        ParseGlobalsOnly,
        ParseAllOptions
    }

    public NBCLIOptions(String[] args) {
        this(args, Mode.ParseAllOptions);
    }

    public NBCLIOptions(String[] args, Mode mode) {
        switch (mode) {
            case ParseGlobalsOnly:
                parseGlobalOptions(args);
                break;
            case ParseAllOptions:
                parseAllOptions(args);
                break;
        }
    }

    private LinkedList<String> parseGlobalOptions(String[] args) {

        LinkedList<String> arglist = new LinkedList<>() {{
            addAll(Arrays.asList(args));
        }};

        if (arglist.peekFirst() == null) {
            wantsBasicHelp = true;
            return arglist;
        }

        // Process --include and --statedir, separately first
        // regardless of position
        LinkedList<String> nonincludes = new LinkedList<>();
        while (arglist.peekFirst() != null) {
            String word = arglist.peekFirst();
            if (word.startsWith("--") && word.contains("=")) {
                String wordToSplit = arglist.removeFirst();
                String[] split = wordToSplit.split("=", 2);
                arglist.offerFirst(split[1]);
                arglist.offerFirst(split[0]);
                continue;
            }

            switch (word) {
                case NB_STATE_DIR:
                    arglist.removeFirst();
                    this.statedirs = readWordOrThrow(arglist, "nosqlbench global state directory");
                    break;
                case INCLUDE:
                    arglist.removeFirst();
                    String include = readWordOrThrow(arglist, "path to include");
                    wantsToIncludePaths.add(include);
                    break;
                default:
                    nonincludes.addLast(arglist.removeFirst());
            }
        }
        this.statedirs = (this.statedirs != null ? this.statedirs : NB_STATEDIR_PATHS);
        this.setStatePath();

        arglist = nonincludes;
        nonincludes = new LinkedList<>();

        // Now that statdirs is settled, auto load argsfile if it is present
        NBCLIArgsFile argsfile = new NBCLIArgsFile();
        argsfile.reserved(NBCLICommandParser.RESERVED_WORDS);
        argsfile.preload("--argsfile-optional", ARGS_FILE_DEFAULT);
        arglist = argsfile.process(arglist);

        // Parse all --argsfile... and other high level options

        while (arglist.peekFirst() != null) {
            String word = arglist.peekFirst();
            if (word.startsWith("--") && word.contains("=")) {
                String wordToSplit = arglist.removeFirst();
                String[] split = wordToSplit.split("=", 2);
                arglist.offerFirst(split[1]);
                arglist.offerFirst(split[0]);
                continue;
            }

            switch (word) {
                // These options modify other options. They should be processed early.
                case NBCLIArgsFile.ARGS_FILE:
                case NBCLIArgsFile.ARGS_FILE_OPTIONAL:
                case NBCLIArgsFile.ARGS_FILE_REQUIRED:
                case NBCLIArgsFile.ARGS_PIN:
                case NBCLIArgsFile.ARGS_UNPIN:
                    if (this.statepath == null) {
                        setStatePath();
                    }
                    arglist = argsfile.process(arglist);
                    break;
                case ANSI:
                    arglist.removeFirst();
                    String doEnableAnsi = readWordOrThrow(arglist, "enable/disable ansi codes");
                    enableAnsi=doEnableAnsi.toLowerCase(Locale.ROOT).matches("enabled|enable|true");
                    break;
                case DASH_V_INFO:
                    consoleLevel = NBLogLevel.INFO;
                    arglist.removeFirst();
                    break;
                case DASH_VV_DEBUG:
                    consoleLevel = NBLogLevel.DEBUG;
                    setWantsStackTraces(true);
                    arglist.removeFirst();
                    break;
                case DASH_VVV_TRACE:
                    consoleLevel = NBLogLevel.TRACE;
                    setWantsStackTraces(true);
                    arglist.removeFirst();
                    break;
                case ANNOTATE_EVENTS:
                    arglist.removeFirst();
                    String toAnnotate = readWordOrThrow(arglist, "annotated events");
                    annotateEvents = toAnnotate.split("\\\\s*,\\\\s*");
                    break;
                case ANNOTATORS_CONFIG:
                    arglist.removeFirst();
                    this.annotatorsConfig = readWordOrThrow(arglist, "annotators config");
                    break;
                case REPORT_GRAPHITE_TO:
                    arglist.removeFirst();
                    reportGraphiteTo = arglist.removeFirst();
                    break;
                case GRAPHITE_LOG_LEVEL:
                    arglist.removeFirst();
                    graphitelogLevel=arglist.removeFirst();
                    break;
                case METRICS_PREFIX:
                    arglist.removeFirst();
                    metricsPrefix = arglist.removeFirst();
                    break;
                case WORKSPACES_DIR:
                    arglist.removeFirst();
                    workspacesDirectory = readWordOrThrow(arglist, "a workspaces directory");
                    break;
                case DOCKER_PROM_TAG:
                    arglist.removeFirst();
                    docker_prom_tag = readWordOrThrow(arglist, "prometheus docker tag");
                    break;
                case DOCKER_PROM_RETENTION_DAYS:
                    arglist.removeFirst();
                    dockerPromRetentionDays = readWordOrThrow(arglist, "prometheus retention (3650d by default)");
                    break;
                case DOCKER_GRAFANA_TAG:
                    arglist.removeFirst();
                    docker_grafana_tag = readWordOrThrow(arglist, "grafana docker tag");
                    break;
                case VERSION:
                    arglist.removeFirst();
                    wantsVersionShort = true;
                    break;
                case VERSION_COORDS:
                    arglist.removeFirst();
                    wantsVersionCoords = true;
                    break;
                case DOCKER_METRICS_AT:
                    arglist.removeFirst();
                    dockerMetricsHost = readWordOrThrow(arglist, "docker metrics host");
                    break;
                case DOCKER_METRICS:
                    arglist.removeFirst();
                    dockerMetrics = true;
                    break;
                case SESSION_NAME:
                    arglist.removeFirst();
                    sessionName = readWordOrThrow(arglist, "a session name");
                    break;
                case LOGS_DIR:
                    arglist.removeFirst();
                    logsDirectory = readWordOrThrow(arglist, "a log directory");
                    break;
                case LOGS_MAX:
                    arglist.removeFirst();
                    logsMax = Integer.parseInt(readWordOrThrow(arglist, "max logfiles to keep"));
                    break;
                case LOGS_LEVEL:
                    arglist.removeFirst();
                    String loglevel = readWordOrThrow(arglist, "a log level");
                    this.logsLevel = NBLogLevel.valueOfName(loglevel);
                    break;
                case LOG_LEVEL_OVERRIDE:
                    arglist.removeFirst();
                    logLevelsOverrides = parseLogLevelOverrides(readWordOrThrow(arglist, "log levels in name:LEVEL,... format"));
                    break;
                case CONSOLE_PATTERN:
                    arglist.removeFirst();
                    consoleLoggingPattern =readWordOrThrow(arglist, "console pattern");
                    break;
                case LOGFILE_PATTERN:
                    arglist.removeFirst();
                    logfileLoggingPattern =readWordOrThrow(arglist, "logfile pattern");
                    break;
                case WITH_LOGGING_PATTERN:
                case LOGGING_PATTERN:
                    arglist.removeFirst();
                    String pattern = readWordOrThrow(arglist, "console and logfile pattern");
                    consoleLoggingPattern = pattern;
                    logfileLoggingPattern = pattern;
                    break;
                case SHOW_STACKTRACES:
                    arglist.removeFirst();
                    showStackTraces = true;
                    break;
                case EXPERIMENTAL:
                    arglist.removeFirst();
                    arglist.addFirst("experimental");
                    arglist.addFirst("--maturity");
                    break;
                case MATURITY:
                    arglist.removeFirst();
                    String maturity = readWordOrThrow(arglist,"maturity of components to allow");
                    this.minMaturity = Maturity.valueOf(maturity.toLowerCase(Locale.ROOT));
                default:
                    nonincludes.addLast(arglist.removeFirst());
            }
        }

        return nonincludes;
    }

    private Path setStatePath() {
        if (statePathAccesses.size() > 0) {
            throw new BasicError("The state dir must be set before it is used by other\n" +
                    " options. If you want to change the statedir, be sure you do it before\n" +
                    " dependent options. These parameters were called before this --statedir:\n" +
                    statePathAccesses.stream().map(s -> "> " + s).collect(Collectors.joining("\n")));
        }
        if (this.statepath != null) {
            return this.statepath;
        }

        List<String> paths = NBEnvironment.INSTANCE.interpolateEach(":", statedirs);
        Path selected = null;

        for (String pathName : paths) {
            Path path = Path.of(pathName);
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    selected = path;
                    break;
                } else {
                    System.err.println("ERROR: possible state dir path is not a directory: '" + path + "'");
                }
            }
        }
        if (selected == null) {
            selected = Path.of(paths.get(paths.size()-1));
        }

        if (!Files.exists(selected)) {
            try {
                Files.createDirectories(
                        selected,
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwx---"))
                );
            } catch (IOException e) {
                throw new BasicError("Could not create state directory at '" + selected + "': " + e.getMessage());
            }
        }

        NBEnvironment.INSTANCE.put(NBEnvironment.NBSTATEDIR, selected.toString());

        return selected;
    }

    private void parseAllOptions(String[] args) {
        LinkedList<String> arglist = parseGlobalOptions(args);

        PathCanonicalizer canonicalizer = new PathCanonicalizer(wantsIncludes());

        LinkedList<String> nonincludes = new LinkedList<>();

        while (arglist.peekFirst() != null) {
            String word = arglist.peekFirst();

            switch (word) {
                case GRAALJS_ENGINE:
                    engine = Scenario.Engine.Graalvm;
                    arglist.removeFirst();
                    break;
                case COMPILE_SCRIPT:
                    arglist.removeFirst();
                    compileScript = true;
                    break;
                case SHOW_SCRIPT:
                    arglist.removeFirst();
                    showScript = true;
                    break;
                case LIST_COMMANDS:
                    arglist.removeFirst();
                    this.wantsListCommands = true;
                    break;
                case LIST_METRICS:
                    arglist.removeFirst();
                    arglist.addFirst("start");
                    Cmd cmd = Cmd.parseArg(arglist, canonicalizer);
                    wantsMetricsForActivity = cmd.getArg("driver");
                    break;
                case HDR_DIGITS:
                    arglist.removeFirst();
                    hdr_digits = Integer.parseInt(readWordOrThrow(arglist, "significant digits"));
                    break;
                case PROGRESS:
                    arglist.removeFirst();
                    progressSpec = readWordOrThrow(arglist, "a progress indicator, like 'log:1m' or 'screen:10s', or just 'log' or 'screen'");
                    break;
                case ENABLE_CHART:
                    arglist.removeFirst();
                    enableChart = true;
                    break;
                case HELP:
                case "-h":
                case "help":
                    arglist.removeFirst();
                    if (arglist.peekFirst() == null) {
                        wantsBasicHelp = true;
                    } else {
                        wantsActivityHelp = true;
                        wantsActivityHelpFor = readWordOrThrow(arglist, "topic");
                    }
                    break;
                case EXPORT_CYCLE_LOG:
                    arglist.removeFirst();
                    rleDumpOptions = readAllWords(arglist);
                    break;
                case IMPORT_CYCLE_LOG:
                    arglist.removeFirst();
                    cyclelogImportOptions = readAllWords(arglist);
                    break;
                case LOG_HISTOGRAMS:
                    arglist.removeFirst();
                    String logto = arglist.removeFirst();
                    histoLoggerConfigs.add(logto);
                    break;
                case LOG_HISTOSTATS:
                    arglist.removeFirst();
                    String logStatsTo = arglist.removeFirst();
                    statsLoggerConfigs.add(logStatsTo);
                    break;
                case CLASSIC_HISTOGRAMS:
                    arglist.removeFirst();
                    String classicHistos = arglist.removeFirst();
                    classicHistoConfigs.add(classicHistos);
                    break;
                case REPORT_INTERVAL:
                    arglist.removeFirst();
                    reportInterval = Integer.parseInt(readWordOrThrow(arglist, "report interval"));
                    break;
                case REPORT_CSV_TO:
                    arglist.removeFirst();
                    reportCsvTo = arglist.removeFirst();
                    break;
                case REPORT_SUMMARY_TO:
                    arglist.removeFirst();
                    reportSummaryTo = readWordOrThrow(arglist, "report summary file");
                    break;
                case LIST_DRIVERS:
                case LIST_ACTIVITY_TYPES:
                    arglist.removeFirst();
                    wantsActivityTypes = true;
                    break;
                case LIST_INPUT_TYPES:
                    arglist.removeFirst();
                    wantsInputTypes = true;
                    break;
                case LIST_OUTPUT_TYPES:
                    arglist.removeFirst();
                    wantsMarkerTypes = true;
                    break;
                case LIST_SCENARIOS:
                    arglist.removeFirst();
                    wantsListScenarios = true;
                    break;
                case LIST_SCRIPTS:
                    arglist.removeFirst();
                    wantsListScripts = true;
                    break;
                case LIST_WORKLOADS:
                    arglist.removeFirst();
                    wantsWorkloadsList = true;
                    break;
                case LIST_APPS:
                    arglist.removeFirst();
                    wantsListApps= true;
                    break;
                case SCRIPT_FILE:
                    arglist.removeFirst();
                    scriptFile = readWordOrThrow(arglist, "script file");
                    break;
                case COPY:
                    arglist.removeFirst();
                    wantsToCopyWorkload = readWordOrThrow(arglist, "workload to copy");
                    break;
                default:
                    nonincludes.addLast(arglist.removeFirst());
            }
        }
        arglist = nonincludes;
        Optional<List<Cmd>> commands = NBCLICommandParser.parse(arglist);
        if (commands.isPresent()) {
            this.cmdList.addAll(commands.get());
        } else {
            String arg = arglist.peekFirst();
            Objects.requireNonNull(arg);
            String helpmsg = """
                Could not recognize command 'ARG'.
                This means that all of the following searches for a compatible command failed:
                1. commands: no scenario command named 'ARG' is known. (start, run, await, ...)
                2. scripts: no auto script named './scripts/auto/ARG.js' in the local filesystem.
                3. scripts: no auto script named 'scripts/auto/ARG.js' was found in the PROG binary.
                4. workloads: no workload file named ARG[.yaml] was found in the local filesystem, even in include paths INCLUDES.
                5. workloads: no workload file named ARG[.yaml] was bundled in PROG binary, even in include paths INCLUDES.
                6. apps: no application named ARG was bundled in PROG.

                You can discover available ways to invoke PROG by using the various --list-* commands:
                [ --list-commands, --list-scripts, --list-workloads (and --list-scenarios), --list-apps ]
                """
                .replaceAll("ARG",arg)
                .replaceAll("PROG","nb5")
                .replaceAll("INCLUDES", String.join(",",this.wantsIncludes()));
            throw new BasicError(helpmsg);

        }
    }


    public String[] wantsIncludes() {
        return wantsToIncludePaths.toArray(new String[0]);
    }

    private Map<String, String> parseLogLevelOverrides(String levelsSpec) {
        Map<String, String> levels = new HashMap<>();
        Arrays.stream(levelsSpec.split("[,;]")).forEach(kp -> {
            String[] ll = kp.split(":");
            if (ll.length != 2) {
                throw new RuntimeException("Log level must have name:level format");
            }
            levels.put(ll[0], ll[1]);
        });
        return levels;
    }

    public Scenario.Engine getScriptingEngine() {
        return engine;
    }

    public List<LoggerConfigData> getHistoLoggerConfigs() {
        List<LoggerConfigData> configs =
                histoLoggerConfigs.stream().map(LoggerConfigData::new).collect(Collectors.toList());
        checkLoggerConfigs(configs, LOG_HISTOGRAMS);
        return configs;
    }

    public List<LoggerConfigData> getStatsLoggerConfigs() {
        List<LoggerConfigData> configs =
                statsLoggerConfigs.stream().map(LoggerConfigData::new).collect(Collectors.toList());
        checkLoggerConfigs(configs, LOG_HISTOSTATS);
        return configs;
    }

    public List<LoggerConfigData> getClassicHistoConfigs() {
        List<LoggerConfigData> configs =
                classicHistoConfigs.stream().map(LoggerConfigData::new).collect(Collectors.toList());
        checkLoggerConfigs(configs, CLASSIC_HISTOGRAMS);
        return configs;
    }

    public Maturity allowMinMaturity() {
        return minMaturity;
    }

    public List<Cmd> getCommands() {
        return cmdList;
    }

    public boolean wantsShowScript() {
        return showScript;
    }

    public boolean wantsCompileScript() {
        return compileScript;
    }

    public boolean wantsVersionCoords() {
        return wantsVersionCoords;
    }

    public boolean isWantsVersionShort() {
        return wantsVersionShort;
    }

    public boolean wantsActivityTypes() {
        return wantsActivityTypes;
    }

    public boolean wantsTopicalHelp() {
        return wantsActivityHelp;
    }

    public boolean wantsStackTraces() {
        return showStackTraces;
    }

    public String wantsTopicalHelpFor() {
        return wantsActivityHelpFor;
    }

    public boolean wantsBasicHelp() {
        return wantsBasicHelp;
    }

    public boolean wantsEnableChart() {
        return enableChart;
    }

    public boolean wantsDockerMetrics() {
        return dockerMetrics;
    }

    public String wantsDockerMetricsAt() {
        return dockerMetricsHost;
    }

    public int getReportInterval() {
        return reportInterval;
    }

    public String wantsReportGraphiteTo() {
        return reportGraphiteTo;
    }

    public String wantsMetricsPrefix() {
        return metricsPrefix;
    }

    public String wantsMetricsForActivity() {
        return wantsMetricsForActivity;
    }

    public String getSessionName() {
        return sessionName;
    }

    public NBLogLevel getConsoleLogLevel() {
        return consoleLevel;
    }

    private String readWordOrThrow(LinkedList<String> arglist, String required) {
        if (arglist.peekFirst() == null) {
            throw new InvalidParameterException(required + " is required after this option");
        }
        return arglist.removeFirst();
    }

    private String[] readAllWords(LinkedList<String> arglist) {
        String[] args = arglist.toArray(new String[0]);
        arglist.clear();
        return args;
    }

    public int getHdrDigits() {
        return hdr_digits;
    }

    public String getProgressSpec() {
        ProgressSpec spec = parseProgressSpec(this.progressSpec);// sanity check
        if (spec.indicatorMode == IndicatorMode.console) {
            if (getConsoleLogLevel().isGreaterOrEqualTo(NBLogLevel.INFO)) {
//                System.err.println("Console is already logging info or more, so progress data on console is " +
//                        "suppressed.");
                spec.indicatorMode = IndicatorMode.logonly;
            } else if (this.getCommands().stream().anyMatch(cmd -> cmd.getCmdType().equals(Cmd.CmdType.script))) {
//                System.err.println("Command line includes script calls, so progress data on console is " +
//                        "suppressed.");
                spec.indicatorMode = IndicatorMode.logonly;
            }
        }
        return spec.toString();
    }

    private void checkLoggerConfigs(List<LoggerConfigData> configs, String configName) {
        Set<String> files = new HashSet<>();
        configs.stream().map(LoggerConfigData::getFilename).forEach(s -> {
            if (files.contains(s)) {
                System.err.println(s + " is included in " + configName + " more than once. It will only be " +
                        "included " +
                        "in the first matching config. Reorder your options if you need to control this.");
            }
            files.add(s);
        });
    }

    public String wantsReportCsvTo() {
        return reportCsvTo;
    }

    public Path getLogsDirectory() {
        return Path.of(logsDirectory);
    }

    public int getLogsMax() {
        return logsMax;
    }

    public NBLogLevel getScenarioLogLevel() {
        return logsLevel;
    }

    public boolean wantsInputTypes() {
        return this.wantsInputTypes;
    }

    public String getScriptFile() {
        if (scriptFile == null) {
            return logsDirectory + File.separator + "_SESSION_" + ".js";
        }

        String expanded = scriptFile;
        if (!expanded.startsWith(File.separator)) {
            expanded = getLogsDirectory() + File.separator + expanded;
        }
        return expanded;
    }

    public boolean wantsMarkerTypes() {
        return wantsMarkerTypes;
    }

    public boolean wantsToDumpCyclelog() {
        return rleDumpOptions.length > 0;
    }

    public boolean wantsToImportCycleLog() {
        return cyclelogImportOptions.length > 0;
    }

    public String[] getCyclelogImportOptions() {
        return cyclelogImportOptions;
    }

    public String[] getCycleLogExporterOptions() {
        return rleDumpOptions;
    }

    public String getConsoleLoggingPattern() {
        return consoleLoggingPattern;
    }

    public Map<String, String> getLogLevelOverrides() {
        return logLevelsOverrides;
    }

    public void setHistoLoggerConfigs(String pattern, String file, String interval) {
        //--log-histograms 'hdrdata.log:.*:2m'
        histoLoggerConfigs.add(String.format("%s:%s:%s", file, pattern, interval));
    }

    public boolean wantsScenariosList() {
        return wantsListScenarios;
    }

    public boolean wantsListScripts() {
        return wantsListScripts;
    }

    public boolean wantsToCopyResource() {
        return wantsToCopyWorkload != null;
    }

    public String wantsToCopyResourceNamed() {
        return wantsToCopyWorkload;
    }

    public boolean wantsWorkloadsList() {
        return wantsWorkloadsList;
    }

    public String getDockerGrafanaTag() {
        return docker_grafana_tag;
    }

    public String getDockerPromTag() {
        return docker_prom_tag;
    }

    public static class LoggerConfigData {
        public String file;
        public String pattern = ".*";
        public String interval = "30 seconds";

        public LoggerConfigData(String histoLoggerSpec) {
            String[] words = histoLoggerSpec.split(":");
            switch (words.length) {
                case 3:
                    interval = words[2].isEmpty() ? interval : words[2];
                case 2:
                    pattern = words[1].isEmpty() ? pattern : words[1];
                case 1:
                    file = words[0];
                    if (file.isEmpty()) {
                        throw new RuntimeException("You must not specify an empty file here for logging data.");
                    }
                    break;
                default:
                    throw new RuntimeException(
                            LOG_HISTOGRAMS +
                                    " options must be in either 'regex:filename:interval' or 'regex:filename' or 'filename' format"
                    );
            }
        }

        public String getFilename() {
            return file;
        }
    }

    private static class ProgressSpec {
        public String intervalSpec;
        public IndicatorMode indicatorMode;

        public String toString() {
            return indicatorMode.toString() + ":" + intervalSpec;
        }
    }

    private ProgressSpec parseProgressSpec(String interval) {
        ProgressSpec progressSpec = new ProgressSpec();
        String[] parts = interval.split(":");
        switch (parts.length) {
            case 2:
                Unit.msFor(parts[1]).orElseThrow(
                        () -> new RuntimeException("Unable to parse progress indicator indicatorSpec '" + parts[1] + "'")
                );
                progressSpec.intervalSpec = parts[1];
            case 1:
                progressSpec.indicatorMode = IndicatorMode.valueOf(parts[0]);
                break;
            default:
                throw new RuntimeException("This should never happen.");
        }
        return progressSpec;
    }

}
