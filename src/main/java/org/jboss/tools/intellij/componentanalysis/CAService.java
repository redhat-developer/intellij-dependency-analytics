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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.redhat.exhort.api.v4.AnalysisReport;
import com.redhat.exhort.api.v4.DependencyReport;
import com.redhat.exhort.api.v4.ProviderReport;
import com.redhat.exhort.api.v4.Source;
import org.apache.commons.io.FileUtils;
import org.jboss.tools.intellij.exhort.ApiService;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Service(Service.Level.PROJECT)
public final class CAService {

    private static final Logger LOG = Logger.getInstance(CAService.class);

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
                                          Set<Dependency> dependencies,
                                          PsiFile file) {
        if (dependenciesModified(filePath, dependencies)) {
            Path tempManifest;
            Path tempDirectory;
            ApiService apiService = ServiceManager.getService(ApiService.class);
            try {
                tempDirectory = Files.createTempDirectory("rhda-idea");
                tempManifest = Files.createFile(Path.of(tempDirectory.toString(),fileName));
                Files.write(tempManifest,PsiDocumentManager.getInstance(file.getProject()).getCachedDocument(file).getText().getBytes());

                if (packageManager.equals("npm")) {
                    Path parentDir = Path.of(filePath).getParent();
                    List<String> lockFiles = new ArrayList<>();
                    if (Files.exists(parentDir.resolve("package-lock.json"))) {
                        lockFiles.add("package-lock.json");
                    }
                    if (Files.exists(parentDir.resolve("pnpm-lock.yaml"))) {
                        lockFiles.add("pnpm-lock.yaml");
                    }
                    if (Files.exists(parentDir.resolve("yarn.lock"))) {
                        lockFiles.add("yarn.lock");
                    }
                    // Check the number of lockfiles
                    if (lockFiles.size() > 1) {
                        throw new RuntimeException("Multiple lockfiles detected: " + String.join(", ", lockFiles));
                    } else if (lockFiles.size() == 1) {
                        // Copy the single lockfile to tempDirectory
                        Path lockFilePath = parentDir.resolve(lockFiles.get(0));
                        Path targetLockFilePath = tempDirectory.resolve(lockFiles.get(0));
                        Files.copy(lockFilePath, targetLockFilePath);
                    } else {
                        throw new RuntimeException("No lockfile found. Please generate a lockfile (package-lock.json, pnpm-lock.yaml, or yarn.lock).");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

//            AnalysisReport report = apiService.getComponentAnalysis(packageManager, fileName, filePath);
            AnalysisReport report = apiService.getComponentAnalysis(packageManager, fileName, tempManifest.toString());
            deleteTempDir(tempDirectory);
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

    private static void deleteTempDir(Path tempDirectory)  {
        try {
            FileUtils.deleteDirectory(tempDirectory.toFile());
        } catch (IOException e) {
            LOG.warn("Failed to delete temp directory: " + tempDirectory, e);
        }
    }


}
