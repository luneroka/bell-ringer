package com.bell_ringer.services;

import com.bell_ringer.models.AttemptTextAnswer;
import com.bell_ringer.models.Attempt;
import com.bell_ringer.models.Question;
import com.bell_ringer.models.id.AttemptTextAnswerId;
import com.bell_ringer.repositories.AttemptTextAnswerRepository;
import com.bell_ringer.repositories.QuestionRepository;
import com.bell_ringer.services.dto.AttemptTextAnswerDto;
import com.bell_ringer.services.dto.AttemptTextAnswerRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AttemptTextAnswerService {

  private final AttemptTextAnswerRepository textAnswers;
  private final AttemptService attemptService;
  private final QuestionRepository questionRepository;

  public AttemptTextAnswerService(AttemptTextAnswerRepository textAnswers,
      AttemptService attemptService,
      QuestionRepository questionRepository) {
    this.textAnswers = textAnswers;
    this.attemptService = attemptService;
    this.questionRepository = questionRepository;
  }

  // ===== DTO Conversion Methods =====

  /**
   * Convert AttemptTextAnswer entity to AttemptTextAnswerDto.
   */
  private AttemptTextAnswerDto convertToDto(AttemptTextAnswer textAnswer) {
    return AttemptTextAnswerDto.forResponse(
        textAnswer.getAttemptId(),
        textAnswer.getQuestionId(),
        textAnswer.getQuiz().getId(),
        textAnswer.getAnswerText(),
        textAnswer.getScore(),
        textAnswer.getIsCorrect(),
        textAnswer.getFeedback(),
        textAnswer.getAnsweredAt());
  }

  // ----------------- Basic reads -----------------

  public Optional<AttemptTextAnswer> findById(AttemptTextAnswerId id) {
    return textAnswers.findById(id);
  }

  public AttemptTextAnswer getRequired(AttemptTextAnswerId id) {
    return textAnswers.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Text answer not found: " + id));
  }

  public AttemptTextAnswerDto getRequiredDto(AttemptTextAnswerId id) {
    AttemptTextAnswer textAnswer = getRequired(id);
    return convertToDto(textAnswer);
  }

  public Optional<AttemptTextAnswerDto> findByAttemptIdAndQuestionId(Long attemptId, Long questionId) {
    return textAnswers.findByAttemptIdAndQuestionId(attemptId, questionId)
        .map(this::convertToDto);
  }

  // ----------------- Query methods -----------------

  public List<AttemptTextAnswerDto> findByAttemptId(Long attemptId) {
    return textAnswers.findByAttemptId(attemptId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public List<AttemptTextAnswerDto> findUnscored() {
    return textAnswers.findUnscored().stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public List<AttemptTextAnswerDto> findUnscoredByAttemptId(Long attemptId) {
    return textAnswers.findUnscoredByAttemptId(attemptId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public long countByAttemptId(Long attemptId) {
    return textAnswers.countByAttemptId(attemptId);
  }

  // ----------------- Submit text answers -----------------

  /**
   * Submit a single text answer for an attempt
   */
  @Transactional
  public AttemptTextAnswerDto submitTextAnswer(AttemptTextAnswerRequest.Submit request) {
    Objects.requireNonNull(request, "request must not be null");
    Objects.requireNonNull(request.attemptId(), "attemptId must not be null");
    Objects.requireNonNull(request.questionId(), "questionId must not be null");
    Objects.requireNonNull(request.answerText(), "answerText must not be null");

    // Validate attempt exists and is not completed
    Attempt attempt = attemptService.getRequired(request.attemptId());
    if (attempt.isCompleted()) {
      throw new IllegalStateException("Cannot submit text answers for completed attempt: " + request.attemptId());
    }

    // Validate question exists
    Question question = questionRepository.findById(request.questionId())
        .orElseThrow(() -> new IllegalArgumentException("Question not found: " + request.questionId()));

    // Check if answer already exists for this attempt/question
    Optional<AttemptTextAnswer> existingAnswer = textAnswers.findByAttemptIdAndQuestionId(
        request.attemptId(), request.questionId());

    if (existingAnswer.isPresent()) {
      throw new IllegalStateException("Text answer already exists for attempt " +
          request.attemptId() + " and question " + request.questionId() +
          ". Use update method to modify existing answer.");
    }

    // Create text answer
    AttemptTextAnswer textAnswer = new AttemptTextAnswer();
    textAnswer.setAttemptId(request.attemptId());
    textAnswer.setQuestionId(request.questionId());
    textAnswer.setAnswerText(request.answerText());
    textAnswer.setAttempt(attempt);
    textAnswer.setQuiz(attempt.getQuiz());
    textAnswer.setQuestion(question);

    AttemptTextAnswer savedAnswer = textAnswers.save(textAnswer);
    return convertToDto(savedAnswer);
  }

  /**
   * Submit multiple text answers in batch
   */
  @Transactional
  public List<AttemptTextAnswerDto> submitTextAnswersBatch(AttemptTextAnswerRequest.SubmitBatch request) {
    Objects.requireNonNull(request, "request must not be null");
    Objects.requireNonNull(request.attemptId(), "attemptId must not be null");

    // Validate attempt exists and is not completed
    Attempt attempt = attemptService.getRequired(request.attemptId());
    if (attempt.isCompleted()) {
      throw new IllegalStateException("Cannot submit text answers for completed attempt: " + request.attemptId());
    }

    List<AttemptTextAnswerDto> results = request.textAnswers().stream()
        .map(textAnswer -> {
          AttemptTextAnswerRequest.Submit submitRequest = new AttemptTextAnswerRequest.Submit(request.attemptId(),
              textAnswer.questionId(), textAnswer.answerText());
          return submitTextAnswer(submitRequest);
        })
        .collect(Collectors.toList());

    return results;
  }

  /**
   * Update an existing text answer (before grading)
   */
  @Transactional
  public AttemptTextAnswerDto updateTextAnswer(Long attemptId, Long questionId,
      AttemptTextAnswerRequest.Update request) {
    Objects.requireNonNull(attemptId, "attemptId must not be null");
    Objects.requireNonNull(questionId, "questionId must not be null");
    Objects.requireNonNull(request, "request must not be null");

    // Validate attempt is not completed
    Attempt attempt = attemptService.getRequired(attemptId);
    if (attempt.isCompleted()) {
      throw new IllegalStateException("Cannot update text answers for completed attempt: " + attemptId);
    }

    // Find existing answer
    AttemptTextAnswer existingAnswer = textAnswers.findByAttemptIdAndQuestionId(attemptId, questionId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Text answer not found for attempt " + attemptId + " and question " + questionId));

    // Check if already graded
    if (existingAnswer.getScore() != null || existingAnswer.getIsCorrect() != null) {
      throw new IllegalStateException("Cannot update text answer that has already been graded");
    }

    // Update answer text
    existingAnswer.setAnswerText(request.answerText());
    AttemptTextAnswer savedAnswer = textAnswers.save(existingAnswer);

    return convertToDto(savedAnswer);
  }

  // ----------------- Grading operations -----------------

  /**
   * Grade a text answer (admin/teacher functionality)
   */
  @Transactional
  public AttemptTextAnswerDto gradeTextAnswer(Long attemptId, Long questionId,
      AttemptTextAnswerRequest.Grade request) {
    Objects.requireNonNull(attemptId, "attemptId must not be null");
    Objects.requireNonNull(questionId, "questionId must not be null");
    Objects.requireNonNull(request, "request must not be null");

    // Find existing answer
    AttemptTextAnswer existingAnswer = textAnswers.findByAttemptIdAndQuestionId(attemptId, questionId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Text answer not found for attempt " + attemptId + " and question " + questionId));

    // Update grading information
    existingAnswer.setScore(request.score());
    existingAnswer.setIsCorrect(request.isCorrect());
    existingAnswer.setFeedback(request.feedback());

    AttemptTextAnswer savedAnswer = textAnswers.save(existingAnswer);
    return convertToDto(savedAnswer);
  }

  // ----------------- Delete operations -----------------

  /**
   * Remove a specific text answer
   */
  @Transactional
  public void removeTextAnswer(Long attemptId, Long questionId) {
    Objects.requireNonNull(attemptId, "attemptId must not be null");
    Objects.requireNonNull(questionId, "questionId must not be null");

    // Validate attempt is not completed
    Attempt attempt = attemptService.getRequired(attemptId);
    if (attempt.isCompleted()) {
      throw new IllegalStateException("Cannot remove text answers from completed attempt: " + attemptId);
    }

    AttemptTextAnswerId id = new AttemptTextAnswerId(attemptId, questionId);
    textAnswers.deleteById(id);
  }

  /**
   * Remove all text answers for an attempt (cleanup)
   */
  @Transactional
  public void removeAllTextAnswersForAttempt(Long attemptId) {
    Objects.requireNonNull(attemptId, "attemptId must not be null");
    textAnswers.deleteByAttemptId(attemptId);
  }

  // ----------------- Analytics methods -----------------

  /**
   * Check if a user has answered a specific question in an attempt
   */
  public boolean hasAnsweredQuestion(Long attemptId, Long questionId) {
    return textAnswers.findByAttemptIdAndQuestionId(attemptId, questionId).isPresent();
  }

  /**
   * Get the number of text questions answered in an attempt
   */
  public long getAnsweredTextQuestionCount(Long attemptId) {
    return textAnswers.countByAttemptId(attemptId);
  }

  /**
   * Get the number of graded text answers for an attempt
   */
  public long getGradedTextAnswerCount(Long attemptId) {
    return textAnswers.findByAttemptId(attemptId).stream()
        .filter(answer -> answer.getScore() != null || answer.getIsCorrect() != null)
        .count();
  }

  /**
   * Check if all text answers for an attempt are graded
   */
  public boolean areAllTextAnswersGraded(Long attemptId) {
    List<AttemptTextAnswer> answers = textAnswers.findByAttemptId(attemptId);
    return answers.stream()
        .allMatch(answer -> answer.getScore() != null || answer.getIsCorrect() != null);
  }
}
