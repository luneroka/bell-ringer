import {
  renderQuestionContent,
  renderAnswerContent,
} from '../../utils/helpers.jsx';

function QuizQuestion({
  index,
  question,
  type,
  choices,
  correctChoices,
  answerText,
}) {
  return (
    <div id='quiz-question' className='d-flex flex-column gap-4'>
      <div>
        <p className='text-muted small-text'>Question {index}/10</p>
        <div className='question-answer-box'>{question}</div>
      </div>

      <div className='w-100'>{renderQuestionContent(type, choices, index)}</div>

      <button className='mx-auto send-btn button-text'>Send</button>

      <div>{renderAnswerContent(type, correctChoices, answerText)}</div>
    </div>
  );
}

export default QuizQuestion;
