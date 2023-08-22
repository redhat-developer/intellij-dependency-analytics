package org.jboss.tools.intellij.exhort;

import com.intellij.openapi.components.Service;
import com.redhat.exhort.Api;
import com.redhat.exhort.impl.ExhortApi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

@Service(Service.Level.PROJECT)
public final class ApiService {
    enum TelemetryKeys {
        MANIFEST, ECOSYSTEM, PLATFORM;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    private final Api exhortApi;

    public ApiService() {
        this(new ExhortApi());
    }

    ApiService(Api exhortApi) {
        this.exhortApi = exhortApi;
    }

    public Path getStackAnalysis(
            final String packageManager,
            final String manifestName,
            final String manifestPath
    ) throws RuntimeException {

        var telemetryMsg = TelemetryService.instance().action("stack-analysis");
        telemetryMsg.property(TelemetryKeys.ECOSYSTEM.toString(), packageManager);
        telemetryMsg.property(TelemetryKeys.PLATFORM.toString(), System.getProperty("os.name"));
        telemetryMsg.property(TelemetryKeys.MANIFEST.toString(), manifestName);

        try {
            var htmlContent = exhortApi.stackAnalysisHtml(manifestPath);
            var tmpFile = Files.createTempFile("exhort_", ".html");
            Files.write(tmpFile, htmlContent.get());

            telemetryMsg.send();
            return tmpFile;

        } catch (IOException | InterruptedException | ExecutionException exc) {
            telemetryMsg.error(exc);
            telemetryMsg.send();
            throw new RuntimeException(exc);
        }
    }
}
