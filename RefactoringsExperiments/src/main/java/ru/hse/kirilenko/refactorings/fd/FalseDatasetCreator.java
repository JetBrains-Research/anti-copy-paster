package ru.hse.kirilenko.refactorings.fd;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.VoidType;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import ru.hse.kirilenko.refactorings.ExtractionConfig;
import ru.hse.kirilenko.refactorings.collectors.CSVBuilder;
import ru.hse.kirilenko.refactorings.handlers.MetadataExtractor;
import ru.hse.kirilenko.refactorings.utils.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static ru.hse.kirilenko.refactorings.utils.NodeUtils.locs;
import static ru.hse.kirilenko.refactorings.utils.OutputUtils.printLn;

public class FalseDatasetCreator {

    public static void main(String[] args) throws Exception {
        FalseDatasetCreator falseDatasetCreator = new FalseDatasetCreator();

        File file = new File("a.txt");

        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            //List<String> repos = new ArrayList<>();
            br.lines().forEach(new Consumer<String>() {
                @Override
                public void accept(String s) {
                    //repos.add(s);

                    String url = "https://github.com/" + s + ".git";
                    try {
                        falseDatasetCreator.run(s, url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });



        } catch (Exception ex) {
            throw ex;
        }
    }

    public void run(final String repoName, final String repoURL) throws Exception {
        GitService gitService = new GitServiceImpl();

        Repository repo = gitService.cloneIfNotExists(repoName, repoURL);

        try (Git git = new Git(repo)) {
            Iterable<RevCommit> commits = git.log().all().call();
            Iterator<RevCommit> it = commits.iterator();
            ArrayList<RevCommit> allCommits = new ArrayList<>();
            while (it.hasNext()) {
                allCommits.add(it.next());
            }

            RevCommit commit = allCommits.get(300);
            String commitId = commit.getId().getName();

            RevTree tree = commit.getTree();

            try (TreeWalk treeWalk = new TreeWalk(repo)) {
                treeWalk.reset(tree);
                while (treeWalk.next()) {
                    if (treeWalk.isSubtree()) {
                        treeWalk.enterSubtree();
                    } else {
                        handleFile(repo, treeWalk.getPathString(), commitId);
                    }
                }
            }
        }
    }

    private void handleFile(Repository repo, final String filePath, final String commitId) throws Exception {
        if (!filePath.endsWith(".java")) {
            return;
        }

        RevWalk revWalk = new RevWalk(repo);
        ObjectId objectId = repo.resolve(commitId);
        RevCommit commit = revWalk.parseCommit(objectId);
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
            while (br.ready()) {
                allFileBuilder.append(br.readLine());
                allFileBuilder.append('\n');
            }

            InputStream stream = new ByteArrayInputStream(allFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
            try {
                CompilationUnit root = JavaParser.parse(stream);
                traverse(repo, filePath, root, root);
            } catch (NullPointerException npe) {
                //skip parse error files
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    void traverse(Repository repo, String filePath, CompilationUnit root, Node cur) throws GitAPIException {
        if (cur instanceof MethodDeclaration) {
            MethodDeclaration md = (MethodDeclaration)cur;
            BlockStmt body =  md.getBody();
            if (body == null) {
                return;
            }
            traverseBlock(repo, filePath, root, md, body, 0, 2);

            //System.out.println();
        } else {
            for (Node n: cur.getChildrenNodes()) {
                traverse(repo, filePath, root, n);
            }
        }
    }

    void traverseBlock(Repository repo, String filePath, CompilationUnit root, MethodDeclaration md, Statement blk, int dep, int k) throws GitAPIException {
        if (!canExtract(blk, dep)) {
            return;
        }

        int count = countStmts(blk);

        List<Statement> next = new ArrayList<>();
        for (Node n: blk.getChildrenNodes()) {
            if (n instanceof Statement) {
                Statement s = (Statement) n;
                int cnt2 = countStmts(s);
                if (cnt2 <= 10 && cnt2 >= k && count - cnt2 >= k) {
                    analyzeBlock(repo, root, md, blk, filePath);
                    System.out.println(n.toString());
                    System.out.println();
                }
                if (cnt2 >= 2 * k) {
                    next.add(s);
                }
            }

        }

        for (Statement statement: next) {
            traverseBlock(repo, filePath, root, md, statement, dep + 1, k);
        }
    }

    int countStmts(Statement statement) {
        if (statement.getChildrenNodes().isEmpty()) {
            return 1;
        } else {
            int res = 1;
            for (Node s: statement.getChildrenNodes()) {
                if (s instanceof Statement) {
                    res += countStmts((Statement) s);
                }

            }

            return res;
        }
    }

    boolean canExtract(Statement blk, int dep) {
        List<Node> l = blk.getChildrenNodes();
        if (l == null) {
            return false;
        }

        int ans = 0;
        for (Node n: l) {
            if (n instanceof Statement) {
                ++ans;
            }
        }

        return ans >= 2 && dep + ans >= 4;
    }

    void analyzeBlock(Repository repo, CompilationUnit root, MethodDeclaration md, Statement blk, String filePath) throws GitAPIException {
        CSVBuilder.shared.completeLine(false);
        calcConnectivity(root, md, blk);

        String fragment = blk.toString();
        String linearFragment = fragment.replace('\n', ' ');
        int fragLocs = StringUtils.countMatches(fragment, "\n")  + 1;
        KeywordsCalculator.calculateCSV(linearFragment, fragLocs);

        CSVBuilder.shared.addStr(linearFragment.length());
        CSVBuilder.shared.addStr((double)linearFragment.length() / fragLocs);

        analyzeDepth(fragment, fragLocs);

        GitBlameAnalyzer.extractLineAuthorAndCreationDate(repo, blk.getBeginLine(), blk.getEndLine(), filePath);
        CSVBuilder.shared.addStr(fragLocs);
    }

    void calcConnectivity(CompilationUnit root, MethodDeclaration md, Statement blk) {
        String fragment = blk.toString();
        int fragmentLocs = locs(blk);
        MembersSets members = new MemberSetsGenerator().instanceMembers(root);

        int totalConnectivity = ConnectivityCalculator.calcConnectivity(fragment, members.total, md.getName());
        int methodConnectivity = ConnectivityCalculator.calcConnectivity(fragment, members.methods, md.getName());
        int fieldsConnectivity = ConnectivityCalculator.calcConnectivity(fragment, members.fields, null);

        CSVBuilder.shared.addStr(totalConnectivity);
        CSVBuilder.shared.addStr((double)totalConnectivity / fragmentLocs);
        CSVBuilder.shared.addStr(fieldsConnectivity);
        CSVBuilder.shared.addStr((double)fieldsConnectivity / fragmentLocs);
        CSVBuilder.shared.addStr(methodConnectivity);
        CSVBuilder.shared.addStr((double)methodConnectivity / fragmentLocs);

        // normalized connectivity duplicate due to zero size of comment
        //CSVBuilder.shared.addStr((double)totalConnectivity / fragmentLocs);
        //CSVBuilder.shared.addStr((double)fieldsConnectivity / fragmentLocs);
        //CSVBuilder.shared.addStr((double)methodConnectivity / fragmentLocs);

        // comment len is zero
        //CSVBuilder.shared.addStr(0);
    }

    void analyzeDepth(String code, int locCount) {
        int dep = 0;
        int area = 0;
        int depInLine = 0;
        for (Character ch: code.toCharArray()) {
            if (ch == '{') {
                dep++;
                depInLine++;
            } else if (ch == '}') {
                dep--;
                depInLine--;
            } else if (ch == '\n'){
                int resDep = dep;
                if (depInLine > 0) {
                    resDep--;
                }
                depInLine = 0;
                area += resDep;
            }
        }

        CSVBuilder.shared.addStr(area);
        CSVBuilder.shared.addStr((double)area / locCount);
    }
}
