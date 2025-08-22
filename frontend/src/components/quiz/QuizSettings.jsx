function QuizSettings({ config, onAbort }) {
  // Extract display values from config
  const getTopicDisplayName = () => {
    if (!config) return 'Unknown Topic';

    // If a child topic is selected, use it
    if (
      config.selectedChildTopic &&
      config.selectedChildTopic !== 'All' &&
      config.childrenCategories
    ) {
      const childCategory = config.childrenCategories.find(
        (cat) => cat.id.toString() === config.selectedChildTopic
      );
      return childCategory?.name || 'Unknown Topic';
    }

    // Otherwise use parent topic
    if (config.selectedParentTopic && config.parentCategories) {
      const parentCategory = config.parentCategories.find(
        (cat) => cat.id.toString() === config.selectedParentTopic
      );
      return parentCategory?.name || 'Unknown Topic';
    }

    return 'Unknown Topic';
  };

  const getQuestionsCount = () => {
    return config?.selectedQuestions || '?';
  };

  const handleAbort = () => {
    if (
      window.confirm(
        'Are you sure you want to abort this quiz? Your progress will be lost.'
      )
    ) {
      onAbort?.();
    }
  };

  return (
    <div className='d-flex justify-content-between align-items-end gap-3'>
      {/* SELECTED TOPIC */}
      <div className='selector'>
        <p className='selector-p small-text text-muted'>Topic</p>
        <div className='selected-box button-text fs-6'>
          {getTopicDisplayName()}
        </div>
      </div>

      {/* SELECT QUESTIONS */}
      <div className='selector'>
        <p className='selector-p small-text text-muted'>Questions</p>
        <div className='selected-box button-text'>{getQuestionsCount()}</div>
      </div>

      {/* ABORT BUTTON */}
      <button
        className='btn btn-danger button-text'
        onClick={handleAbort}
        style={{
          width: '100%',
          minWidth: '120px',
          maxWidth: '400px',
          height: '48px',
        }}
      >
        Abort Quiz
      </button>
    </div>
  );
}

export default QuizSettings;
