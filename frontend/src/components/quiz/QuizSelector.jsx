function QuizSelector() {
  return (
    <div className='d-flex justify-content-between align-items-end'>
      {/* TOPIC SELECT */}
      <div>
        <p className='selector-p small-text text-muted'>Pick a topic</p>
        <select name='topic' id='topic-select' className='form-select'>
          <option value='' selected disabled>
            SELECT
          </option>
          <option value='react'>REACT</option>
          <option value='html'>HTML</option>
          <option value='css'>CSS</option>
        </select>
      </div>

      {/* QUESTIONS SELECT */}
      <div>
        <p className='selector-p small-text text-muted'>
          Select number of questions
        </p>
        <select name='topic' id='questions-select' className='form-select'>
          <option value='' selected disabled>
            SELECT
          </option>
          <option value='react'>5</option>
          <option value='html'>10</option>
          <option value='css'>15</option>
          <option value='css'>20</option>
        </select>
      </div>

      {/* SHUFFLE BUTTON */}
      <button
        className='btn btn-primary button-text'
        style={{ width: '300px', height: '48px' }}
      >
        Shuffle
      </button>
    </div>
  );
}

export default QuizSelector;
