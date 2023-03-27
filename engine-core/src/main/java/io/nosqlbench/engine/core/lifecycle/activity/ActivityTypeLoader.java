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

package io.nosqlbench.engine.core.lifecycle.activity;

import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.StandardActivityType;
import io.nosqlbench.nb.annotations.Maturity;
import io.nosqlbench.api.system.NBEnvironment;
import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.api.spi.SimpleServiceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ActivityTypeLoader {

    private static final Logger logger = LogManager.getLogger(ActivityTypeLoader.class);
    private final SimpleServiceLoader<ActivityType> ACTIVITYTYPE_SPI_FINDER = new SimpleServiceLoader<ActivityType>(ActivityType.class, Maturity.Any);
    private final SimpleServiceLoader<DriverAdapter> DRIVERADAPTER_SPI_FINDER = new SimpleServiceLoader<>(DriverAdapter.class, Maturity.Any);
    private final Set<URL> jarUrls = new HashSet<>();

    public ActivityTypeLoader setMaturity(Maturity maturity) {
        ACTIVITYTYPE_SPI_FINDER.setMaturity(maturity);
        return this;
    }

    public ActivityTypeLoader() {

        List<String> libpaths = NBEnvironment.INSTANCE.interpolateEach(":", "$" + NBEnvironment.NBLIBS);
        Set<URL> urlsToAdd = new HashSet<>();

        for (String libpaths_entry : libpaths) {
            Path libpath = Path.of(libpaths_entry);
            if (Files.isDirectory(libpath)) {
                urlsToAdd = addLibDir(urlsToAdd, libpath);
            } else if (Files.isRegularFile(libpath) && libpath.toString().toLowerCase().endsWith(".zip")) {
                urlsToAdd = addZipDir(urlsToAdd, libpath);
            } else if (Files.isRegularFile(libpath) && libpath.toString().toLowerCase().endsWith(".jar")) {
                urlsToAdd = addJarFile(urlsToAdd, libpath);
            }
        }
        extendClassLoader(urlsToAdd);
    }

    private synchronized void extendClassLoader(String... paths) {
        Set<URL> urls = new HashSet<>();
        for (String path : paths) {
            URL url = null;
            try {
                url = new URL(path);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            urls.add(url);
        }
        extendClassLoader(urls);
    }

    private synchronized void extendClassLoader(Set<URL> urls) {
        Set<URL> newUrls = new HashSet<>();
        if (!jarUrls.containsAll(urls)) {
            for (URL url : urls) {
                if (!jarUrls.contains(url)) {
                    newUrls.add(url);
                    jarUrls.add(url);
                }
            }
            URL[] newUrlAry = newUrls.toArray(new URL[]{});
            URLClassLoader ucl = URLClassLoader.newInstance(newUrlAry, Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(ucl);
            logger.debug("Extended class loader layering with " + newUrls);
        } else {
            logger.debug("All URLs specified were already in a class loader.");
        }
    }

    private Set<URL> addJarFile(Set<URL> urls, Path libpath) {
        try {
            urls.add(libpath.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return urls;
    }

    private Set<URL> addZipDir(Set<URL> urlsToAdd, Path libpath) {
        return urlsToAdd;
    }

    private Set<URL> addLibDir(Set<URL> urlsToAdd, Path libpath) {
        Set<URL> urls = NBIO.local()
            .searchPrefixes(libpath.toString())
            .extensionSet(".jar")
            .list().stream().map(Content::getURL)
            .collect(Collectors.toSet());
        urlsToAdd.addAll(urls);
        return urlsToAdd;
    }

    public Optional<ActivityType> load(ActivityDef activityDef) {

        final String driverName = activityDef.getParams()
            .getOptionalString("driver", "type")
            .orElseThrow(() -> new BasicError("The parameter 'driver=' is required."));

        activityDef.getParams()
            .getOptionalString("jar")
            .map(jar -> {
                Set<URL> urls = NBIO.local().search(jar)
                    .list()
                    .stream().map(Content::getURL)
                    .collect(Collectors.toSet());
                return urls;
            })
            .ifPresent(this::extendClassLoader);

        return this.getDriverAdapter(driverName,activityDef)
            .or(() -> ACTIVITYTYPE_SPI_FINDER.getOptionally(driverName));

    }

    private Optional<ActivityType> getDriverAdapter(String activityTypeName, ActivityDef activityDef) {
        Optional<DriverAdapter> oda = DRIVERADAPTER_SPI_FINDER.getOptionally(activityTypeName);

        if (oda.isPresent()) {
            DriverAdapter<?, ?> driverAdapter = oda.get();

            ActivityType activityType = new StandardActivityType<>(driverAdapter, activityDef);
            return Optional.of(activityType);
        } else {
            return Optional.empty();
        }
    }

    public Set<String> getAllSelectors() {
        Map<String, Maturity> allSelectors = ACTIVITYTYPE_SPI_FINDER.getAllSelectors();
        Map<String, Maturity> addAdapters = DRIVERADAPTER_SPI_FINDER.getAllSelectors();
        Set<String> all = new LinkedHashSet<>();
        all.addAll(allSelectors.keySet());
        all.addAll(addAdapters.keySet());
        return all;
    }
}
