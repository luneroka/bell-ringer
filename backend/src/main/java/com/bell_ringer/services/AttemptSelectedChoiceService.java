package com.bell_ringer.services;

import com.bell_ringer.models.AttemptSelectedChoice;
import com.bell_ringer.models.Attempt;
import com.bell_ringer.models.Choice;
import com.bell_ringer.models.Question;
import com.bell_ringer.models.id.AttemptSelectedChoiceId;
import com.bell_ringer.repositories.AttemptRepository;
import com.bell_ringer.repositories.AttemptSelectedChoiceRepository;
import com.bell_ringer.repositories.ChoiceRepository;
import com.bell_ringer.repositories.QuestionRepository;
import com.bell_ringer.services.dto.AttemptSelectedChoiceDto;
import com.bell_ringer.services.dto.AttemptSelectedChoiceRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AttemptSelectedChoiceService {

  private final AttemptSelectedChoiceRepository selectedChoices;
  private final AttemptRepository attemptRepository;
  private final QuestionRepository questionRepository;
  private final ChoiceRepository choiceRepository;

  public AttemptSelectedChoiceService(AttemptSelectedChoiceRepository selectedChoices,
      AttemptRepository attemptRepository,
      QuestionRepository questionRepository,
      ChoiceRepository choiceRepository) {
    this.selectedChoices = selectedChoices;
    this.attemptRepository = attemptRepository;
    this.questionRepository = questionRepository;
    this.choiceRepository = choiceRepository;
  }

  // ===== DTO Conversion Methods =====

  /**
   * Convert AttemptSelectedChoice entity to AttemptSelectedChoiceDto.
   */
  private AttemptSelectedChoiceDto convertToDto(AttemptSelectedChoice selectedChoice) {
    return AttemptSelectedChoiceDto.forResponse(
        selectedChoice.getAttemptId(),
        selectedChoice.getQuestionId(),
        selectedChoice.getChoiceId(),
        selectedChoice.getQuiz().getId(),
        selectedChoice.getSelectedAt());
  }

  // ----------------- Basic reads -----------------

  public Optional<AttemptSelectedChoice> findById(AttemptSelectedChoiceId id) {
    return selectedChoices.findById(id);
  }

  public AttemptSelectedChoice getRequired(AttemptSelectedChoiceId id) {
    return selectedChoices.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Selected choice not found: " + id));
  }

  public AttemptSelectedChoiceDto getRequiredDto(AttemptSelectedChoiceId id) {
    AttemptSelectedChoice selectedChoice = getRequired(id);
    return convertToDto(selectedChoice);
  }

  // ----------------- Query methods -----------------

  public List<AttemptSelectedChoiceDto> findByAttemptId(Long attemptId) {
    return selectedChoices.findByAttemptId(attemptId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public List<AttemptSelectedChoiceDto> findByAttemptIdAndQuestionId(Long attemptId, Long questionId) {
    return selectedChoices.findByAttemptIdAndQuestionId(attemptId, questionId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public long countByAttemptId(Long attemptId) {
    return selectedChoices.countByAttemptId(attemptId);
  }

  // ----------------- Submit choices -----------------

  /**
   * Submit a single choice selection for an attempt
   */
  @Transactional
  public AttemptSelectedChoiceDto submitChoice(AttemptSelectedChoiceRequest.Submit request) {
    Objects.requireNonNull(request, "request must not be null");
    Objects.requireNonNull(request.attemptId(), "attemptId must not be null");
    Objects.requireNonNull(request.questionId(), "questionId must not be null");
    Objects.requireNonNull(request.choiceId(), "choiceId must not be null");

    // Validate attempt exists and is not completed
    Attempt attempt = attemptRepository.findById(request.attemptId())
        .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + request.attemptId()));
    if (attempt.isCompleted()) {
      throw new IllegalStateException("Cannot submit choices for completed attempt: " + request.attemptId());
    }

    // Validate question and choice exist
    Question question = questionRepository.findById(request.questionId())
        .orElseThrow(() -> new IllegalArgumentException("Question not found: " + request.questionId()));
    Choice choice = choiceRepository.findById(request.choiceId())
        .orElseThrow(() -> new IllegalArgumentException("Choice not found: " + request.choiceId()));

    // Validate choice belongs to question
    if (!choice.getQuestion().getId().equals(question.getId())) {
      throw new IllegalArgumentException(
          "Choice " + request.choiceId() + " does not belong to question " + request.questionId());
    }

    // Create selected choice
    AttemptSelectedChoice selectedChoice = new AttemptSelectedChoice();
    selectedChoice.setAttemptId(request.attemptId());
    selectedChoice.setQuestionId(request.questionId());
    selectedChoice.setChoiceId(request.choiceId());
    selectedChoice.setAttempt(attempt);
    selectedChoice.setQuiz(attempt.getQuiz());
    selectedChoice.setQuestion(question);
    selectedChoice.setChoice(choice);

    AttemptSelectedChoice savedChoice = selectedChoices.save(selectedChoice);
    return convertToDto(savedChoice);
  }

  /**
   * Submit multiple choice selections in batch
   */
  @Transactional
  public List<AttemptSelectedChoiceDto> submitChoicesBatch(AttemptSelectedChoiceRequest.SubmitBatch request) {
    Objects.requireNonNull(request, "request must not be null");
    Objects.requireNonNull(request.attemptId(), "attemptId must not be null");

    // Validate attempt exists and is not completed
    Attempt attempt = attemptRepository.findById(request.attemptId())
        .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + request.attemptId()));
    if (attempt.isCompleted()) {
      throw new IllegalStateException("Cannot submit choices for completed attempt: " + request.attemptId());
    }

    List<AttemptSelectedChoiceDto> results = request.selectedChoices().stream()
        .map(choice -> {
          AttemptSelectedChoiceRequest.Submit submitRequest = new AttemptSelectedChoiceRequest.Submit(
              request.attemptId(), choice.questionId(), choice.choiceId());
          return submitChoice(submitRequest);
        })
        .collect(Collectors.toList());

    return results;
  }

  /**
   * Update a choice selection (change from one choice to another for the same
   * question)
   */
  @Transactional
  public AttemptSelectedChoiceDto updateChoice(Long attemptId, Long questionId, Long oldChoiceId,
      AttemptSelectedChoiceRequest.Update request) {
    Objects.requireNonNull(attemptId, "attemptId must not be null");
    Objects.requireNonNull(questionId, "questionId must not be null");
    Objects.requireNonNull(oldChoiceId, "oldChoiceId must not be null");
    Objects.requireNonNull(request, "request must not be null");

    // Validate attempt is not completed
    Attempt attempt = attemptRepository.findById(attemptId)
        .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));
    if (attempt.isCompleted()) {
      throw new IllegalStateException("Cannot update choices for completed attempt: " + attemptId);
    }

    // Remove old choice selection
    AttemptSelectedChoiceId oldId = new AttemptSelectedChoiceId(attemptId, questionId, oldChoiceId);
    selectedChoices.deleteById(oldId);

    // Add new choice selection
    AttemptSelectedChoiceRequest.Submit submitRequest = new AttemptSelectedChoiceRequest.Submit(attemptId, questionId,
        request.newChoiceId());
    return submitChoice(submitRequest);
  }

  // ----------------- Delete operations -----------------

  /**
   * Remove a specific choice selection
   */
  @Transactional
  public void removeChoice(Long attemptId, Long questionId, Long choiceId) {
    Objects.requireNonNull(attemptId, "attemptId must not be null");
    Objects.requireNonNull(questionId, "questionId must not be null");
    Objects.requireNonNull(choiceId, "choiceId must not be null");

    // Validate attempt is not completed
    Attempt attempt = attemptRepository.findById(attemptId)
        .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));
    if (attempt.isCompleted()) {
      throw new IllegalStateException("Cannot remove choices from completed attempt: " + attemptId);
    }

    AttemptSelectedChoiceId id = new AttemptSelectedChoiceId(attemptId, questionId, choiceId);
    selectedChoices.deleteById(id);
  }

  /**
   * Remove all choice selections for an attempt (cleanup)
   */
  @Transactional
  public void removeAllChoicesForAttempt(Long attemptId) {
    Objects.requireNonNull(attemptId, "attemptId must not be null");
    selectedChoices.deleteByAttemptId(attemptId);
  }

  // ----------------- Analytics methods -----------------

  /**
   * Check if a user has answered a specific question in an attempt
   */
  public boolean hasAnsweredQuestion(Long attemptId, Long questionId) {
    return !selectedChoices.findByAttemptIdAndQuestionId(attemptId, questionId).isEmpty();
  }

  /**
   * Get the number of questions answered in an attempt
   */
  public long getAnsweredQuestionCount(Long attemptId) {
    return selectedChoices.findByAttemptId(attemptId).stream()
        .map(AttemptSelectedChoice::getQuestionId)
        .distinct()
        .count();
  }
}
