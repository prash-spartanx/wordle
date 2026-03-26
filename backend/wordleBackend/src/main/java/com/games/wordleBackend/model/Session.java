package com.games.wordleBackend.model;

import com.games.wordleBackend.enums.GameStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sessionId;

    private String targetWord;
    private int currentAttempt;
    private int maxAttempts;
    private boolean wordGuessed;

    @Enumerated(EnumType.STRING)
    private GameStatus gameStatus;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GuessAttempt> guessAttempts;
}

