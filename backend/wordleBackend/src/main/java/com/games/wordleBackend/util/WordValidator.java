// WordValidator.java — new file in a util or service package
package com.games.wordleBackend.util;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component
public class WordValidator {

    private final Set<String> validWords = new HashSet<>();

    // runs once after Spring creates this bean — loads file into memory
    @PostConstruct
    public void loadWords() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(
                        getClass().getClassLoader().getResourceAsStream("wordlist.txt")
                )
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toUpperCase();
                if (word.length() == 5) {
                    validWords.add(word);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load word list", e);
        }
    }

    public boolean isValid(String word) {
        return validWords.contains(word.toUpperCase());
    }
}