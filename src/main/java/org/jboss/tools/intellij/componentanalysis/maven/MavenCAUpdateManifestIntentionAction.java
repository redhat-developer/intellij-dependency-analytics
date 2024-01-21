package org.jboss.tools.intellij.componentanalysis.maven;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.xml.util.XmlUtil;
import com.redhat.exhort.api.DependencyReport;
import com.redhat.exhort.api.PackageRef;
import org.jboss.tools.intellij.componentanalysis.CAUpdateManifestIntentionAction;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlValue;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class MavenCAUpdateManifestIntentionAction extends CAUpdateManifestIntentionAction {
    @Override
    protected String getTextImpl() {
        return "Add Redhat GA Maven Repository to your pom.xml";
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

        XmlTag id = repository.createChildTag("id", repository.getNamespace(), "redhat-ga", false);
        XmlTag name = repository.createChildTag("name", repository.getNamespace(), "Redhat GA Maven Repository", false);
        XmlTag url = repository.createChildTag("url", repository.getNamespace(), getRepositoryUrl(dependency), false);
        id.setName("id");
        name.setName("name");
        url.setName("url");
        id.getValue().setText("redhat-ga");
        name.getValue().setText("Redhat GA Maven Repository");
        url.getValue().setText(getRepositoryUrl(dependency));
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

    private static String getRepositoryUrl(DependencyReport dependency) {
        return getRepositoryUrlFromPurl(dependency);
    }

    private static String getRepositoryUrlFromPurl(DependencyReport dependency) {
        AtomicReference<PackageRef> packageRef = new AtomicReference<>();
        if(Objects.nonNull(dependency.getRecommendation()))
        {
            packageRef.set(dependency.getRecommendation());
        }
        else {
            dependency.getIssues().stream().filter(issue -> Objects.nonNull(issue.getRemediation().getTrustedContent())).findFirst().ifPresent( value -> packageRef.set(value.getRemediation().getTrustedContent().getRef()));
        }
        return packageRef.get().purl().getQualifiers().get("repository_url");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        boolean manifestIsPomXml = file != null && "pom.xml".equals(file.getName());
        boolean repositoryInPom = false;
        PsiElement rootPomElement = getRootPomElement(file);
        Optional<PsiElement> repoWrapper = Arrays.stream(rootPomElement.getChildren()).filter(psi -> psi instanceof XmlTag && ((XmlTag) psi).getName().equals("repositories")).findFirst();
        if (repoWrapper.isPresent()) {
             XmlTag repositories = (XmlTag) repoWrapper.get();
            repositoryInPom = Arrays.stream(repositories.getChildren())
                    .flatMap(element -> Arrays.stream(element.getChildren()))
                    .filter(element -> element instanceof XmlTag && "id".equals(((XmlTag) element).getName()))
                    .flatMap(tagValue -> Arrays.stream(((XmlTag) tagValue).getValue().getChildren()))
                    .anyMatch(tag -> tag instanceof XmlText && (((XmlText) tag).getValue().trim().equals("redhat-ga")));

        }

        return manifestIsPomXml && !repositoryInPom;

    }
}
