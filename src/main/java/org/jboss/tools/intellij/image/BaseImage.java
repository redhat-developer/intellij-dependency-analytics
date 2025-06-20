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

import java.util.Objects;

public class BaseImage {
    private final String imageName;
    private final String platform;

    public BaseImage(String imageName, String platform) {
        this.imageName = imageName;
        this.platform = platform;
    }

    public String getImageName() {
        return imageName;
    }

    public String getPlatform() {
        return platform;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseImage baseImage = (BaseImage) o;
        return Objects.equals(imageName, baseImage.imageName) && Objects.equals(platform, baseImage.platform);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageName, platform);
    }

    @Override
    public String toString() {
        return "BaseImage{" +
                "imageName='" + imageName + '\'' +
                ", platform='" + platform + '\'' +
                '}';
    }
}
