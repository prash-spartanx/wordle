package com.games.wordleBackend.dto.response;

import com.games.wordleBackend.enums.GuessStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LetterResult {
    private int index;
    private String letter;
    private GuessStatus guessStatus;
}
