package org.jetbrains.research.anticopypaster.metrics;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.anticopypaster.models.MemberSets;
import org.jetbrains.research.anticopypaster.models.features.Feature;
import org.jetbrains.research.anticopypaster.models.features.FeatureItem;
import org.jetbrains.research.anticopypaster.models.features.FeaturesVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;

import static org.jetbrains.research.anticopypaster.metrics.DepthAnalyzer.getNestingArea;

public class MetricCalculator {
    private final String statementsStr;
    private final PsiMethod method;

    private final int beginLine;
    private final int endLine;
    private final FeaturesVector featuresVector;

    public MetricCalculator(String code, PsiMethod dummyPsiMethod, int beginLine, int endLine) {
        this.method = dummyPsiMethod;
        this.statementsStr = code;
        this.beginLine = beginLine;
        this.endLine = endLine;
        this.featuresVector = new FeaturesVector(82); // TODO: Make dimension changeable outside
        computeFeatureVector();
    }


    private void computeFeatureVector() {
        couplingFeatures();
        keywordFeatures();
        methodFeatures();
        metaFeatures();
        historicalFeatures();
    }

    public FeaturesVector getFeaturesVector() {
        return this.featuresVector;
    }

    private void couplingFeatures() {
        PsiFile thisFile = method.getContainingFile();
        MemberSets memberSets = MemberSets.extractAllMethodsAndFields(thisFile);

        int linesCount = endLine - beginLine + 1;

        int fieldMatches = 0;
        int methodMatches = 0;
        int totalMatches;

        PsiFileFactory factory = PsiFileFactory.getInstance(thisFile.getProject());
        @Nullable PsiFile psiFromText = factory.createFileFromText(statementsStr, thisFile);
        PsiElement psiElement = factory.createFileFromText(statementsStr, thisFile);
        psiElement.getText();
        // search for all identifiers (methods and variables) in the code fragment
        @NotNull Collection<PsiIdentifier> identifiers = PsiTreeUtil.collectElementsOfType(psiFromText,
                PsiIdentifier.class);
        HashSet<String> identifiersNames = new HashSet<>();
        identifiers.forEach(i -> identifiersNames.add(i.getText()));

        for (String fieldName : memberSets.fields) {
            if (identifiersNames.contains(fieldName)) {
                fieldMatches += 1;
            }
        }

        for (String methodName : memberSets.methods) {
            if (identifiersNames.contains(methodName)) {
                methodMatches += 1;
            }
        }

        totalMatches = methodMatches + fieldMatches;

        featuresVector.addFeature(new FeatureItem(Feature.TotalConnectivity, totalMatches));
        featuresVector.addFeature(new FeatureItem(
                Feature.TotalConnectivityPerLine, (double) totalMatches / linesCount));
        featuresVector.addFeature(new FeatureItem(Feature.FieldConnectivity, fieldMatches));
        featuresVector.addFeature(new FeatureItem(
                Feature.FieldConnectivityPerLine, (double) fieldMatches / linesCount));
        featuresVector.addFeature(new FeatureItem(Feature.MethodConnectivity, methodMatches));
        featuresVector.addFeature(new FeatureItem(
                Feature.MethodConnectivityPerLine, (double) methodMatches / linesCount));
    }

    private void keywordFeatures() {
        List<String> allKeywords = Arrays.asList(
                "continue", "for", "new", "switch",
                "assert", "synchronized", "boolean", "do",
                "if", "this", "break", "double",
                "throw", "byte", "else", "case",
                "instanceof", "return", "transient",
                "catch", "int", "short", "try",
                "char", "final", "finally", "long",
                "strictfp", "float", "super", "while"); // 31 keywords, id from 20 to 81

        HashMap<String, Integer> counts = new HashMap<>();
        for (String key : allKeywords) {
            counts.put(key, StringUtils.countMatches(this.statementsStr, key));
        }

        int linesCount = endLine - beginLine + 1;

        int id = 20; // initialized with 20 to account for shift in Keyword-Features begin id.
        for (String keyword : allKeywords) {
            Integer count = counts.get(keyword);
            featuresVector.addFeature(new FeatureItem(Feature.fromId(id++), count));
            featuresVector.addFeature(new FeatureItem(Feature.fromId(id++), (double) count / linesCount));
        }
    }

    private void methodFeatures() {
        String methodStr = this.method.getText();
        int methodArea = getNestingArea(methodStr);
        int lineCount = StringUtils.countMatches(methodStr, "\n") + 1;

        featuresVector.addFeature(new FeatureItem(Feature.MethodDeclarationLines, lineCount));
        featuresVector.addFeature(new FeatureItem(Feature.MethodDeclarationSymbols, methodStr.length()));
        featuresVector.addFeature(new FeatureItem(
                Feature.MethodDeclarationSymbolsPerLine, (double) methodStr.length() / lineCount));
        featuresVector.addFeature(new FeatureItem(Feature.MethodDeclarationArea, methodArea));
        featuresVector.addFeature(new FeatureItem(
                Feature.MethodDeclarationAreaPerLine, (double) methodArea / lineCount));

    }

    private void metaFeatures() {
        String fragment = statementsStr;
        int fragmentArea = getNestingArea(fragment);
        int lineCount = StringUtils.countMatches(fragment, "\n") + 1;

        featuresVector.addFeature(new FeatureItem(Feature.TotalLinesOfCode, lineCount));
        featuresVector.addFeature(new FeatureItem(Feature.TotalSymbols, fragment.length()));
        featuresVector.addFeature(new FeatureItem(
                Feature.SymbolsPerLine, (double) fragment.length() / lineCount));
        featuresVector.addFeature(new FeatureItem(Feature.Area, fragmentArea));
        featuresVector.addFeature(new FeatureItem(
                Feature.AreaPerLine, (double) fragmentArea / lineCount));

    }

    private void historicalFeatures() {
        // Temporary solution by padding with zeroes
        featuresVector.addFeature(
                new FeatureItem(Feature.TotalCommitsInFragment, 0));
        featuresVector.addFeature(
                new FeatureItem(Feature.TotalAuthorsInFragment, 0));
        featuresVector.addFeature(new FeatureItem(Feature.LiveTimeOfFragment, 0));
        featuresVector.addFeature(
                new FeatureItem(Feature.LiveTimePerLine, (double) 0));
    }
}