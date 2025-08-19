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
