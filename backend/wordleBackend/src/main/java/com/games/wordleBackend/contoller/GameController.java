package com.games.wordleBackend.contoller;

import com.games.wordleBackend.dto.request.SubmitGuessRequest;
import com.games.wordleBackend.dto.response.StartGameResponse;
import com.games.wordleBackend.dto.response.SubmitGuessResponse;
import com.games.wordleBackend.enums.GameStatus;
import com.games.wordleBackend.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/game")
public class GameController{
    private final GameService service;
    public GameController(GameService service) {
        this.service = service;
     }
    @PostMapping("/start")
    public ResponseEntity<StartGameResponse> startGame() {
        return ResponseEntity.ok(service.startGame());
    }

    @PostMapping("/guess")
    public ResponseEntity<SubmitGuessResponse> submitGuess(@RequestBody SubmitGuessRequest request) {
        return ResponseEntity.ok(service.submitGuess(request));
    }
}
