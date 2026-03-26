package com.games.wordleBackend.dto.response;

import com.games.wordleBackend.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartGameResponse {
    private String sessionId;
    private GameStatus gameStatus;
    private int currentAttempt;
    private int maxAttempts;
}
