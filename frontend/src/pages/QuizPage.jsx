import { useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import QuizQuestion from '../components/quiz/QuizQuestion';
import QuizSettings from '../components/quiz/QuizSettings';
import { IoIosArrowForward } from 'react-icons/io';
import { FaFlagCheckered } from 'react-icons/fa';

// Constants
const QUESTION_TYPES = {
  MULTIPLE_CHOICE: ['multiple_choice', 'MULTIPLE_CHOICE'],
  UNIQUE_CHOICE: ['unique_choice', 'UNIQUE_CHOICE'],
  TRUE_FALSE: ['true_false', 'TRUE_FALSE'],
  SHORT_ANSWER: ['short_answer', 'SHORT_ANSWER'],
};

const CHOICE_QUESTION_TYPES = [
  ...QUESTION_TYPES.MULTIPLE_CHOICE,
  ...QUESTION_TYPES.UNIQUE_CHOICE,
  ...QUESTION_TYPES.TRUE_FALSE,
];

function QuizPage() {
  const location = useLocation();
  const navigate = useNavigate();

  // Helper functions
  const getStorageData = (key, fallback = null) =>
    JSON.parse(sessionStorage.getItem(key) || 'null') || fallback;

  const setStorageData = (key, data) =>
    sessionStorage.setItem(key, JSON.stringify(data));

  const removeStorageData = (key) => sessionStorage.removeItem(key);

  // Get quiz data from location state or session storage
  const statePayload =
    location.state?.quizQuestions ?? location.state?.quiz ?? null;
  const configPayload = location.state?.config ?? null;

  const storedPayload = getStorageData('quizPayload');
  const storedConfig = getStorageData('quizConfig');

  const initialPayload = statePayload || storedPayload;
  const initialConfig = configPayload || storedConfig;

  // Normalize payload to consistent format
  const normalizePayload = (payload) => {
    if (!payload) return null;
    if (Array.isArray(payload))
      return { id: null, questions: payload, attemptId: null };
    return {
      id: payload.id ?? null,
      questions: payload.questions ?? null,
      attemptId: payload.attemptId ?? null,
    };
  };

  const normalized = normalizePayload(initialPayload);

  // State management
  const [quizId, setQuizId] = useState(normalized?.id || null);
  const [attemptId, setAttemptId] = useState(normalized?.attemptId || null);
  const [questions, setQuestions] = useState(normalized?.questions || null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [loadingQuestion, setLoadingQuestion] = useState(false);
  const [userAnswers, setUserAnswers] = useState({});
  const [submittedQuestions, setSubmittedQuestions] = useState(new Set());
  const [error, setError] = useState(null);
  const [submittingAnswer, setSubmittingAnswer] = useState(false);

  // Debug logging
  console.log('QuizPage initialized with:', {
    quizId,
    attemptId,
    questionsCount: questions?.length,
  });

  // Helper functions for API calls
  const getAuthenticatedUser = async () => {
    const user = (await import('../utils/firebase.config')).auth.currentUser;
    if (!user) throw new Error('User not authenticated. Please log in again.');
    return user;
  };

  const getApiHeaders = async () => {
    const user = await getAuthenticatedUser();
    const idToken = await user.getIdToken();
    return { Authorization: `Bearer ${idToken}` };
  };

  const getBaseUrl = () =>
    import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

  const isChoiceQuestionType = (type) => CHOICE_QUESTION_TYPES.includes(type);

  const isShortAnswerType = (type) =>
    QUESTION_TYPES.SHORT_ANSWER.includes(type);

  const needsChoices = (question) =>
    isChoiceQuestionType(question.type) &&
    (!question.choices || question.choices.length === 0);

  const needsAnswerText = (question) =>
    isShortAnswerType(question.type) && !question.answerText;

  // persist payload for refresh/resume
  useEffect(() => {
    if (questions) {
      const payload = { id: quizId, questions, attemptId };
      setStorageData('quizPayload', payload);
    }
  }, [quizId, questions, attemptId]);

  // API fetch functions
  const fetchChoices = async (questionId) => {
    const headers = await getApiHeaders();
    const baseUrl = getBaseUrl();

    const response = await fetch(
      `${baseUrl}/api/v1/choices/question/${questionId}`,
      { headers }
    );

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(
        `Failed to fetch choices: ${response.status} ${errorText}`
      );
    }

    const choices = await response.json();
    return Array.isArray(choices) ? choices : choices.choices || choices;
  };

  const fetchOpenAnswer = async (questionId) => {
    const headers = await getApiHeaders();
    const baseUrl = getBaseUrl();

    const response = await fetch(
      `${baseUrl}/api/v1/open-answers/question/${questionId}`,
      { headers }
    );

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(
        `Failed to fetch open answer: ${response.status} ${errorText}`
      );
    }

    const answerData = await response.json();

    // Handle different response formats
    if (Array.isArray(answerData) && answerData.length > 0) {
      return answerData[0].answerText || answerData[0].answer;
    }
    return answerData.answerText || answerData.answer;
  };

  // fetch choices/open-answer for current question when it becomes active
  useEffect(() => {
    async function fetchExtras() {
      if (!questions || !questions[currentIndex]) return;

      const question = questions[currentIndex];
      console.log(
        'fetchExtras called for question:',
        question.id,
        'type:',
        question.type
      );

      if (!needsChoices(question) && !needsAnswerText(question)) {
        console.log('No need to fetch - already has data');
        return;
      }

      setLoadingQuestion(true);
      setError(null);

      try {
        if (needsChoices(question)) {
          console.log(
            'Fetching choices for question:',
            question.id,
            'type:',
            question.type
          );
          const choices = await fetchChoices(question.id);

          setQuestions((prev) => {
            const copy = [...prev];
            copy[currentIndex] = { ...copy[currentIndex], choices };
            console.log('Updated question with choices:', copy[currentIndex]);
            return copy;
          });
        }

        if (needsAnswerText(question)) {
          console.log('Fetching open answer for question:', question.id);
          const answerText = await fetchOpenAnswer(question.id);

          setQuestions((prev) => {
            const copy = [...prev];
            copy[currentIndex] = { ...copy[currentIndex], answerText };
            console.log(
              'Updated question with answerText:',
              copy[currentIndex]
            );
            return copy;
          });
        }
      } catch (error) {
        console.error('Failed to fetch question extras', error);
        setError('Failed to load question details. Please try refreshing.');
      } finally {
        setLoadingQuestion(false);
      }
    }

    fetchExtras();
  }, [currentIndex]);

  useEffect(() => {
    if (!questions) {
      // nothing to render, go back to home/selector
      navigate('/', { replace: true });
    }
  }, [questions, navigate]);

  if (!questions || questions.length === 0) return null;

  const current = questions[currentIndex];

  // Submission functions
  const submitChoiceAnswers = async (question, answers) => {
    if (!question.choices || answers.length === 0) {
      throw new Error('No choices available or no answers selected');
    }

    const selectedChoices = answers.map((answer) => {
      const choice = question.choices.find((c) => {
        const choiceText =
          typeof c === 'string' ? c : c.choiceText || c.text || String(c);
        return choiceText === answer;
      });
      if (!choice?.id) {
        throw new Error(`Could not find choice ID for answer: ${answer}`);
      }
      return { questionId: question.id, choiceId: choice.id };
    });

    const headers = await getApiHeaders();
    const baseUrl = getBaseUrl();

    const response = await fetch(`${baseUrl}/api/v1/attempt-choices/batch`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...headers,
      },
      body: JSON.stringify({
        attemptId: attemptId,
        selectedChoices: selectedChoices,
      }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(
        `Failed to submit choices: ${response.status} ${errorText}`
      );
    }

    console.log('Choice answers submitted successfully');
  };

  const submitTextAnswer = async (question, answerText) => {
    if (!answerText?.trim()) {
      throw new Error('Text answer is required');
    }

    const headers = await getApiHeaders();
    const baseUrl = getBaseUrl();

    // Try to submit a new answer first
    let response = await fetch(`${baseUrl}/api/v1/attempt-text-answers`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...headers,
      },
      body: JSON.stringify({
        attemptId: attemptId,
        questionId: question.id,
        answerText: answerText.trim(),
      }),
    });

    // If answer already exists, update it instead
    if (!response.ok && response.status === 500) {
      const errorText = await response.text();
      if (errorText.includes('Text answer already exists')) {
        console.log('Answer already exists, updating instead...');
        response = await fetch(
          `${baseUrl}/api/v1/attempt-text-answers/${attemptId}/${question.id}`,
          {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
              ...headers,
            },
            body: JSON.stringify({ answerText: answerText.trim() }),
          }
        );
      }
    }

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(
        `Failed to submit text answer: ${response.status} ${errorText}`
      );
    }

    const result = await response.json();
    console.log('Text answer submitted successfully:', result);

    // Store the scoring result for display
    setQuestions((prev) => {
      const copy = [...prev];
      copy[currentIndex] = { ...copy[currentIndex], scoringResult: result };
      return copy;
    });
  };

  const handleQuestionSubmit = async (answers) => {
    if (!attemptId || !questions?.[currentIndex]) {
      console.error('Missing attemptId or question data for submission');
      return;
    }

    const currentQuestion = questions[currentIndex];

    // Check if this question has already been submitted
    if (submittedQuestions.has(currentQuestion.id)) {
      console.log('Question already submitted, skipping...');
      return;
    }

    setSubmittingAnswer(true);
    setError(null);

    try {
      const questionType = currentQuestion.type?.toLowerCase();

      if (
        ['multiple_choice', 'unique_choice', 'true_false'].includes(
          questionType
        )
      ) {
        await submitChoiceAnswers(currentQuestion, answers);
      } else if (questionType === 'short_answer') {
        await submitTextAnswer(currentQuestion, answers[0]);
      }

      // Mark this question as submitted to prevent double submissions
      setSubmittedQuestions((prev) => new Set(prev).add(currentQuestion.id));

      // Store user's answers for this question locally for display
      setUserAnswers((prev) => ({ ...prev, [currentIndex]: answers }));
    } catch (error) {
      console.error('Failed to submit answer:', error);
      setError(`Failed to submit answer: ${error.message}`);
    } finally {
      setSubmittingAnswer(false);
    }
  };

  // Navigation and completion functions
  const cleanupSession = () => {
    removeStorageData('quizPayload');
    removeStorageData('quizConfig');
  };

  const navigateToResults = (score, totalQuestions) => {
    cleanupSession();
    navigate('/quiz-results', {
      state: {
        score,
        totalQuestions,
        config: initialConfig,
        quizId: quizId,
      },
      replace: true,
    });
  };

  const isCurrentQuestionAnswered =
    userAnswers.hasOwnProperty(currentIndex) ||
    (questions?.[currentIndex] &&
      submittedQuestions.has(questions[currentIndex].id));

  const handleNext = () => {
    console.log(
      'handleNext called - currentIndex:',
      currentIndex,
      'total questions:',
      questions.length
    );
    if (currentIndex < questions.length - 1) {
      setCurrentIndex((i) => i + 1);
    } else {
      console.log('Finishing quiz - calling completeQuiz()');
      completeQuiz();
    }
  };

  const handleAbortQuiz = () => {
    cleanupSession();
    navigate('/', { replace: true });
  };

  // Score calculation functions
  const calculateScoreFromAttempt = (attemptData) => {
    let score = 0;

    console.log('Calculating score from attempt data:', attemptData);

    // Calculate score from choice questions
    if (attemptData.selectedChoices && questions) {
      const choiceScore = questions.reduce((count, question) => {
        if (!question.choices) return count;

        const userChoices = attemptData.selectedChoices.filter(
          (choice) => choice.questionId === question.id
        );

        if (userChoices.length === 0) return count;

        const questionType = question.type?.toLowerCase();

        if (questionType === 'multiple_choice') {
          const correctChoiceIds = question.choices
            .filter((choice) => choice.isCorrect)
            .map((choice) => choice.id);
          const selectedChoiceIds = userChoices.map(
            (choice) => choice.choiceId
          );

          const hasAllCorrect = correctChoiceIds.every((id) =>
            selectedChoiceIds.includes(id)
          );
          const hasNoIncorrect = selectedChoiceIds.every((id) =>
            correctChoiceIds.includes(id)
          );

          const isCorrect = hasAllCorrect && hasNoIncorrect;
          console.log(
            `Question ${question.id} (multiple_choice): correct=${isCorrect}`
          );
          return isCorrect ? count + 1 : count;
        } else {
          const selectedChoice = question.choices.find(
            (choice) => choice.id === userChoices[0].choiceId
          );
          const isCorrect = selectedChoice?.isCorrect || false;
          console.log(
            `Question ${question.id} (${questionType}): correct=${isCorrect}`
          );
          return isCorrect ? count + 1 : count;
        }
      }, 0);

      console.log('Choice questions score:', choiceScore);
      score += choiceScore;
    }

    // Calculate score from text answers
    if (attemptData.textAnswers) {
      const textScore = attemptData.textAnswers.reduce((count, answer) => {
        const isCorrect = answer.isCorrect || false;
        console.log(
          `Text answer for question ${answer.questionId}: correct=${isCorrect}`
        );
        return isCorrect ? count + 1 : count;
      }, 0);

      console.log('Text questions score:', textScore);
      score += textScore;
    }

    console.log('Final calculated score:', score);
    return score;
  };

  const calculateLocalScore = () => {
    return (
      questions?.reduce((count, question) => {
        if (question.scoringResult) {
          return question.scoringResult.isCorrect ? count + 1 : count;
        }
        return count;
      }, 0) || 0
    );
  };

  const completeQuiz = async () => {
    console.log('completeQuiz called with attemptId:', attemptId);
    if (!attemptId) {
      console.error('No attemptId available to complete quiz');
      navigateToResults(0, questions?.length || 0);
      return;
    }

    try {
      const headers = await getApiHeaders();
      const baseUrl = getBaseUrl();

      console.log('Completing quiz attempt:', attemptId);

      // Step 1: Complete the attempt
      const completeResponse = await fetch(
        `${baseUrl}/api/v1/attempts/${attemptId}/complete`,
        {
          method: 'POST',
          headers,
        }
      );

      if (!completeResponse.ok) {
        const errorText = await completeResponse.text();
        console.error(
          `Failed to complete quiz: ${completeResponse.status} ${errorText}`
        );
        navigateToResults(0, questions?.length || 0);
        return;
      }

      console.log('Quiz completed successfully');

      // Step 2: Get the detailed attempt data with answers to calculate score
      const attemptResponse = await fetch(
        `${baseUrl}/api/v1/attempts/${attemptId}/detailed`,
        { headers }
      );

      if (!attemptResponse.ok) {
        console.error('Failed to fetch attempt details for scoring');
        const fallbackScore = calculateLocalScore();
        navigateToResults(fallbackScore, questions?.length || 0);
        return;
      }

      const attemptData = await attemptResponse.json();
      console.log('Attempt data for scoring:', attemptData);

      // Step 3: Calculate the actual score from the attempt data
      const calculatedScore = calculateScoreFromAttempt(attemptData);
      navigateToResults(calculatedScore, questions?.length || 0);
    } catch (error) {
      console.error('Error completing quiz:', error);
      const fallbackScore = calculateLocalScore();
      navigateToResults(fallbackScore, questions?.length || 0);
    }
  };

  // Redirect to home if no questions
  useEffect(() => {
    if (!questions) {
      navigate('/', { replace: true });
    }
  }, [questions, navigate]);

  // Early return if no questions
  if (!questions || questions.length === 0) return null;

  const currentQuestion = questions[currentIndex];

  return (
    <>
      {error && (
        <div className='alert alert-danger' role='alert'>
          {error}
        </div>
      )}

      <QuizSettings config={initialConfig} onAbort={handleAbortQuiz} />
      {console.log('Rendering QuizQuestion with:', {
        question: currentQuestion.question,
        type: currentQuestion.type,
        answerText: currentQuestion.answerText,
        scoringResult: currentQuestion.scoringResult,
      })}
      <QuizQuestion
        index={currentIndex + 1}
        total={questions.length}
        question={currentQuestion.question}
        type={currentQuestion.type}
        choices={currentQuestion.choices}
        correctChoices={currentQuestion.choices} // Use choices array since it contains isCorrect info
        answerText={currentQuestion.answerText}
        scoringResult={currentQuestion.scoringResult} // Pass scoring result for text answers
        loading={loadingQuestion}
        submitting={submittingAnswer}
        onSubmit={handleQuestionSubmit}
      />

      <div style={{ marginTop: '16px' }}>
        <button
          className='next-btn btn btn-primary'
          onClick={handleNext}
          disabled={!isCurrentQuestionAnswered}
        >
          {currentIndex < questions.length - 1 ? (
            <>
              <IoIosArrowForward />
            </>
          ) : (
            <FaFlagCheckered />
          )}
        </button>
      </div>
    </>
  );
}

export default QuizPage;
