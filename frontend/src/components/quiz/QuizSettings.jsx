function QuizSettings() {
  return (
    <div className='d-flex justify-content-between align-items-end gap-3'>
      {/* SELECTED TOPIC */}
      <div className='selector'>
        <p className='selector-p small-text text-muted'>Topic</p>
        <div className='selected-box button-text'>REACT</div>
      </div>

      {/* SELECT QUESTIONS */}
      <div className='selector'>
        <p className='selector-p small-text text-muted'>Questions</p>
        <div className='selected-box button-text'>10</div>
      </div>

      {/* SHUFFLE BUTTON */}
      <button
        className='btn btn-primary button-text'
        style={{
          width: '100%',
          minWidth: '120px',
          maxWidth: '400px',
          height: '48px',
        }}
      >
        Re-shuffle
      </button>
    </div>
  );
}

export default QuizSettings;
