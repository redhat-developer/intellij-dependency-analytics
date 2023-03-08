package org.jboss.tools.intellij.analytics;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.MapAnnotation;

import java.util.Map;
import java.util.HashMap;

@Service
@State(
    name = "Settings",
    storages = {
      @Storage(value = "analytics.settings.xml", roamingType = RoamingType.DISABLED)
})
public final class Settings implements ICookie, PersistentStateComponent<Settings> {

    // str representation of ICookie.Name values are key.
    private Map<String, String> settings = new HashMap();

    @Override
    public Settings getState() {
      return this;
    }

    @Override
    public void loadState(Settings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public void setValue(ICookie.Name name, String value) {
        this.settings.put(name.name(), value);
    }

    @Override
    public String getValue(ICookie.Name name) {
      return this.settings.getOrDefault(name.name(), "");
    }

    @MapAnnotation
    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }
}
