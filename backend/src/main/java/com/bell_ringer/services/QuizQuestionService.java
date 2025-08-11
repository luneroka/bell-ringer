
package com.bell_ringer.services;

import com.bell_ringer.models.Question;
import com.bell_ringer.models.Quiz;
import com.bell_ringer.models.QuizQuestion;
import com.bell_ringer.models.id.QuizQuestionId;
import com.bell_ringer.repositories.QuizQuestionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class QuizQuestionService {

    private final QuizQuestionRepository links;

    @PersistenceContext
    private EntityManager em;

    public QuizQuestionService(QuizQuestionRepository links) {
        this.links = links;
    }

    /** Return all link rows for a quiz. */
    public List<QuizQuestion> findAllByQuizId(Long quizId) {
        Objects.requireNonNull(quizId, "quizId must not be null");
        return links.findAllByQuizId(quizId);
    }

    /** Return only question IDs linked to a quiz. */
    public List<Long> findQuestionIdsByQuizId(Long quizId) {
        return findAllByQuizId(quizId).stream()
                .map(qq -> qq.getQuestion().getId())
                .toList();
    }

    /** Count how many questions are attached to a quiz. */
    public long countByQuizId(Long quizId) {
        Objects.requireNonNull(quizId, "quizId must not be null");
        return links.countByQuizId(quizId);
    }

    /** Does this quiz already contain this question? */
    public boolean exists(Long quizId, Long questionId) {
        Objects.requireNonNull(quizId, "quizId must not be null");
        Objects.requireNonNull(questionId, "questionId must not be null");
        return links.existsByQuizIdAndQuestionId(quizId, questionId);
    }

    // ------------------------- Write operations ------------------------- //

    /**
     * Add a single (quiz, question) link if absent.
     * Uses EntityManager references to avoid loading full entities.
     */
    @Transactional
    public QuizQuestion addIfAbsent(Long quizId, Long questionId) {
        Objects.requireNonNull(quizId, "quizId must not be null");
        Objects.requireNonNull(questionId, "questionId must not be null");
        if (links.existsByQuizIdAndQuestionId(quizId, questionId)) {
            // already present; return a lightweight instance
            QuizQuestion qq = new QuizQuestion();
            qq.setQuiz(em.getReference(Quiz.class, quizId));
            qq.setQuestion(em.getReference(Question.class, questionId));
            return qq;
        }
        Quiz quizRef = em.getReference(Quiz.class, quizId);
        Question questionRef = em.getReference(Question.class, questionId);
        QuizQuestion qq = new QuizQuestion(quizRef, questionRef);
        return links.save(qq);
    }

    /** Add many links; ignores duplicates within input and existing rows. */
    @Transactional
    public List<QuizQuestion> addAllIfAbsent(Long quizId, List<Long> questionIds) {
        Objects.requireNonNull(quizId, "quizId must not be null");
        if (questionIds == null || questionIds.isEmpty()) return List.of();
        Set<Long> unique = new HashSet<>(questionIds);
        List<QuizQuestion> created = new ArrayList<>(unique.size());
        for (Long qid : unique) {
            created.add(addIfAbsent(quizId, qid));
        }
        return created;
    }

    /** Remove a single link row. Returns number of rows deleted (0 or 1). */
    @Transactional
    public long remove(Long quizId, Long questionId) {
        Objects.requireNonNull(quizId, "quizId must not be null");
        Objects.requireNonNull(questionId, "questionId must not be null");
        return links.deleteByQuizIdAndQuestionId(quizId, questionId);
    }

    /** Remove all links for a quiz. Returns number of rows deleted. */
    @Transactional
    public long clear(Long quizId) {
        Objects.requireNonNull(quizId, "quizId must not be null");
        return links.deleteByQuizId(quizId);
    }

    /**
     * Sync links to exactly match the provided question ids:
     * - Adds missing links
     * - Removes extra links
     * Returns a summary of the final order of question IDs (in the order provided).
     */
    @Transactional
    public List<Long> replaceAll(Long quizId, List<Long> desiredQuestionIds) {
        Objects.requireNonNull(quizId, "quizId must not be null");
        List<Long> desired = desiredQuestionIds == null ? List.of() : desiredQuestionIds;

        // Current set
        List<Long> current = findQuestionIdsByQuizId(quizId);
        Set<Long> currentSet = new HashSet<>(current);
        Set<Long> desiredSet = new HashSet<>(desired);

        // Remove extras
        for (Long qid : currentSet) {
            if (!desiredSet.contains(qid)) {
                links.deleteByQuizIdAndQuestionId(quizId, qid);
            }
        }
        // Add missing (preserve desired input order)
        for (Long qid : desired) {
            if (!currentSet.contains(qid)) {
                addIfAbsent(quizId, qid);
            }
        }
        // Return the new ordered list
        return desired;
    }

    /** Convenience: delete by composite id. */
    @Transactional
    public void deleteById(Long quizId, Long questionId) {
        links.deleteById(new QuizQuestionId(quizId, questionId));
    }
}
