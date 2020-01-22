package ru.hse.kirilenko.refactorings.legacy;

import gr.uom.java.xmi.decomposition.*;
import gr.uom.java.xmi.decomposition.replacement.Replacement;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.*;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

public class FirstExperiments {
    public static void main(String[] args) throws Exception {
        //experiments("tmp/refactoring-toy-example", "https://github.com/danilofes/refactoring-toy-example.git");

        experiments("tmp/common-lang", "https://github.com/apache/commons-lang.git");
    }

    private static void experiments(String pathToLocalRepo, String repoURL) throws Exception {
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

        Repository repo = gitService.cloneIfNotExists(pathToLocalRepo, repoURL);

        try (Git git = new Git(repo)) {
            Iterable<RevCommit> commits = git.log().all().call();
            int count = 0;
            for (RevCommit commit : commits) {
                System.out.println("LogCommit: " + commit);
                count++;
            }
            System.out.println(count);
        }

        FileWriter fileWriter = new FileWriter("experiment_results_3.txt");
        final PrintWriter printWriter = new PrintWriter(fileWriter);

        FileWriter fileWriter2 = new FileWriter("experiment_results_4.txt");
        final PrintWriter printWriter2 = new PrintWriter(fileWriter2);

        miner.detectAll(repo, "master", new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                handleCommit(commitId, refactorings, null);
                //handleCommit(commitId, refactorings, printWriter);
                //handleCommitAll(commitId, refactorings, printWriter2);
            }
        });

        printWriter.close();
    }

    private static void handleCommitAll(String commitId, List<Refactoring> refactorings, PrintWriter pw) {
        boolean shouldPrintCommitId = false;
        for (Refactoring ref : refactorings) {
            if (ref.getRefactoringType() == RefactoringType.EXTRACT_OPERATION) {
                shouldPrintCommitId = true;
                printLn("§§§§§§§§§§§§§§§", pw);
                printLn(ref.toString(), pw);
                ExtractOperationRefactoring refactoring = (ExtractOperationRefactoring)ref;
                UMLOperationBodyMapper mapper = refactoring.getBodyMapper();
                for(AbstractCodeMapping mapping : mapper.getMappings()) {
                    AbstractCodeFragment fragment1 = mapping.getFragment1();
                    printLn(fragment1.toString(), pw);
                }
                printLn("", pw);
                for(AbstractCodeMapping mapping : mapper.getMappings()) {
                    AbstractCodeFragment fragment2 = mapping.getFragment2();
                    printLn(fragment2.toString(), pw);
                }
            }

        }

        if (shouldPrintCommitId) {
            printLn("COMMIT ID: " + commitId, pw);
            printLn("§§§§§§§§§§§§§§§", pw);
        }
    }

    private static void handleCommit(String commitId, List<Refactoring> refactorings, PrintWriter pw) {
        boolean shouldPrintCommitId = false;
        for (Refactoring ref : refactorings) {
            if (ref.getRefactoringType() == RefactoringType.EXTRACT_OPERATION) {
                shouldPrintCommitId = true;
                printLn("§§§§§§§§§§§§§§§", pw);
                printLn(ref.toString(), pw);
                ExtractOperationRefactoring refactoring = (ExtractOperationRefactoring)ref;
                Set<AbstractCodeFragment> s = refactoring.getExtractedCodeFragmentsFromSourceOperation();
                for (AbstractCodeFragment fr: s) {
                    printLn(fr.getString(),pw);
                }
                /*printLn(refactoring.getSourceOperationBeforeExtraction().getBody().getCompositeStatement().toString() , pw);
                printLn(refactoring.getSourceOperationAfterExtraction().getBody().getCompositeStatement().codeRange().getCodeElement(), pw);
                printLn(refactoring.getSourceOperationAfterExtraction().getBody().getCompositeStatement().codeRange().getDescription(), pw);
                printLn(refactoring.getSourceOperationAfterExtraction().getBody().getCompositeStatement().codeRange().getFilePath(), pw);*/
                printCompositeStatement(refactoring.getSourceOperationBeforeExtraction().getBody().getCompositeStatement(),  0);
                UMLOperationBodyMapper mapper = refactoring.getBodyMapper();
                /*for(AbstractCodeMapping mapping : mapper.getMappings()) {
                    AbstractCodeFragment fragment1 = mapping.getFragment1();
                    AbstractCodeFragment fragment2 = mapping.getFragment2();
                    printLn("Fragment 1:", pw);
                    printLn(fragment1.toString(), pw);
                    printLn("Fragment 2:", pw);
                    printLn(fragment2.toString(), pw);

                    Set<Replacement> replacements = mapping.getReplacements();
                    for(Replacement replacement : replacements) {
                        String valueBefore = replacement.getBefore();
                        String valueAfter = replacement.getAfter();
                        Replacement.ReplacementType type = replacement.getType();
                        printLn("BEFORE:", pw);
                        printLn(valueBefore, pw);
                        printLn("AFTER:", pw);
                        printLn(valueAfter, pw);
                        printLn("TYPE:", pw);
                        printLn(type.toString(), pw);
                    }
                }*/
            }

        }

        if (shouldPrintCommitId) {
            printLn("COMMIT ID: " + commitId, pw);
            printLn("§§§§§§§§§§§§§§§", pw);
        }
    }

    private static void printCompositeStatement(CompositeStatementObject statementObject, int offset) {
        String statement = statementObject.toString();
        boolean shouldCloseBracket = false;
        if(statement.equals("{")) shouldCloseBracket = true;

        printWithOneEndl(pushSpaces(statement, statementObject.getLocationInfo().getStartColumn()));
        for (AbstractStatement as: statementObject.getStatements()) {
            if (as instanceof CompositeStatementObject) {
                printCompositeStatement((CompositeStatementObject)as, as.getLocationInfo().getStartColumn());
            } else if (as instanceof StatementObject) {
                printWithOneEndl(pushSpaces(((StatementObject)as).toString(), as.getLocationInfo().getStartColumn()));
            } else {
                printWithOneEndl(pushSpaces("ERROR STATEMENT\n", as.getLocationInfo().getStartColumn()));
            }
        }

        if (shouldCloseBracket) printWithOneEndl(pushSpaces("}", statementObject.getLocationInfo().getStartColumn()));
    }

    private static void printWithOneEndl(String s) {
        if (s.endsWith("\n")) {
            System.out.print(s);
        } else {
            System.out.println(s);
        }
    }
    private static String pushSpaces(String to, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; ++i) {
            sb.append(' ');
        }
        sb.append(to);

        return sb.toString();
    }

    private static void printLn(String line, PrintWriter pw) {
        if (pw == null) {
            System.out.println(line);
        } else {
            pw.println(line);
        }
    }
}
