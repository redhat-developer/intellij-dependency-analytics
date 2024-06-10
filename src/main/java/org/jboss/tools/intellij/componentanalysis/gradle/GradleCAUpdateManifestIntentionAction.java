package org.jboss.tools.intellij.componentanalysis.gradle;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.redhat.exhort.api.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAUpdateManifestIntentionAction;
import org.jboss.tools.intellij.componentanalysis.gradle.build.filetype.BuildGradleFileType;
import org.jboss.tools.intellij.componentanalysis.gradle.build.psi.BuildGradleFile;
import org.jboss.tools.intellij.componentanalysis.gradle.build.psi.BuildGradleTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GradleCAUpdateManifestIntentionAction extends CAUpdateManifestIntentionAction {
    @Override
    protected String getTextImpl() {
        return "Add Redhat GA Maven Repository to your build.gradle";
    }

    GradleCAUpdateManifestIntentionAction(PsiElement element, DependencyReport report) {
        super(element, report);
    }

    @Override
    protected void updateManifest(Project project, Editor editor, PsiFile file, DependencyReport dependency) {
        PsiElement repositories = Arrays.stream(file.getChildren()).filter(psi -> psi instanceof LeafPsiElement)
                .filter(psi -> ((LeafPsiElement) psi).getElementType().equals(BuildGradleTypes.REPOSITORIES.toString())).findFirst().get();
        String repositoriesBlock = repositories.getText();
        int lastRightCurlyBracket = repositoriesBlock.lastIndexOf("}");
        repositoriesBlock = repositoriesBlock.substring(0,lastRightCurlyBracket);
        StringBuilder updatedRepository = new StringBuilder(repositoriesBlock);
        if(!repositoriesBlock.endsWith(System.lineSeparator())) {
            updatedRepository.append(System.lineSeparator());
        }
        String repositoryUrl = getRepositoryUrl(dependency);

        updatedRepository.append(String.format("    maven { %s     url \"%s\"%s    }%s}",System.lineSeparator(),repositoryUrl,System.lineSeparator(),System.lineSeparator()));
        BuildGradleFile dummyFile = (BuildGradleFile) PsiFileFactory.getInstance(project).createFileFromText("dummy-build.gradle", BuildGradleFileType.INSTANCE, updatedRepository.toString());
        PsiElement modifiedRepositories = Arrays.stream(dummyFile.getChildren()).findFirst().get();
        repositories.replace(modifiedRepositories);

    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }
}
