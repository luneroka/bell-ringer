import { FaCheckSquare } from 'react-icons/fa';
import { TiDelete } from 'react-icons/ti';

// Normalize question type to lowercase for consistent handling
const normalizeType = (type) => type?.toLowerCase();

// Extract choice text regardless of API format
const getChoiceText = (choice) => {
  if (typeof choice === 'string') return choice;
  return choice.choiceText || choice.text || String(choice);
};

export function renderQuestionContent(
  type,
  choices,
  index,
  userAnswers = [],
  setUserAnswers,
  submitted = false
) {
  const normalizedType = normalizeType(type);

  const handleChoiceChange = (value, isChecked = true) => {
    if (submitted) return;

    if (normalizedType === 'multiple_choice') {
      if (isChecked) {
        setUserAnswers((prev) => [...prev, value]);
      } else {
        setUserAnswers((prev) => prev.filter((answer) => answer !== value));
      }
    } else {
      // For unique_choice and true_false, only one selection allowed
      setUserAnswers([value]);
    }
  };

  const handleTextChange = (value) => {
    if (submitted) return;
    setUserAnswers([value]);
  };

  // Handle choice-based questions (multiple_choice, unique_choice, true_false)
  if (
    ['multiple_choice', 'unique_choice', 'true_false'].includes(normalizedType)
  ) {
    return (
      <div className='choices-container'>
        {choices && choices.length > 0 ? (
          choices.map((choice, idx) => {
            const choiceValue = getChoiceText(choice);
            const isChecked = userAnswers.includes(choiceValue);
            const inputType =
              normalizedType === 'multiple_choice' ? 'checkbox' : 'radio';

            return (
              <div key={idx} className='choice-option'>
                <input
                  type={inputType}
                  id={`choice-${index}-${idx}`}
                  name={`question-${index}`}
                  value={choiceValue}
                  checked={isChecked}
                  onChange={(e) =>
                    handleChoiceChange(choiceValue, e.target.checked)
                  }
                  disabled={submitted}
                />
                <label
                  htmlFor={`choice-${index}-${idx}`}
                  className='choice-label'
                >
                  {choiceValue}
                </label>
              </div>
            );
          })
        ) : (
          <div
            style={{
              color: 'red',
              padding: '10px',
              border: '1px solid red',
              borderRadius: '4px',
            }}
          >
            <strong>No choices available!</strong>
            <br />
            Type: {type}
            <br />
            Choices: {JSON.stringify(choices)}
          </div>
        )}
      </div>
    );
  }

  // Handle short answer questions
  if (normalizedType === 'short_answer') {
    const handleAutoResize = (e) => {
      e.target.style.height = 'auto';
      e.target.style.height = e.target.scrollHeight + 'px';
      handleTextChange(e.target.value);
    };

    return (
      <div className='short-answer-container'>
        <textarea
          className='form-control short-answer-input'
          placeholder='Type your answer here...'
          rows='1'
          onInput={handleAutoResize}
          value={userAnswers[0] || ''}
          disabled={submitted}
          style={{ overflow: 'hidden' }}
        />
      </div>
    );
  }

  // Fallback for unknown types
  return <div>Unsupported question type: {type}</div>;
}

export function renderAnswerContent(
  type,
  correctChoices,
  answerText,
  userAnswers = []
) {
  const normalizedType = normalizeType(type);

  // Validate answers based on question type
  const checkAnswersCorrect = () => {
    if (!userAnswers || userAnswers.length === 0 || !correctChoices)
      return false;

    if (normalizedType === 'multiple_choice') {
      // For multiple choice: user must select ALL correct answers and NO wrong ones
      const correctAnswers = correctChoices
        .filter((choice) => choice.isCorrect)
        .map((choice) => getChoiceText(choice));

      return (
        correctAnswers.length === userAnswers.length &&
        correctAnswers.every((answer) => userAnswers.includes(answer))
      );
    }

    if (['unique_choice', 'true_false'].includes(normalizedType)) {
      // For unique choice and true/false: user must select exactly one correct answer
      const correctChoice = correctChoices.find((choice) => choice.isCorrect);
      if (!correctChoice) return false;

      const correctAnswer = getChoiceText(correctChoice);
      return userAnswers.length === 1 && userAnswers[0] === correctAnswer;
    }

    return false; // Short answer can't be validated client-side
  };

  const isCorrect = checkAnswersCorrect();

  // Generate feedback based on question type
  if (
    ['multiple_choice', 'unique_choice', 'true_false'].includes(normalizedType)
  ) {
    let correctAnswerText;

    if (normalizedType === 'multiple_choice') {
      // Show all correct answers for multiple choice
      const correctAnswers = correctChoices
        ? correctChoices
            .filter((choice) => choice.isCorrect)
            .map((choice) => getChoiceText(choice))
        : [];
      correctAnswerText = correctAnswers.join(', ');
    } else {
      // Show single correct answer for unique choice and true/false
      const correctChoice = correctChoices
        ? correctChoices.find((choice) => choice.isCorrect)
        : null;
      correctAnswerText = correctChoice
        ? getChoiceText(correctChoice)
        : 'Unknown';
    }

    return (
      <div className='answer-feedback'>
        {isCorrect ? (
          <div className='d-flex align-items-center fst-italic'>
            <FaCheckSquare className='text-success me-2' />
            Correct!
          </div>
        ) : (
          <div>
            <div className='d-flex align-items-center fst-italic mb-2'>
              <TiDelete size={24} className='text-danger me-2' />
              Incorrect!
            </div>
            <div>
              The correct answer is: <strong>{correctAnswerText}</strong>
            </div>
          </div>
        )}
      </div>
    );
  }

  if (normalizedType === 'short_answer') {
    return (
      <div className='question-answer-box'>
        <div className='d-flex align-items-center fst-italic mb-2'>
          <span className='text-info me-2'>📝</span>
          Your answer submitted for review
        </div>
        <div>
          <strong>Expected answer:</strong> {answerText}
        </div>
      </div>
    );
  }

  return null;
}
