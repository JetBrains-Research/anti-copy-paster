package ru.hse.kirilenko.refactorings.utils.trie;

import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class Trie {
    private TrieNode root;

    public Trie(List<String> words) {
        root = new TrieNode();
        for (String word: words) {
            insert(word);
        }
    }

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String word) {
        TrieNode current = root;

        for (int i = 0; i < word.length(); i++) {
            current = current.getChildren()
                    .computeIfAbsent(word.charAt(i), c -> new TrieNode());
        }
        current.setEndOfWord(true);
    }

    public boolean find(String word) {
        TrieNode current = root;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            TrieNode node = current.getChildren().get(ch);
            if (node == null) {
                return false;
            }
            current = node;
        }
        return current.isWord();
    }

    public HashMap<String, Integer> calculate(String codeFragment) {
        HashMap<String, Integer> result = new HashMap<>();
        StringTokenizer st = new StringTokenizer(codeFragment, " }(){\t\n\r");

        while (st.hasMoreTokens()) {
            String word = st.nextToken();
            if (find(word)) {
                Integer cnt = result.get(word);
                result.put(word, (cnt == null) ? 1 : cnt + 1);
            }
        }

        return result;
    }
}
