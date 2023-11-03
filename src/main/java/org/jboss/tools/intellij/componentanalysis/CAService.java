/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.componentanalysis;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.redhat.exhort.api.AnalysisReport;
import com.redhat.exhort.api.DependencyReport;
import com.redhat.exhort.api.ProviderReport;
import com.redhat.exhort.api.Source;
import org.jboss.tools.intellij.exhort.ApiService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class CAService {

    public static CAService getInstance() {
        return ServiceManager.getService(CAService.class);
    }

    private final Cache<String, Map<Dependency, Map<VulnerabilitySource, DependencyReport>>> vulnerabilityCache = Caffeine.newBuilder()
            .maximumSize(100)
            .build();

    private final Cache<String, Set<Dependency>> dependencyCache = Caffeine.newBuilder()
            .expireAfterWrite(25, TimeUnit.SECONDS)
            .maximumSize(100)
            .build();

    public static Map<Dependency, Map<VulnerabilitySource, DependencyReport>> getReports(String filePath) {
        return Collections.unmodifiableMap(getInstance().vulnerabilityCache.get(filePath, p -> Collections.emptyMap()));
    }

    public static void deleteReports(String filePath) {
        getInstance().vulnerabilityCache.invalidate(filePath);
    }

    public static boolean dependenciesModified(String filePath, Set<Dependency> dependencies) {
        return !dependencies.equals(getInstance().dependencyCache.get(filePath, p -> Collections.emptySet()));
    }

    public static boolean performAnalysis(String packageManager,
                                          String fileName,
                                          String filePath,
                                          Set<Dependency> dependencies) {
        if (dependenciesModified(filePath, dependencies)) {
            ApiService apiService = ServiceManager.getService(ApiService.class);
            AnalysisReport report = apiService.getComponentAnalysis(packageManager, fileName, filePath);
            if (report == null) {
                throw new RuntimeException("Failed to perform component analysis, result is invalid.");
            }

            Map<Dependency, Map<VulnerabilitySource, DependencyReport>> resultMap = new ConcurrentHashMap<>();

            if (report.getProviders() != null) {
                // Avoid comparing the version of dependency
                Map<Dependency, Dependency> dependencyMap = dependencies
                        .parallelStream()
                        .collect(Collectors.toMap(
                                Function.identity(),
                                d -> new Dependency(d, false),
                                (o1, o2) -> o1
                        ));


                report.getProviders().entrySet()
                        .parallelStream()
                        .filter(pe -> {
                            ProviderReport providerReport = pe.getValue();
                            return providerReport.getStatus() != null
                                    && Boolean.TRUE.equals(providerReport.getStatus().getOk())
                                    && providerReport.getSources() != null;
                        })
                        .forEach(pe -> {
                            String providerName = pe.getKey();
                            ProviderReport providerReport = pe.getValue();

                            if (providerReport.getSources() != null) {
                                providerReport.getSources().entrySet()
                                        .parallelStream()
                                        .filter(se -> {
                                            Source source = se.getValue();
                                            return source.getDependencies() != null;
                                        })
                                        .forEach(se -> {
                                            String sourceName = se.getKey();
                                            Source source = se.getValue();

                                            if (source.getDependencies() != null && !source.getDependencies().isEmpty()) {
                                                Map<Dependency, DependencyReport> sourceReportMap = source.getDependencies()
                                                        .parallelStream()
                                                        .filter(r -> Objects.nonNull(r.getRef()))
                                                        .collect(Collectors.toMap(
                                                                r -> new Dependency(r.getRef().purl(), false),
                                                                Function.identity(),
                                                                (o1, o2) -> o1
                                                        ));

                                                dependencyMap.entrySet()
                                                        .parallelStream()
                                                        .filter(e -> sourceReportMap.containsKey(e.getValue()))
                                                        .forEach(e -> {
                                                            Dependency d = e.getKey();
                                                            DependencyReport dr = sourceReportMap.get(e.getValue());
                                                            resultMap.computeIfAbsent(d, key -> new ConcurrentHashMap<>())
                                                                    .put(new VulnerabilitySource(providerName, sourceName), dr);
                                                        });
                                            }
                                        });
                            }
                        });
            }

            if (!resultMap.isEmpty()) {
                getInstance().vulnerabilityCache.put(filePath, resultMap);
            } else {
                getInstance().vulnerabilityCache.invalidate(filePath);
            }

            getInstance().dependencyCache.put(filePath, dependencies);
            return true;
        }
        return false;
    }
}
