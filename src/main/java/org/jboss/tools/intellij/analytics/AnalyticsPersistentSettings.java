package org.jboss.tools.intellij.analytics;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

@Service
@State(
    name = "AnalyticsPersistentSettings",
    storages = {
      @Storage(file = "analytics.settings.xml", roamingType = RoamingType.DISABLED)
})
public final class AnalyticsPersistentSettings implements PersistentStateComponent<AnalyticsPersistentSettings> {

    private String lspVersion;

    @Override
    public AnalyticsPersistentSettings getState() {
      return this;
    }

    @Override
    public void loadState(AnalyticsPersistentSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void setLSPVersion(final String version) {
      this.lspVersion = version;
    }

    public String getLSPVersion() {
      return this.lspVersion;
    }
}
