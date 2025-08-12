
package com.bell_ringer.services;

import com.bell_ringer.models.Category;
import com.bell_ringer.models.Quiz;
import com.bell_ringer.repositories.QuizRepository;
import com.bell_ringer.repositories.QuizRepository.DifficultyStatsRow;
import com.bell_ringer.services.dto.QuizDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizzes;
    private final CategoryService categories;

    @PersistenceContext
    private EntityManager em;

    public QuizService(QuizRepository quizzes, CategoryService categories) {
        this.quizzes = quizzes;
        this.categories = categories;
    }

    // ===== DTO Conversion Methods =====

    /**
     * Convert Quiz entity to QuizDto without question IDs (for performance).
     */
    private QuizDto convertToDto(Quiz quiz) {
        return QuizDto.forResponse(
                quiz.getId(),
                quiz.getUserId(),
                quiz.getCategory().getId(),
                quiz.getCategory().getName(),
                quiz.getCreatedAt(),
                quiz.getCompletedAt());
    }

    /**
     * Convert Quiz entity to QuizDto with question IDs included.
     */
    private QuizDto convertToDtoWithQuestions(Quiz quiz, List<Long> questionIds) {
        return QuizDto.forResponseWithQuestions(
                quiz.getId(),
                quiz.getUserId(),
                quiz.getCategory().getId(),
                quiz.getCategory().getName(),
                questionIds,
                quiz.getCreatedAt(),
                quiz.getCompletedAt());
    }

    // ----------------- Basic reads -----------------

    public Optional<Quiz> findById(Long id) {
        return quizzes.findById(id);
    }

    public Quiz getRequired(Long id) {
        return quizzes.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + id));
    }

    public QuizDto getRequiredDto(Long id) {
        Quiz quiz = getRequired(id);
        return convertToDto(quiz);
    }

    // ----------------- Creation & linkage -----------------

    /**
     * Create a quiz for the given user and category (no questions attached yet).
     */
    @Transactional
    public Quiz create(UUID userId, Long categoryId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        Category category = categories.getRequired(categoryId);
        Quiz q = new Quiz(userId, category);
        return quizzes.save(q);
    }

    /**
     * Create a quiz and attach the given question ids into the link table
     * `quiz_questions`.
     * This uses a native batch insert for simplicity.
     */
    @Transactional
    public Quiz createWithQuestions(UUID userId, Long categoryId, List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            throw new IllegalArgumentException("questionIds must not be empty");
        }
        Quiz quiz = create(userId, categoryId);
        addQuestions(quiz.getId(), questionIds);
        return quiz;
    }

    /**
     * Attach questions to an existing quiz via native inserts.
     */
    @Transactional
    public void addQuestions(Long quizId, List<Long> questionIds) {
        Objects.requireNonNull(quizId, "quizId must not be null");
        if (questionIds == null || questionIds.isEmpty())
            return;

        // TODO : enforce same-category policy in service (cheap guard)

        // Batch insert
        for (Long qid : questionIds) {
            em.createNativeQuery(
                    "INSERT INTO quiz_questions(quiz_id, question_id) VALUES (:quizId, :qid) ON CONFLICT DO NOTHING")
                    .setParameter("quizId", quizId)
                    .setParameter("qid", qid)
                    .executeUpdate();
        }
    }

    // ----------------- Completion -----------------

    /** Mark a quiz as completed now. */
    @Transactional
    public Quiz markCompleted(Long quizId) {
        Quiz quiz = getRequired(quizId);
        quiz.setCompletedAt(OffsetDateTime.now());
        return quizzes.save(quiz);
    }

    /** Mark a quiz as completed now and return DTO. */
    @Transactional
    public QuizDto markCompletedDto(Long quizId) {
        Quiz quiz = markCompleted(quizId);
        return convertToDto(quiz);
    }

    /** Clear completion flag (admin/debug). */
    @Transactional
    public Quiz clearCompleted(Long quizId) {
        Quiz quiz = getRequired(quizId);
        quiz.setCompletedAt(null);
        return quizzes.save(quiz);
    }

    // ----------------- History & accuracy -----------------

    /** Count completed quizzes by user & category (uses quizzes.completed_at). */
    public long countCompletedByUserAndCategory(UUID userId, Long categoryId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        return quizzes.countCompletedByUserAndCategoryWithCompletedAt(userId, categoryId);
    }

    /** DTO for per-difficulty accuracy. Values are in [0,1]. */
    public record Accuracy(double easy, double medium, double hard) {
    }

    /** Load accuracy per difficulty for a user in a category. */
    public Accuracy loadAccuracy(UUID userId, Long categoryId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(categoryId, "categoryId must not be null");

        List<DifficultyStatsRow> rows = quizzes.findAccuracyByUserAndCategory(userId, categoryId);
        long eTot = 0, eCor = 0, mTot = 0, mCor = 0, hTot = 0, hCor = 0;
        for (DifficultyStatsRow r : rows) {
            String d = r.getDifficulty();
            if (d == null)
                continue;
            switch (d.toUpperCase()) {
                case "EASY" -> {
                    eTot += r.getTotal();
                    eCor += r.getCorrect();
                }
                case "MEDIUM" -> {
                    mTot += r.getTotal();
                    mCor += r.getCorrect();
                }
                case "HARD" -> {
                    hTot += r.getTotal();
                    hCor += r.getCorrect();
                }
            }
        }
        double e = eTot == 0 ? 0.5 : (double) eCor / eTot;
        double m = mTot == 0 ? 0.5 : (double) mCor / mTot;
        double h = hTot == 0 ? 0.5 : (double) hCor / hTot;
        return new Accuracy(e, m, h);
    }
}
