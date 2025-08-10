
package com.bell_ringer.repositories;

import com.bell_ringer.models.Category;
import com.bell_ringer.models.Question;
import com.bell_ringer.models.Question.Difficulty;
import com.bell_ringer.models.Question.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

  // Pagination-friendly queries
  Page<Question> findAllByType(Type type, Pageable pageable);
  Page<Question> findAllByDifficulty(Difficulty difficulty, Pageable pageable);
  Page<Question> findAllByCategory(Category category, Pageable pageable);
  Page<Question> findAllByCategoryId(Long categoryId, Pageable pageable);

  // Combined filters
  Page<Question> findAllByTypeAndDifficulty(Type type, Difficulty difficulty, Pageable pageable);
  Page<Question> findAllByCategoryIdAndType(Long categoryId, Type type, Pageable pageable);
  Page<Question> findAllByCategoryIdAndDifficulty(Long categoryId, Difficulty difficulty, Pageable pageable);
  Page<Question> findAllByCategoryIdAndTypeAndDifficulty(Long categoryId, Type type, Difficulty difficulty, Pageable pageable);

  // Metrics & guards
  long countByCategoryId(Long categoryId);
  boolean existsByCategoryId(Long categoryId);

  // Random selection (Postgres)
  @Query(value = """
      SELECT * FROM questions
      ORDER BY random()
      LIMIT :limit
      """,
      nativeQuery = true)
  List<Question> pickRandom(@Param("limit") int limit);

  @Query(value = """
      SELECT * FROM questions
      WHERE (:categoryId IS NULL OR category_id = :categoryId)
        AND (:typeName   IS NULL OR type = :typeName)
        AND (:diffName   IS NULL OR difficulty = :diffName)
      ORDER BY random()
      LIMIT :limit
      """,
      nativeQuery = true)
  List<Question> pickRandomFiltered(@Param("categoryId") Long categoryId,
                                    @Param("typeName") String typeName,
                                    @Param("diffName") String diffName,
                                    @Param("limit") int limit);
}
