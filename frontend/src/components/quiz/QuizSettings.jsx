function QuizSettings() {
  return (
    <div className='d-flex justify-content-between align-items-end'>
      {/* SELECTED TOPIC */}
      <div>
        <p className='selector-p small-text text-muted'>Topic</p>
        <div className='selected-box button-text'>REACT</div>
      </div>

      {/* SELECT QUESTIONS */}
      <div>
        <p className='selector-p small-text text-muted'>Questions</p>
        <div className='selected-box button-text'>10</div>
      </div>

      {/* SHUFFLE BUTTON */}
      <button
        className='btn btn-primary button-text'
        style={{ width: '300px', height: '48px' }}
      >
        Re-shuffle
      </button>
    </div>
  );
}

export default QuizSettings;
