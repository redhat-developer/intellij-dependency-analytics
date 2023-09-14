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
import org.jboss.tools.intellij.exhort.ApiService;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class CAService {

    public static CAService getInstance() {
        return ServiceManager.getService(CAService.class);
    }

    private final Cache<String, Map<Dependency, DependencyReport>> vulnerabilityCache = Caffeine.newBuilder()
            .maximumSize(100)
            .build();

    private final Cache<String, Set<Dependency>> dependencyCache = Caffeine.newBuilder()
            .expireAfterWrite(25, TimeUnit.SECONDS)
            .maximumSize(100)
            .build();

    public static Map<Dependency, DependencyReport> getReports(String filePath) {
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
            if (report.getDependencies() != null) {
                // Avoid comparing the version of dependency
                Map<Dependency, Dependency> dependencyMap = Collections.unmodifiableMap(
                        dependencies
                                .parallelStream()
                                .collect(Collectors.toMap(
                                        Function.identity(),
                                        d -> new Dependency(d, false),
                                        (o1, o2) -> o1
                                ))
                );

                Map<Dependency, DependencyReport> reportMap = Collections.unmodifiableMap(
                        report.getDependencies()
                                .parallelStream()
                                .filter(r -> Objects.nonNull(r.getRef()))
                                .collect(Collectors.toMap(
                                        r -> new Dependency(r.getRef().purl(), false),
                                        Function.identity(),
                                        (o1, o2) -> o1
                                ))
                );

                Map<Dependency, DependencyReport> resultMap = Collections.unmodifiableMap(
                        dependencyMap.entrySet()
                                .parallelStream()
                                .filter(e -> reportMap.containsKey(e.getValue()))
                                .map(e -> {
                                    DependencyReport dp = reportMap.get(e.getValue());
                                    return new AbstractMap.SimpleEntry<>(e.getKey(), dp);
                                })
                                .collect(Collectors.toMap(
                                        AbstractMap.SimpleEntry::getKey,
                                        AbstractMap.SimpleEntry::getValue,
                                        (o1, o2) -> o1
                                ))
                );

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
