package com.games.wordleBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "guess_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuessAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int attemptNumber;
    private String submittedWord;

    @Column(columnDefinition = "TEXT")
    private String letterResults;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

}
