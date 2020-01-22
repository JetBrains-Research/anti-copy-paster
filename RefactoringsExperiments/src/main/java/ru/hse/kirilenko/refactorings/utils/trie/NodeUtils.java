package ru.hse.kirilenko.refactorings.utils.trie;

import com.github.javaparser.ast.Node;
import org.apache.commons.lang3.StringUtils;

public class NodeUtils {
    public static int locs(Node node) {
        return node.getEndLine() - node.getBeginLine() + 1;
    }

    public static int locsString(String fragment) {
        return StringUtils.countMatches(fragment, '\n') + 1;
    }
}
