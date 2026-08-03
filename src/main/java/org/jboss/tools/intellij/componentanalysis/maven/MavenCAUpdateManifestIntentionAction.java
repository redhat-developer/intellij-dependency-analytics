package org.jboss.tools.intellij.componentanalysis.maven;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import io.github.guacsec.trustifyda.api.v5.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAUpdateManifestIntentionAction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public final class MavenCAUpdateManifestIntentionAction extends CAUpdateManifestIntentionAction {
    @Override
    protected String getTextImpl() {
        String repoUrl = getRepositoryUrl(this.dependency);
        return "Add " + getRepositoryDisplayName(repoUrl) + " to your pom.xml";
    }

    MavenCAUpdateManifestIntentionAction(PsiElement element, DependencyReport report) {
        super(element, report);
    }

    @Override
    protected void updateManifest(Project project, Editor editor, PsiFile file, DependencyReport dependency) {
        PsiElement rootProjectElement = getRootPomElement(file);
        XmlTag repositories = new XmlTagImpl();
        XmlTag rootProjectXml = (XmlTag)rootProjectElement;
        if(Arrays.stream(rootProjectElement.getChildren()).noneMatch(psi -> psi instanceof XmlTag && ((XmlTag)psi).getName().equals("repositories"))) {

            repositories = rootProjectXml.createChildTag("repositories",rootProjectXml.getNamespace(),"",false);
            repositories.setName("repositories");

        }
        else {
            Optional<PsiElement> repoWrapper = Arrays.stream(rootProjectElement.getChildren()).filter(psi -> psi instanceof XmlTag && ((XmlTag) psi).getName().equals("repositories")).findFirst();
            if(repoWrapper.isPresent()) {
                repositories = (XmlTag)repoWrapper.get();
            }
        }

        XmlTag repository = repositories.createChildTag("repository", repositories.getNamespace(), "", false);
        repository.setName("repository");

        String repoUrl = getRepositoryUrl(dependency);
        String repoId = getRepositoryId(repoUrl);
        String repoName = getRepositoryDisplayName(repoUrl);

        XmlTag id = repository.createChildTag("id", repository.getNamespace(), repoId, false);
        XmlTag name = repository.createChildTag("name", repository.getNamespace(), repoName, false);
        XmlTag url = repository.createChildTag("url", repository.getNamespace(), repoUrl, false);
        id.setName("id");
        name.setName("name");
        url.setName("url");
        id.getValue().setText(repoId);
        name.getValue().setText(repoName);
        url.getValue().setText(repoUrl);
        repository.addSubTag(id,false);
        repository.addSubTag(name,false);
        repository.addSubTag(url,false);
        // only after subtags created and populated, add them as subtags, so outer tags will be populated with their values.
        repositories.addSubTag(repository,true);
        rootProjectXml.addSubTag(repositories,true);

    }

    @NotNull
    private static PsiElement getRootPomElement(PsiFile file) {
        PsiElement rootProjectElement = Arrays.stream(file.getChildren())
                .filter(element -> element instanceof XmlDocument)
                .flatMap(element -> Arrays.stream(element.getChildren()))
                .filter(element -> element instanceof XmlTag && "project".equals(((XmlTag) element).getName())).findFirst().get();
        return rootProjectElement;
    }


    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        boolean manifestIsPomXml = file != null && "pom.xml".equals(file.getName());
        boolean repositoryInPom = false;
        PsiElement rootPomElement = getRootPomElement(file);
        Optional<PsiElement> repoWrapper = Arrays.stream(rootPomElement.getChildren()).filter(psi -> psi instanceof XmlTag && ((XmlTag) psi).getName().equals("repositories")).findFirst();
        if (repoWrapper.isPresent()) {
             XmlTag repositories = (XmlTag) repoWrapper.get();
            String targetRepoId = getRepositoryId(getRepositoryUrl(this.dependency));
            repositoryInPom = Arrays.stream(repositories.getChildren())
                    .flatMap(element -> Arrays.stream(element.getChildren()))
                    .filter(element -> element instanceof XmlTag && "id".equals(((XmlTag) element).getName()))
                    .flatMap(tagValue -> Arrays.stream(((XmlTag) tagValue).getValue().getChildren()))
                    .anyMatch(tag -> tag instanceof XmlText && (((XmlText) tag).getValue().trim().equals(targetRepoId)));

        }

        return manifestIsPomXml && !repositoryInPom;

    }
}
