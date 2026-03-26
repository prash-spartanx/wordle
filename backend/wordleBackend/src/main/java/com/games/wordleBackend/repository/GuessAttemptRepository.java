package com.games.wordleBackend.repository;

import com.games.wordleBackend.model.GuessAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuessAttemptRepository extends JpaRepository<GuessAttempt, Long> {
}
