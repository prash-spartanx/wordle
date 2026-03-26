package com.games.wordleBackend.dto.response;

import com.games.wordleBackend.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitGuessResponse {
    private List<LetterResult> letterResults;
    private boolean wordGuessed;
    private int currentAttempt;
    private int attemptsRemaining;
    private GameStatus gameStatus;
    private String revealedWord;
}
