package org.jboss.tools.intellij.analytics;

import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class PlatformTest {

  @Test
  public void testDetectPlatform() {
    final Properties props = new Properties();

    props.setProperty("os.name", "windows");
    assertEquals(Platform.WINDOWS, Platform.detect(props));

    props.setProperty("os.name", "linux");
    assertEquals(Platform.LINUX, Platform.detect(props));

    props.setProperty("os.name", "osx");
    assertEquals(Platform.MACOS, Platform.detect(props));

    props.setProperty("os.name", "darwin");
    assertEquals(Platform.MACOS, Platform.detect(props));

    props.setProperty("os.name", "mac os x");
    assertEquals(Platform.MACOS, Platform.detect(props));
  }

  @Test(expected = PlatformDetectionException.class)
  public void testPlatformDetectionException() {
    final Properties props = new Properties();
    props.setProperty("os.name", "random");
    Platform.detect(props);
  }
}
