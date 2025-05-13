/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.image;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.redhat.exhort.api.v4.AnalysisReport;
import com.redhat.exhort.image.ImageRef;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class ImageCacheService {

    private final Cache<BaseImage, AnalysisReport> reportCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    private final Cache<BaseImage, ImageRef> imageCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    static ImageCacheService getInstance(Project project) {
        return project.getService(ImageCacheService.class);
    }

    Map<BaseImage, AnalysisReport> getReports(Collection<BaseImage> images) {
        return this.reportCache.getAllPresent(images);
    }

    List<BaseImage> getImagesWithoutReport(Collection<BaseImage> images) {
        var existImageRefs = this.reportCache.getAllPresent(images).keySet();
        return images.stream().filter(imageRef -> !existImageRefs.contains(imageRef)).collect(Collectors.toList());
    }

    void deleteReports(final Collection<BaseImage> images) {
        reportCache.invalidateAll(images);
    }

    void cacheReports(final Map<BaseImage, AnalysisReport> reports) {
        reportCache.putAll(reports);
    }

    Map<BaseImage, ImageRef> getImages(Collection<BaseImage> images) {
        return this.imageCache.getAllPresent(images);
    }

    void deleteImages(final Collection<BaseImage> images) {
        imageCache.invalidateAll(images);
    }

    void cacheImages(final Map<BaseImage, ImageRef> images) {
        imageCache.putAll(images);
    }
}
