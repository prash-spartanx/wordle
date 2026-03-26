package com.games.wordleBackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitGuessRequest {
    private String sessionId;
    private String guessedWord;   // ← was guessWord, now matches frontend JSON key
}
