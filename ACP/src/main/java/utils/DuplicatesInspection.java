package utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public final class DuplicatesInspection {
    private static ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);

    public InspectionResult resolve(final Project project, final String code) {
        final ArrayList<Future<FindResult>> tasks = new ArrayList<>();
        ProjectFileIndex.SERVICE.getInstance(project).iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(@NotNull VirtualFile fileOrDir) {
                final PsiFile file = PsiManager.getInstance(project).findFile(fileOrDir);
                if (file == null || !file.getName().endsWith(".java")) {
                    return true;
                }

                tasks.add(pool.submit(() -> ApplicationManager.getApplication().runReadAction(new Computable<FindResult>() {

                    @Override
                    public FindResult compute() {
                        String content = file.getText().replace('\n', ' ').replace('\t', ' ').replace('\r', ' ').replaceAll("\\s+","");
                        return new FindResult(StringUtils.countMatches(content, code), file);
                    }
                })));

                return true;
            }
        });

        int matches = 0;
        HashSet<PsiFile> files = new HashSet<>();

        for (Future<FindResult> future: tasks) {
            try {
                FindResult res = future.get();
                matches += res.count;
                if (res.count > 0) {
                    files.add(res.file);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return new InspectionResult(matches, files);
    }

    public PsiFile resolve(final Project project, final PsiFile file_) {
        final ArrayList<PsiFile> tasks = new ArrayList<>();
        ProjectFileIndex.SERVICE.getInstance(project).iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(@NotNull VirtualFile fileOrDir) {
                PsiFile file = PsiManager.getInstance(project).findFile(fileOrDir);
                if (file == null || !file.getName().endsWith(".java")) {
                    return true;
                }

                if (file.getName().equals(file_.getName())) {
                    tasks.add(file);
                }


                return true;
            }
        });

        return tasks.isEmpty() ? null : tasks.get(0);
    }

    public static class FindResult {
        public FindResult(int count, PsiFile file) {
            this.count = count;
            this.file = file;
        }

        public int count;
        public PsiFile file;
    }

    public static class InspectionResult {
        public InspectionResult(int count, HashSet<PsiFile> files) {
            this.count = count;
            this.files = files;
        }

        public int count;
        public HashSet<PsiFile> files;
    }
}
