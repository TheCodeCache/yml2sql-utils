package com.local.datalake.alias;

import static com.local.datalake.common.Constants.COMMA;
import static com.local.datalake.common.Constants.KEYWORDS_FILE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Maintains a list of reserved words
 * 
 * It uses "Trie" under the hood for an efficient search operation
 * 
 * @author manoranjan
 */
public class KeyWords {

    /**
     * loads all reserved-words into Trie just one single time
     */
    static {
        Trie trie = new Trie();
        try (Stream<String> stream = Files.lines(Paths.get(KEYWORDS_FILE))) {
            stream.map(word -> word.split(COMMA)).flatMap(Arrays::stream).map(String::trim).forEach(trie::insert);
        } catch (IOException ex) {
            throw new RuntimeException("Exception in reading keywords.txt file", ex);
        }
    }

    /**
     * Returns true if the alias is found in Reserved Words, else false
     * 
     * @param string
     * @return
     */
    public static boolean have(String string) {
        return Trie.search(string);
    }

    /**
     * Trie Implementation
     */
    private static class Trie {

        /**
         * Trie-Node Model
         */
        private static class TrieNode {
            private final Map<Character, TrieNode> children = new HashMap<>();
            private boolean                        endOfWord;

            Map<Character, TrieNode> getChildren() {
                return children;
            }

            boolean isEndOfWord() {
                return endOfWord;
            }

            void setEndOfWord(boolean endOfWord) {
                this.endOfWord = endOfWord;
            }
        }

        // root node of trie structure
        private static TrieNode root;

        Trie() {
            root = new TrieNode();
        }

        /**
         * inserts every character from the reserved words into Trie
         * 
         * @param word
         */
        private void insert(String word) {
            TrieNode current = root;

            for (char ch : word.toCharArray()) {
                current = current.getChildren().computeIfAbsent(ch, che -> new TrieNode());
            }
            current.setEndOfWord(true);
        }

        /**
         * Search for a given word
         * 
         * @note: gives O(K) search time, where K is the word length
         * @param word
         * @return
         */
        protected static boolean search(String word) {
            TrieNode current = root;

            for (char ch : word.toCharArray()) {
                TrieNode node = current.getChildren().get(ch);
                if (node == null) {
                    return false;
                }
                current = node;
            }
            return current.isEndOfWord();
        }
    }
}
