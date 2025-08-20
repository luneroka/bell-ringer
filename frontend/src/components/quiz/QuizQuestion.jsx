import { useState, useEffect } from 'react';
import {
  renderQuestionContent,
  renderAnswerContent,
} from '../../utils/helpers.jsx';

function QuizQuestion({
  index,
  total,
  question,
  type,
  choices,
  correctChoices,
  answerText,
  scoringResult,
  loading = false,
  submitting = false,
  onSubmit,
}) {
  const [userAnswers, setUserAnswers] = useState([]);
  const [submitted, setSubmitted] = useState(false);

  // Reset state when question changes
  useEffect(() => {
    setUserAnswers([]);
    setSubmitted(false);
  }, [index, question]); // Reset when question or index changes

  const handleSubmit = () => {
    setSubmitted(true);
    if (onSubmit) {
      onSubmit(userAnswers);
    }
  };

  return (
    <div id='quiz-question' className='d-flex flex-column gap-4'>
      <div>
        <p className='text-muted small-text'>
          Question {index}/{total}
        </p>
        <div className='question-answer-box'>{question}</div>
      </div>

      {loading ? (
        <div>Loading question details...</div>
      ) : (
        <div className='w-100'>
          {renderQuestionContent(
            type,
            choices,
            index,
            userAnswers,
            setUserAnswers,
            submitted
          )}
        </div>
      )}

      <button
        className='mx-auto send-btn button-text'
        onClick={handleSubmit}
        disabled={
          submitted || loading || submitting || userAnswers.length === 0
        }
      >
        {submitting ? 'Submitting...' : submitted ? 'Submitted' : 'Send'}
      </button>

      {submitted && (
        <div>
          {renderAnswerContent(
            type,
            correctChoices,
            answerText,
            userAnswers,
            scoringResult
          )}
        </div>
      )}
    </div>
  );
}

export default QuizQuestion;
