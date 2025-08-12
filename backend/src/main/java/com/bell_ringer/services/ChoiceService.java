package com.bell_ringer.services;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bell_ringer.models.Choice;
import com.bell_ringer.repositories.ChoiceRepository;
import com.bell_ringer.services.dto.ChoiceDto;

@Service
public class ChoiceService {
  private final ChoiceRepository choiceRepository;

  public ChoiceService(ChoiceRepository choiceRepository) {
    this.choiceRepository = choiceRepository;
  }

  // Helper method to convert Choice entity to ChoiceDto
  private ChoiceDto convertToDto(Choice choice) {
    return ChoiceDto.forResponse(
        choice.getId(),
        choice.getQuestion().getId(),
        choice.getChoiceText(),
        choice.isCorrect());
  }

  // Helper method to convert list of Choice entities to list of ChoiceDtos
  private List<ChoiceDto> convertToDtoList(List<Choice> choices) {
    return choices.stream()
        .map(this::convertToDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public ChoiceDto getById(Long choiceId) {
    if (choiceId == null)
      throw new IllegalArgumentException("choiceId must not be null");
    Choice choice = choiceRepository.findById(choiceId)
        .orElseThrow(() -> new NoSuchElementException("Choice not found: id=" + choiceId));
    return convertToDto(choice);
  }

  @Transactional(readOnly = true)
  public List<ChoiceDto> getByQuestionId(Long questionId) {
    if (questionId == null)
      throw new IllegalArgumentException("questionId must not be null");
    List<Choice> choices = choiceRepository.findByQuestionId(questionId);
    return convertToDtoList(choices);
  }

  @Transactional(readOnly = true)
  public ChoiceDto.ChoiceCheckDto isCorrect(Long choiceId) {
    if (choiceId == null)
      throw new IllegalArgumentException("choiceId must not be null");
    boolean correct = choiceRepository.existsByIdAndIsCorrectTrue(choiceId);
    return ChoiceDto.check(choiceId, correct);
  }

  @Transactional(readOnly = true)
  public List<ChoiceDto> getCorrectByQuestionId(Long questionId) {
    if (questionId == null)
      throw new IllegalArgumentException("questionId must not be null");
    List<Choice> choices = choiceRepository.findByQuestionIdAndIsCorrectTrue(questionId);
    return convertToDtoList(choices);
  }

  @Transactional(readOnly = true)
  public List<Long> getCorrectChoiceIds(Long questionId) {
    if (questionId == null)
      throw new IllegalArgumentException("questionId must not be null");
    return choiceRepository.findByQuestionIdAndIsCorrectTrue(questionId)
        .stream().map(Choice::getId).toList();
  }
}