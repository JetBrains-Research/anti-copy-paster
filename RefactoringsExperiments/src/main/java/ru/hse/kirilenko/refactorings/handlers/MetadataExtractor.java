package ru.hse.kirilenko.refactorings.handlers;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.VoidType;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import ru.hse.kirilenko.refactorings.ExtractionConfig;
import ru.hse.kirilenko.refactorings.collectors.CSVBuilder;
import ru.hse.kirilenko.refactorings.utils.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static ru.hse.kirilenko.refactorings.utils.NodeUtils.locs;
import static ru.hse.kirilenko.refactorings.utils.OutputUtils.printLn;

public class MetadataExtractor {
    private Repository repo;
    private PrintWriter out;

    public MetadataExtractor(final Repository repo, final PrintWriter out) {
        this.repo = repo;
        this.out = out;
    }

    public void extractFragment(final String commitId,
                                final String filePath,
                                int firstLine,
                                int lastLine,
                                int firstCol,
                                int lastCol,
                                boolean applyLineConstraints) throws Exception {
        RevWalk revWalk = new RevWalk(repo);
        ObjectId objectId = repo.resolve(commitId);
        RevCommit commit = revWalk.parseCommit(objectId);
        //repo.exactRef("").getStorage().
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(filePath));
        if (!treeWalk.next()) {
            return;
        }
        ObjectId objtId = treeWalk.getObjectId(0);
        ObjectLoader loader = repo.open(objtId);
        InputStream in = loader.openStream();
        StringBuilder allFileBuilder = new StringBuilder();

        try(BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            int skipLines = 0;
            while (skipLines++ < firstLine) {
                allFileBuilder.append(br.readLine());
                allFileBuilder.append('\n');
            }

            if (firstLine == lastLine) {
                String line = br.readLine();
                allFileBuilder.append(line);
                allFileBuilder.append('\n');
                if (line != null) {
                    for (int pos = firstCol; pos <= lastCol; ++pos) {
                        System.err.print(line.charAt(pos));
                    }
                }
            }

            StringBuilder codeFragmentBuilder = new StringBuilder();
            int procLines = firstLine;
            while (procLines <= lastLine) {
                String line = br.readLine();
                allFileBuilder.append(line);
                allFileBuilder.append('\n');
                if (line != null) {
                    String extractedLineFragment = extractLineFragment((applyLineConstraints && firstLine == procLines) ? firstCol : 0,
                            (applyLineConstraints && lastLine == procLines) ? lastCol : line.length() - 1, line);
                    codeFragmentBuilder.append(extractedLineFragment);
                    codeFragmentBuilder.append(' ');
                    printLn(extractedLineFragment, out);

                }
                procLines++;
            }

            while (br.ready()) {
                allFileBuilder.append(br.readLine());
                allFileBuilder.append('\n');
            }

            if (ExtractionConfig.parseJava) {
                try {
                    InputStream stream = new ByteArrayInputStream(allFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
                    CompilationUnit root = JavaParser.parse(stream);
                    MembersSets members = new MemberSetsGenerator().instanceMembers(root);
                    traverse(root, firstCol, firstLine, lastCol, lastLine, members);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


            String codeFragmentString = codeFragmentBuilder.toString();
            int fragLinesCount = lastLine - firstLine + 1;
            KeywordsCalculator.calculate(codeFragmentString, fragLinesCount, out);

            printLn("FRAGMENT LENGTH: " + codeFragmentString.length(), out);
            printLn("FRAGMENT LINE AVG SIZE: " + (double)codeFragmentString.length() / fragLinesCount, out);
            CSVBuilder.shared.addStr(codeFragmentString.length());
            CSVBuilder.shared.addStr((double)codeFragmentString.length() / fragLinesCount);
            analyzeDepth(allFileBuilder.toString(), firstLine, lastLine);
        } catch (Exception ex) {
            System.err.println("Cannot extract fragment from file");
        }
    }

    boolean traverse(Node cur, int fc, int fr, int ec, int er, MembersSets instanceMembers) {
        // node inside fragment
        if (isBefore(fc, fr, cur.getBeginColumn(), cur.getBeginLine()) && isBefore(cur.getEndColumn(), cur.getEndLine(), ec, er)) {
            String fragment = cur.toString();

            if (cur instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration)cur;
                MethodDataExtractor.extractParamsCount(md, out);
                MethodDataExtractor.isVoidMethod(md, out);
                int totalConnectivity = ConnectivityCalculator.calcConnectivity(fragment, instanceMembers.total, md.getName());
                int methodConnectivity = ConnectivityCalculator.calcConnectivity(fragment, instanceMembers.methods, md.getName());
                int fieldsConnectivity = ConnectivityCalculator.calcConnectivity(fragment, instanceMembers.fields, null);

                printLn("TOTAL CONNECTIVITY: " + totalConnectivity, out);
                printLn("AVG TOTAL CONNECTIVITY: " + (double)totalConnectivity / (er - fr + 1), out);

                printLn("FIELDS CONNECTIVITY: " + fieldsConnectivity, out);
                printLn("AVG FIELDS CONNECTIVITY: " + (double)fieldsConnectivity / (er - fr + 1), out);

                printLn("METHODS CONNECTIVITY: " + methodConnectivity, out);
                printLn("AVG METHODS CONNECTIVITY: " + (double)methodConnectivity / (er - fr + 1), out);
                CSVBuilder.shared.addStr(totalConnectivity);
                CSVBuilder.shared.addStr((double)totalConnectivity / (er - fr + 1));
                CSVBuilder.shared.addStr(fieldsConnectivity);
                CSVBuilder.shared.addStr((double)fieldsConnectivity / (er - fr + 1));
                CSVBuilder.shared.addStr(methodConnectivity);
                CSVBuilder.shared.addStr((double)methodConnectivity / (er - fr + 1));
                if (md.hasComment()) {
                    JavadocComment comment = md.getJavaDoc();
                    if (comment != null) {
                        int realLocs = locs(cur) - locs(comment);
                        printLn("NORMALIZED TOTAL CONNECTIVITY: " + (double)totalConnectivity / realLocs, out);
                        printLn("NORMALIZED FIELDS CONNECTIVITY: " + (double)fieldsConnectivity / realLocs, out);
                        printLn("NORMALIZED METHODS CONNECTIVITY: " + (double)methodConnectivity / realLocs, out);
                        //CSVBuilder.shared.addStr((double)totalConnectivity / realLocs);
                        //CSVBuilder.shared.addStr((double)fieldsConnectivity / realLocs);
                        //CSVBuilder.shared.addStr((double)methodConnectivity / realLocs);
                        String content = comment.getContent();
                        if (content != null) {
                            printLn("COMMENT LEN: " + content.length(), out);
                            //CSVBuilder.shared.addStr(content.length());
                        } else {
                            //CSVBuilder.shared.addStr(0);
                        }
                    } else {
                        //CSVBuilder.shared.addStr(0);
                        //CSVBuilder.shared.addStr(0);
                        //CSVBuilder.shared.addStr(0);
                        //CSVBuilder.shared.addStr(0);
                    }

                } else {
                    //CSVBuilder.shared.addStr(0);
                    //CSVBuilder.shared.addStr(0);
                    //CSVBuilder.shared.addStr(0);
                    //CSVBuilder.shared.addStr(0);
                    printLn("NO COMMENT", out);
                }

                List<NameExpr> thrws = md.getThrows();
                //printLn("HAS TROWS: " + (thrws != null && !thrws.isEmpty()), out);
                //CSVBuilder.shared.addStr((thrws != null && !thrws.isEmpty()) ? 1 : 0);
                BlockStmt body =  md.getBody();
                /*if (body != null) {
                    for (Statement stmt: body.getStmts()) {
                        //if
                    }
                }*/
            }

            return true;
        } else {
            for (Node n: cur.getChildrenNodes()) {
                if (traverse(n, fc, fr, ec, er, instanceMembers)) {
                    return true;
                }
            }
        }

        return false;
    }

    void analyzeDepth(String code,
                      int firstLine,
                      int lastLine) {
        int dep = 0;
        int line = 0;
        int area = 0;
        printLn("DEPTHS:", out);
        StringBuilder depsBuilder = new StringBuilder();
        int depInLine = 0;
        for (Character ch: code.toCharArray()) {
            if (ch == '{') {
                dep++;
                depInLine++;
            } else if (ch == '}') {
                dep--;
                depInLine--;
            } else if (ch == '\n'){
                if (line >= firstLine && line <= lastLine) {
                    int resDep = dep;
                    if (depInLine > 0) {
                        resDep--;
                    }
                    depInLine = 0;
                    out.print(resDep + " ");
                    depsBuilder.append(resDep).append("_");
                    area += resDep;
                }
                line++;
            }
        }

        out.println();
        //CSVBuilder.shared.addStr(depsBuilder.toString());
        printLn("AREA: " + area, out);
        printLn("AVG DEPTH: " + (double)area/(lastLine - firstLine + 1), out);
        CSVBuilder.shared.addStr(area);
        CSVBuilder.shared.addStr((double)area/(lastLine - firstLine + 1));
    }

    String extractLineFragment(int firstCol, int lastCol, String line) {
        StringBuilder result = new StringBuilder();

        for (int pos = firstCol; pos <= lastCol; ++pos) {
            result.append(line.charAt(pos));
        }

        return result.toString();
    }

    void extractLineAuthorAndCreationDate(int firstLine,
                                          int lastLine,
                                          String filePath) throws GitAPIException {
        final BlameResult result = new Git(repo).blame().setFilePath(filePath)
                .setTextComparator(RawTextComparator.WS_IGNORE_ALL).call();

        if (result == null) {
            CSVBuilder.shared.addStr(0);
            CSVBuilder.shared.addStr(0);
            CSVBuilder.shared.addStr(0);
            CSVBuilder.shared.addStr(0);
            printLn("Blame results not available!", out);
            return;
        }

        ArrayList<Integer> creationDates = new ArrayList<>();
        Set<String> commits = new HashSet<>();
        Set<String> authors = new HashSet<>();
        final RawText rawText = result.getResultContents();
        for (int i = firstLine; i < Math.min(rawText.size(), lastLine + 1); i++) {
            final PersonIdent sourceAuthor = result.getSourceAuthor(i);
            final RevCommit sourceCommit = result.getSourceCommit(i);
            if (sourceCommit != null) {
                creationDates.add(sourceCommit.getCommitTime());
                commits.add(sourceCommit.getName());
                authors.add(sourceAuthor.getName());
            }
            printLn(sourceAuthor.getName() +
                    (sourceCommit != null ? "/" + sourceCommit.getCommitTime() + "/" + sourceCommit.getName() : ""), out);
        }

        printLn("UNIQUE COMMITS: " + commits.size(), out);
        printLn("UNIQUE AUTHORS: " + authors.size(), out);
        CSVBuilder.shared.addStr(commits.size());
        CSVBuilder.shared.addStr(authors.size());

        int minTime = Integer.MAX_VALUE;
        int maxTime = Integer.MIN_VALUE;

        for (Integer time: creationDates) {
            if (minTime > time) {
                minTime = time;
            }
            if (maxTime < time) {
                maxTime = time;
            }
        }

        if (minTime != Integer.MAX_VALUE) {
            printLn("CODE LIVING TIME TOTAL: " + (maxTime - minTime), out);
            CSVBuilder.shared.addStr((maxTime - minTime));
            int totalTime = 0;
            for (Integer time: creationDates) {
                totalTime += time - minTime;
            }

            printLn("LINES AVG TIME: " + (double)totalTime / creationDates.size(), out);
            CSVBuilder.shared.addStr((double)totalTime / creationDates.size());
        } else {
            CSVBuilder.shared.addStr(0);
            CSVBuilder.shared.addStr(0);
        }
    }

    private boolean isInFragment(int col, int row, int fc, int fr, int ec, int er) {
        return (fr < row && row < er) || (fr == row && col >= fc) || (er == row && col <= ec);
    }

    private boolean isBefore(int c1, int r1, int c2, int r2) {
        return (r1 < r2) || (r1 == r2 && c1 <= c2);
    }
}
