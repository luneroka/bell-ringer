package com.bell_ringer.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bell_ringer.models.OpenAnswer;
import com.bell_ringer.repositories.OpenAnswerRepository;
import com.bell_ringer.services.dto.OpenAnswerDto;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class OpenAnswerService {
  private final OpenAnswerRepository openAnswerRepository;

  public OpenAnswerService(OpenAnswerRepository openAnswerRepository) {
    this.openAnswerRepository = openAnswerRepository;
  }

  // ===== DTO Conversion Methods =====

  /**
   * Convert OpenAnswer entity to DTO
   */
  public OpenAnswerDto convertToDto(OpenAnswer openAnswer) {
    return OpenAnswerDto.forResponse(openAnswer);
  }

  /**
   * Convert list of OpenAnswer entities to DTOs
   */
  public List<OpenAnswerDto> convertToDtoList(List<OpenAnswer> openAnswers) {
    return openAnswers.stream()
        .map(this::convertToDto)
        .toList();
  }

  // ===== Read Operations =====

  /**
   * Get open answer by ID as DTO
   */
  @Transactional(readOnly = true)
  public Optional<OpenAnswerDto> getDto(Long id) {
    return openAnswerRepository.findById(id)
        .map(this::convertToDto);
  }

  /**
   * Get open answer by ID as DTO (throws exception if not found)
   */
  @Transactional(readOnly = true)
  public OpenAnswerDto getRequiredDto(Long id) {
    return getDto(id)
        .orElseThrow(() -> new EntityNotFoundException("OpenAnswer not found with id: " + id));
  }

  /**
   * Get all open answers for a specific question as DTOs
   */
  @Transactional(readOnly = true)
  public List<OpenAnswerDto> listByQuestionDto(Long questionId) {
    return convertToDtoList(openAnswerRepository.findByQuestionId(questionId));
  }

  /**
   * Find open answer by question ID and answer text (case-insensitive)
   */
  @Transactional(readOnly = true)
  public Optional<OpenAnswerDto> findByQuestionAndAnswerDto(Long questionId, String answer) {
    return openAnswerRepository.findByQuestionIdAndAnswerIgnoreCase(questionId, answer)
        .map(this::convertToDto);
  }

  /**
   * Check if an open answer exists for question and answer text
   * (case-insensitive)
   */
  @Transactional(readOnly = true)
  public boolean existsByQuestionAndAnswer(Long questionId, String answer) {
    return openAnswerRepository.existsByQuestionIdAndAnswerIgnoreCase(questionId, answer);
  }

  // ===== Entity Access Methods (for internal service use) =====

  /**
   * Get OpenAnswer entity by ID (for internal service use)
   */
  @Transactional(readOnly = true)
  public Optional<OpenAnswer> get(Long id) {
    return openAnswerRepository.findById(id);
  }

  /**
   * Get OpenAnswer entity by ID (throws exception if not found)
   */
  @Transactional(readOnly = true)
  public OpenAnswer getRequired(Long id) {
    return get(id)
        .orElseThrow(() -> new EntityNotFoundException("OpenAnswer not found with id: " + id));
  }

  /**
   * Get all OpenAnswer entities for a question (for internal service use)
   */
  @Transactional(readOnly = true)
  public List<OpenAnswer> listByQuestion(Long questionId) {
    return openAnswerRepository.findByQuestionId(questionId);
  }
}
