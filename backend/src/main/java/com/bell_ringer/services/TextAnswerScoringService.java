package com.bell_ringer.services;

import com.bell_ringer.models.OpenAnswer;
import com.bell_ringer.models.Question;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for automatically scoring text answers based on rubric keywords
 */
@Service
public class TextAnswerScoringService {

  /**
   * Automatically score a text answer against the question's open answer rubrics
   */
  public ScoringResult scoreTextAnswer(String userAnswer, Question question, List<OpenAnswer> openAnswers) {
    if (userAnswer == null || userAnswer.trim().isEmpty()) {
      return new ScoringResult(0, false, "Empty answer provided");
    }

    if (openAnswers == null || openAnswers.isEmpty()) {
      return new ScoringResult(60, true, "No rubric available - default pass"); // Default passing score
    }

    // Find the best matching rubric
    ScoringResult bestResult = null;
    int highestScore = 0;

    for (OpenAnswer openAnswer : openAnswers) {
      ScoringResult result = scoreAgainstRubric(userAnswer, openAnswer);
      if (result.score() > highestScore) {
        highestScore = result.score();
        bestResult = result;
      }
    }

    return bestResult != null ? bestResult : new ScoringResult(0, false, "No valid rubric found");
  }

  /**
   * Score a text answer against a specific rubric
   */
  private ScoringResult scoreAgainstRubric(String userAnswer, OpenAnswer openAnswer) {
    List<String> mustKeywords = openAnswer.getMustKeywords();
    List<String> shouldKeywords = openAnswer.getShouldKeywords();
    int minScore = openAnswer.getMinScore() != null ? openAnswer.getMinScore() : 70;

    // Normalize the user answer for matching
    String normalizedAnswer = normalizeText(userAnswer);

    // Check MUST keywords (required for passing)
    int mustMatches = 0;
    StringBuilder feedback = new StringBuilder();

    for (String keyword : mustKeywords) {
      if (containsKeyword(normalizedAnswer, keyword)) {
        mustMatches++;
      } else {
        if (feedback.length() > 0)
          feedback.append("; ");
        feedback.append("Missing required concept: ").append(keyword);
      }
    }

    // Check SHOULD keywords (bonus points)
    int shouldMatches = 0;
    for (String keyword : shouldKeywords) {
      if (containsKeyword(normalizedAnswer, keyword)) {
        shouldMatches++;
      }
    }

    // Calculate score
    int score = calculateScore(mustMatches, mustKeywords.size(), shouldMatches, shouldKeywords.size(), minScore);
    boolean isCorrect = score >= minScore;

    // Generate feedback
    String finalFeedback = generateFeedback(score, mustMatches, mustKeywords.size(),
        shouldMatches, shouldKeywords.size(), feedback.toString());

    return new ScoringResult(score, isCorrect, finalFeedback);
  }

  /**
   * Calculate the final score based on keyword matches
   */
  private int calculateScore(int mustMatches, int totalMust, int shouldMatches, int totalShould, int minScore) {
    if (totalMust == 0 && totalShould == 0) {
      return minScore; // Default score if no keywords
    }

    // MUST keywords are worth 70% of the score
    double mustWeight = 0.7;
    double shouldWeight = 0.3;

    double mustScore = totalMust > 0 ? (double) mustMatches / totalMust : 1.0;
    double shouldScore = totalShould > 0 ? (double) shouldMatches / totalShould : 1.0;

    // If no MUST keywords, SHOULD keywords get full weight
    if (totalMust == 0) {
      mustWeight = 0.0;
      shouldWeight = 1.0;
    }
    // If no SHOULD keywords, MUST keywords get full weight
    else if (totalShould == 0) {
      mustWeight = 1.0;
      shouldWeight = 0.0;
    }

    double finalScore = (mustScore * mustWeight + shouldScore * shouldWeight) * 100;

    // Ensure minimum score for complete MUST keyword coverage
    if (totalMust > 0 && mustMatches == totalMust && finalScore < minScore) {
      finalScore = minScore;
    }

    return Math.min(100, Math.max(0, (int) Math.round(finalScore)));
  }

  /**
   * Generate human-readable feedback
   */
  private String generateFeedback(int score, int mustMatches, int totalMust,
      int shouldMatches, int totalShould, String missingConcepts) {
    StringBuilder feedback = new StringBuilder();

    feedback.append("Score: ").append(score).append("/100. ");

    if (totalMust > 0) {
      feedback.append("Required concepts: ").append(mustMatches).append("/").append(totalMust).append(" found. ");
    }

    if (totalShould > 0) {
      feedback.append("Additional concepts: ").append(shouldMatches).append("/").append(totalShould).append(" found. ");
    }

    if (!missingConcepts.isEmpty()) {
      feedback.append(missingConcepts);
    } else if (mustMatches == totalMust && shouldMatches == totalShould) {
      feedback.append("Excellent answer covering all key concepts!");
    } else if (mustMatches == totalMust) {
      feedback.append("Good answer covering all required concepts.");
    }

    return feedback.toString();
  }

  /**
   * Normalize text for keyword matching
   */
  private String normalizeText(String text) {
    return text.toLowerCase()
        .replaceAll("[^a-z0-9\\s]", " ") // Remove punctuation
        .replaceAll("\\s+", " ") // Normalize whitespace
        .trim();
  }

  /**
   * Check if the text contains a keyword (flexible matching)
   */
  private boolean containsKeyword(String text, String keyword) {
    String normalizedKeyword = normalizeText(keyword);

    // Direct substring match
    if (text.contains(normalizedKeyword)) {
      return true;
    }

    // Word boundary match for exact terms
    String[] keywordParts = normalizedKeyword.split("\\s+");
    if (keywordParts.length == 1) {
      // Single word - check as whole word
      return text.matches(".*\\b" + keywordParts[0] + "\\b.*");
    } else {
      // Multi-word phrase - check if all words are present
      for (String part : keywordParts) {
        if (!text.contains(part)) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Result of text answer scoring
   */
  public record ScoringResult(
      int score,
      boolean isCorrect,
      String feedback) {
  }
}
