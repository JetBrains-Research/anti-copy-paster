package ru.hse.kirilenko.refactorings.utils;

import com.github.javaparser.ast.Node;

public class NodeUtils {
    public static int locs(Node node) {
        return node.getEndLine() - node.getBeginLine() + 1;
    }
}
