package ru.hse.kirilenko.refactorings.utils;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.VoidType;

import java.io.PrintWriter;
import java.util.List;

import static ru.hse.kirilenko.refactorings.utils.OutputUtils.printLn;

public class MethodDataExtractor {
    public static void extractParamsCount(MethodDeclaration md, PrintWriter out) {
        List<Parameter> params = md.getParameters();
        if (params != null) {
            printLn("PARAMS COUNT: " + params.size(), out);
            //CSVBuilder.shared.addStr(params.size());
        } else {
            //CSVBuilder.shared.addStr(0);
        }
    }

    public static void isVoidMethod(MethodDeclaration md, PrintWriter out) {
        printLn("IS VOID METHOD: " + (md.getType() instanceof VoidType), out);
        //CSVBuilder.shared.addStr((md.getType() instanceof VoidType) ? 1 : 0);
    }
}
