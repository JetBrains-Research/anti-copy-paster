package org.jetbrains.research.anticopypaster.utils;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.research.extractMethod.metrics.MetricCalculator;
import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MetricsGatherer {

    private List<FeaturesVector> methodsMetrics;

    public MetricsGatherer(){
        this.methodsMetrics = new ArrayList<>();
        gatherMetrics();
    }

    private void gatherMetrics(){
        System.err.println("Hello world");
        Project project = ProjectManager.getInstance().getOpenProjects()[0];

        Collection<VirtualFile> vfCollection = FileTypeIndex.getFiles(
                JavaFileType.INSTANCE,
                GlobalSearchScope.projectScope(project));
        List<PsiFile> pfList = new ArrayList<>();
        for(VirtualFile file: vfCollection){
            pfList.add(PsiManager.getInstance(project).findFile(file));
        }

//        try(FileWriter fr = new FileWriter("D:\\Desktop\\MethodStuff\\fileList.txt")){
//            for(PsiFile file:pfList){
//                fr.write(file.getName());
//                fr.write("\n");
//            }
//        }catch(IOException io){
//
//        }
        List<PsiMethod> methods = new ArrayList<>();
        List<Integer> methodStart = new ArrayList<>();
        List<Integer> methodEnd = new ArrayList<>();

//        try(FileWriter fr = new FileWriter("D:\\Desktop\\MethodStuff\\classList.txt")){
            for(PsiFile psiFile: pfList){
                PsiJavaFile psiJavaFile = (PsiJavaFile)psiFile;
                PsiClass[] classes = psiJavaFile.getClasses();
                Collection<PsiMethod> psiMethods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod.class);
        //                for(PsiClass psiClass: classes){
        //                    fr.write(Objects.requireNonNull(psiClass.getName()));
        //                    fr.write("\n");
        //                    PsiMethod[] methodArr = psiClass.getMethods();
                    for(PsiMethod method: psiMethods) {
                        int start = PsiUtil.getNumberOfLine(psiFile, method.getTextRange().getStartOffset());
                        int end = PsiUtil.getNumberOfLine(psiFile, method.getTextRange().getEndOffset());
//                        fr.write(method.getName());
//                        fr.write("\n");
//                        fr.write(Integer.toString(end));
//                        fr.write("\n");
                        methodStart.add(start);
                        methodEnd.add(end);
                        methods.add(method);
                    }

               // }
            }
//        }catch(IOException io) {
//
//        }
        for(int i = 0; i < methods.size(); i++){
            int startOffset = methodStart.get(i);
            int endOffset = methodEnd.get(i);
            PsiMethod method = methods.get(i);
//            try(FileWriter fr = new FileWriter("D:\\Desktop\\MethodStuff\\testing2.txt")){
//                fr.write("Line before MetricCalculator");
//            }catch(IOException ioe){
//
//            }

            FeaturesVector features = new
                    MetricCalculator(method, method.getText(), startOffset, endOffset).getFeaturesVector();

//            try(FileWriter fr = new FileWriter("D:\\Desktop\\MethodStuff\\testing3.txt")){
//                fr.write("Did calculator build?");
//            }catch(IOException ioe){
//
//            }
            methodsMetrics.add(features);
            String fileLocation = "D:\\Desktop\\MethodStuff\\";
            String filename = fileLocation + method.getName() + ".txt";

//            try (FileWriter fileWriter = new FileWriter(filename)) {
//                fileWriter.write(method.getName() + "\n");
//                fileWriter.write(method.getText() + "\n\n");
                float[] theNumbers = features.buildArray();

                for (int j = 0; j < theNumbers.length; j++) {
//                    fileWriter.write(j + " : " + theNumbers[j]);
                    System.out.println(theNumbers[i]);
                }
//            } catch (IOException io) {
//                System.out.println("Print failed but you won't see this");
//            }
        }
    }
}
