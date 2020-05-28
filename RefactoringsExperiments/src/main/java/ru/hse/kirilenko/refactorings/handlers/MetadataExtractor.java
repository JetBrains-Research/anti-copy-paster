package ru.hse.kirilenko.refactorings.handlers;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import ru.hse.kirilenko.refactorings.ExtractionConfig;
import ru.hse.kirilenko.refactorings.csv.SparseCSVBuilder;
import ru.hse.kirilenko.refactorings.csv.models.CSVItem;
import ru.hse.kirilenko.refactorings.csv.models.Feature;
import ru.hse.kirilenko.refactorings.utils.*;
import ru.hse.kirilenko.refactorings.utils.calcers.ConnectivityCalculator;
import ru.hse.kirilenko.refactorings.utils.calcers.KeywordsCalculator;
import ru.hse.kirilenko.refactorings.utils.calcers.MemberSetsGenerator;
import ru.hse.kirilenko.refactorings.utils.calcers.MembersSets;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static ru.hse.kirilenko.refactorings.legacy.OutputUtils.printLn;

public class MetadataExtractor {
    private Repository repo;
    private PrintWriter out;

    public MetadataExtractor(final Repository repo, final PrintWriter out) {
        this.repo = repo;
        this.out = out;
    }

    public Repository getRepo() {
        return repo;
    }

    public MethodDeclaration extractFragment(final String commitId,
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
            return null;
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

            MethodDeclaration md = null;
            if (ExtractionConfig.parseJava) {
                try {
                    InputStream stream = new ByteArrayInputStream(allFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
                    CompilationUnit root = JavaParser.parse(stream);
                    MembersSets members = new MemberSetsGenerator().instanceMembers(root);
                    md = traverse(root, firstCol, firstLine, lastCol, lastLine, members);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


            String codeFragmentString = codeFragmentBuilder.toString();
            int fragLinesCount = lastLine - firstLine + 1;
            KeywordsCalculator.calculateCSV(codeFragmentString, fragLinesCount);

            printLn("FRAGMENT LENGTH: " + codeFragmentString.length(), out);
            printLn("FRAGMENT LINE AVG SIZE: " + (double)codeFragmentString.length() / fragLinesCount, out);
            SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalSymbolsInCodeFragment, codeFragmentString.length()));
            SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.AverageSymbolsInCodeLine, (double)codeFragmentString.length() / fragLinesCount));
            analyzeDepth(allFileBuilder.toString(), firstLine, lastLine);
            return md;
        } catch (Exception ex) {
            System.err.println("Cannot extract fragment from file");
        }

        return null;
    }

    MethodDeclaration traverse(Node cur, int fc, int fr, int ec, int er, MembersSets instanceMembers) {
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

                SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalConnectivity, totalConnectivity));
                SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalConnectivityPerLine, (double)totalConnectivity / (er - fr + 1)));
                SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.FieldConnectivity, fieldsConnectivity));
                SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.FieldConnectivityPerLine, (double)fieldsConnectivity / (er - fr + 1)));
                SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.MethodConnectivity, methodConnectivity));
                SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.MethodConnectivityPerLine, (double)methodConnectivity / (er - fr + 1)));

                return md;
            }

            return null;
        } else {
            for (Node n: cur.getChildrenNodes()) {
                MethodDeclaration md = traverse(n, fc, fr, ec, er, instanceMembers);
                if (md != null) {
                    return md;
                }
            }
        }

        return null;
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
        printLn("AREA: " + area, out);
        printLn("AVG DEPTH: " + (double)area/(lastLine - firstLine + 1), out);
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalLinesDepth, area));
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.AverageLinesDepth, (double)area/(lastLine - firstLine + 1)));
    }

    String extractLineFragment(int firstCol, int lastCol, String line) {
        StringBuilder result = new StringBuilder();

        for (int pos = firstCol; pos <= lastCol; ++pos) {
            result.append(line.charAt(pos));
        }

        return result.toString();
    }

    private boolean isBefore(int c1, int r1, int c2, int r2) {
        return (r1 < r2) || (r1 == r2 && c1 <= c2);
    }
}
