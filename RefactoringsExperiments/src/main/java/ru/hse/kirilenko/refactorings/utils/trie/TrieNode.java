package ru.hse.kirilenko.refactorings.utils.trie;

import java.util.HashMap;

public class TrieNode {
    private HashMap<Character, TrieNode> children;
    private String content;
    private boolean isWord;

    public TrieNode() {
        children = new HashMap<>();
        content = "";
        isWord = false;
    }

    public HashMap<Character, TrieNode> getChildren() {
        return children;
    }

    public boolean isWord() {
        return isWord;
    }

    public void setEndOfWord(boolean word) {
        isWord = word;
    }


}
