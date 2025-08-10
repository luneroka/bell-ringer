package com.bell_ringer.repositories;

import com.bell_ringer.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

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
  @Query(value = """
      SELECT * FROM questions
      WHERE (:categoryIds IS NULL OR category_id = ANY(:categoryIds))
        AND (:typeName   IS NULL OR type = :typeName)
        AND (:diffName   IS NULL OR difficulty = :diffName)
      ORDER BY random()
      LIMIT :limit
      """,
      nativeQuery = true)
  List<Question> pickRandomFilteredMany(@Param("categoryIds") Long[] categoryIds,
                                        @Param("typeName") String typeName,
                                        @Param("diffName") String diffName,
                                        @Param("limit") int limit);
}
