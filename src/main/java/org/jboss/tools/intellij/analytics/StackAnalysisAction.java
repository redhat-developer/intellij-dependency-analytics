package org.jboss.tools.intellij.analytics;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;


public class StackAnalysisAction extends AnAction {
    private static final Logger log = Logger.getInstance(StackAnalysisAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // Logic to Run SA
        log.warn("Class:PopupDialogAction_metho:actionPerformed_:_Called.");
        log.warn("Manifest File Location= "+ event.getData(PlatformDataKeys.VIRTUAL_FILE).getPath());
        log.warn("Action Location= "+ event.getPlace());
    }

    @Override
    public void update(AnActionEvent event) {
        // Set supported file extensions
        List<String> extensions = Arrays.asList("xml", "json", "txt");
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);

        // Check if file where context menu is opened is type of supported extension.
        // If yes then show the action for SA in menu
        if (psiFile != null) {
            event.getPresentation().setEnabledAndVisible(extensions
                    .contains(psiFile.getFileType().getDefaultExtension()));
        }
    }
}