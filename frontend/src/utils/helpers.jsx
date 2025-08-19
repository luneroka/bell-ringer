import { FaCheckSquare } from 'react-icons/fa';
import { TiDelete } from 'react-icons/ti';

export function renderQuestionContent(type, choices, index) {
  switch (type) {
    case 'multiple_choice':
    case 'unique_choice':
      return (
        <div className='choices-container'>
          {choices.map((choice, idx) => (
            <div key={idx} className='choice-option'>
              <input
                type={type === 'multiple_choice' ? 'checkbox' : 'radio'}
                id={`choice-${index}-${idx}`}
                name={
                  type === 'multiple_choice'
                    ? `question-${index}-choice-${idx}`
                    : `question-${index}`
                }
                value={choice}
              />
              <label
                htmlFor={`choice-${index}-${idx}`}
                className='choice-label'
              >
                {choice}
              </label>
            </div>
          ))}
        </div>
      );

    case 'true_false':
      return (
        <div className='choices-container'>
          {['True', 'False'].map((choice, idx) => (
            <div key={idx} className='choice-option'>
              <input
                type='radio'
                id={`choice-${index}-${idx}`}
                name={`question-${index}`}
                value={choice.toLowerCase()}
              />
              <label
                htmlFor={`choice-${index}-${idx}`}
                className='choice-label'
              >
                {choice}
              </label>
            </div>
          ))}
        </div>
      );

    case 'short_answer':
      const handleAutoResize = (e) => {
        e.target.style.height = 'auto';
        e.target.style.height = e.target.scrollHeight + 'px';
      };

      return (
        <div className='short-answer-container'>
          <textarea
            className='form-control short-answer-input'
            placeholder='Type your answer here...'
            rows='1'
            onInput={handleAutoResize}
            style={{ overflow: 'hidden' }}
          />
        </div>
      );
  }
}

export function renderAnswerContent(
  type,
  correctChoices,
  answerText,
  userAnswers = []
) {
  userAnswers = ['Run  after the component rendered'];
  // Helper function to check if user's answers are correct
  const checkAnswersCorrect = () => {
    if (!userAnswers || userAnswers.length === 0) return false;

    // For multiple choice: all correct answers must be selected, no wrong ones
    if (type === 'multiple_choice') {
      return (
        correctChoices.length === userAnswers.length &&
        correctChoices.every((answer) => userAnswers.includes(answer))
      );
    }

    // For unique choice and true/false: only one correct answer
    return userAnswers.length === 1 && correctChoices.includes(userAnswers[0]);
  };

  switch (type) {
    case 'multiple_choice':
    case 'unique_choice':
    case 'true_false':
      const isCorrect = checkAnswersCorrect();
      const correctAnswersText = correctChoices.join(', ');

      return (
        <div className='answer-feedback'>
          {isCorrect ? (
            <div className='d-flex align-items-center'>
              <FaCheckSquare className='text-success me-2' />
              Correct!
            </div>
          ) : (
            <div>
              <div className='d-flex align-items-center'>
                <TiDelete size={24} className='text-danger me-2' />
                Incorrect! <br />
              </div>
              The answer is: {correctAnswersText}
            </div>
          )}
        </div>
      );

    case 'short_answer':
      // For short answer, we'll assume this will be checked by backend later
      // For now, showing as "pending" or you can pass isCorrect as a parameter
      const isShortAnswerCorrect = true; // This should come from backend validation

      return (
        <div className='question-answer-box'>
          <div>
            {isShortAnswerCorrect ? (
              <div className='d-flex align-items-center'>
                <FaCheckSquare className='text-success me-2' />
                Correct!
              </div>
            ) : (
              <div className='d-flex align-items-center'>
                <TiDelete size={24} className='text-danger me-2' />
                Incorrect!
              </div>
            )}
          </div>
          <div className='fst-italic'>{answerText}</div>
        </div>
      );

    default:
      return null;
  }
}
