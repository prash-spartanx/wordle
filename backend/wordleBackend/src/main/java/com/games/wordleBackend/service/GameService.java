package com.games.wordleBackend.service;

import com.games.wordleBackend.dto.request.SubmitGuessRequest;
import com.games.wordleBackend.dto.response.LetterResult;
import com.games.wordleBackend.dto.response.StartGameResponse;
import com.games.wordleBackend.dto.response.SubmitGuessResponse;
import com.games.wordleBackend.enums.GameStatus;
import com.games.wordleBackend.enums.GuessStatus;
import com.games.wordleBackend.model.GuessAttempt;
import com.games.wordleBackend.model.Session;
import com.games.wordleBackend.repository.GuessAttemptRepository;
import com.games.wordleBackend.repository.SessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.games.wordleBackend.util.WordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final SessionRepository sessionRepository;
    private final GuessAttemptRepository guessAttemptRepository;
    private final ObjectMapper objectMapper;
    // in GameService constructor — add WordValidator
    private final WordValidator wordValidator;

    public GameService(SessionRepository sessionRepository,
                       GuessAttemptRepository guessAttemptRepository,
                       ObjectMapper objectMapper,
                       WordValidator wordValidator) {
        this.sessionRepository = sessionRepository;
        this.guessAttemptRepository = guessAttemptRepository;
        this.objectMapper = objectMapper;
        this.wordValidator = wordValidator;
    }

    private static final List<String> WORD_BANK = List.of(
            "APPLE", "BERRY", "MANGO", "PEACH", "GRAPE",
            "LEMON", "PEARL", "PLUMS", "CRANE", "SLATE"
    );

    // ─── PUBLIC METHODS ───────────────────────────────────────────────

    public StartGameResponse startGame() {
        log.info(">> startGame called");

        Session session = new Session();
        session.setSessionId(UUID.randomUUID().toString());
        session.setTargetWord(pickRandomWord());
        session.setCurrentAttempt(0);
        session.setMaxAttempts(6);
        session.setWordGuessed(false);
        session.setGameStatus(GameStatus.IN_PROGRESS);

        sessionRepository.save(session);

        log.info("<< startGame done | sessionId={} | targetWord={} | status={}",
                session.getSessionId(), session.getTargetWord(), session.getGameStatus());

        return new StartGameResponse(
                session.getSessionId(),
                session.getGameStatus(),
                session.getCurrentAttempt(),
                session.getMaxAttempts()
        );
    }

    public SubmitGuessResponse submitGuess(SubmitGuessRequest request) {
        log.info(">> submitGuess received | sessionId={} | guessedWord={}",
                request.getSessionId(), request.getGuessedWord());

        // 1. fetch session
        Session session = sessionRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> {
                    log.error("Session not found: {}", request.getSessionId());
                    return new RuntimeException("Session not found: " + request.getSessionId());
                });

        log.info("   session found | currentAttempt={} | maxAttempts={} | gameStatus={}",
                session.getCurrentAttempt(), session.getMaxAttempts(), session.getGameStatus());

        // 2. guard — game must be in progress
        if (session.getGameStatus() != GameStatus.IN_PROGRESS) {
            log.warn("   game already over | status={}", session.getGameStatus());
            throw new IllegalStateException("Game is already over. Status: " + session.getGameStatus());
        }

        // 3. validate guess
        String guess = request.getGuessedWord().toUpperCase();
        if (guess.length() != 5) {
            log.warn("   invalid guess length={}", guess.length());
            throw new IllegalArgumentException("Guess must be exactly 5 letters.");
        }

        if (!wordValidator.isValid(guess)) {
            log.warn("   invalid word attempted: {}", guess);
            throw new IllegalArgumentException("Not a valid English word.");
        }

        // 4. evaluate
        List<LetterResult> letterResults = evaluateGuess(guess, session.getTargetWord());
        log.info("   evaluateGuess done | results={}", letterResults);

        // 5. save attempt
        GuessAttempt attempt = new GuessAttempt();
        attempt.setAttemptNumber(session.getCurrentAttempt() + 1);
        attempt.setSubmittedWord(guess);
        attempt.setLetterResults(toJson(letterResults));
        attempt.setSession(session);
        guessAttemptRepository.save(attempt);
        log.info("   attempt saved | attemptNumber={}", attempt.getAttemptNumber());

        // 6. increment attempt — THIS IS CRITICAL
        session.setCurrentAttempt(session.getCurrentAttempt() + 1);
        log.info("   currentAttempt incremented to {}", session.getCurrentAttempt());

        // 7. check win
        boolean wordGuessed = letterResults.stream()
                .allMatch(result -> result.getGuessStatus() == GuessStatus.CORRECT);

        if (wordGuessed) {
            session.setWordGuessed(true);
            session.setGameStatus(GameStatus.WON);
            log.info("   result=WON");
        } else if (session.getCurrentAttempt() >= session.getMaxAttempts()) {
            session.setGameStatus(GameStatus.LOST);
            log.info("   result=LOST | used all {} attempts", session.getMaxAttempts());
        } else {
            log.info("   result=IN_PROGRESS | attemptsLeft={}",
                    session.getMaxAttempts() - session.getCurrentAttempt());
        }

        // 8. save session
        sessionRepository.save(session);

        int attemptsRemaining = session.getMaxAttempts() - session.getCurrentAttempt();
        String revealedWord = null;
        if (wordGuessed || session.getGameStatus() == GameStatus.LOST) {
            revealedWord = session.getTargetWord();
        }

        SubmitGuessResponse response = new  SubmitGuessResponse(
                letterResults,
                wordGuessed,
                session.getCurrentAttempt(),
                attemptsRemaining,
                session.getGameStatus(),
                revealedWord        // ← add this
        );

        log.info("<< submitGuess response | gameStatus={} | attemptsRemaining={} | wordGuessed={}",
                response.getGameStatus(), response.getAttemptsRemaining(), response.isWordGuessed());

        return response;
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────────────

    private List<LetterResult> evaluateGuess(String guess, String targetWord) {
        List<LetterResult> results = new ArrayList<>();
        boolean[] targetUsed  = new boolean[5];
        boolean[] guessMatched = new boolean[5];

        // pass 1 — CORRECT
        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == targetWord.charAt(i)) {
                results.add(new LetterResult(i, String.valueOf(guess.charAt(i)), GuessStatus.CORRECT));
                targetUsed[i]  = true;
                guessMatched[i] = true;
            } else {
                results.add(null);
            }
        }

        // pass 2 — EXISTS or INCORRECT
        for (int i = 0; i < 5; i++) {
            if (guessMatched[i]) continue;
            char guessChar = guess.charAt(i);
            boolean found = false;
            for (int j = 0; j < 5; j++) {
                if (!targetUsed[j] && targetWord.charAt(j) == guessChar) {
                    targetUsed[j] = true;
                    found = true;
                    break;
                }
            }
            results.set(i, new LetterResult(
                    i,
                    String.valueOf(guessChar),
                    found ? GuessStatus.EXISTS : GuessStatus.INCORRECT
            ));
        }

        return results;
    }

    private String pickRandomWord() {
        return WORD_BANK.get((int) (Math.random() * WORD_BANK.size()));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }
}