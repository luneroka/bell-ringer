import { renderQuestionContent } from '../../utils/helpers.jsx';

function QuizQuestion({ index, question, type, choices, answerText }) {
  return (
    <div id='quiz-question' className='d-flex flex-column gap-4'>
      <div>
        <p className='text-muted small-text'>Question {index}/10</p>
        <div className='d-flex flex-column gap-4 bg-light p-4 rounded-3'>
          {question}
        </div>
      </div>

      <div className='w-100'>{renderQuestionContent(type, choices, index)}</div>

      <button className='mx-auto send-btn button-text'>Send</button>

      <div className='d-flex flex-column'>
        <p>Result</p>
        <div>{answerText}</div>
      </div>
    </div>
  );
}

export default QuizQuestion;
