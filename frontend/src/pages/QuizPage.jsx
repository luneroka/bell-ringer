import { useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import QuizQuestion from '../components/quiz/QuizQuestion';
import QuizSettings from '../components/quiz/QuizSettings';
import { IoIosArrowForward } from 'react-icons/io';
import { FaFlagCheckered } from 'react-icons/fa';

function QuizPage() {
  const location = useLocation();
  const navigate = useNavigate();

  // location.state may contain either:
  // - an array of questions (quizQuestions)
  // - an object { id: <quizId>, questions: [...] }
  const statePayload =
    location.state?.quizQuestions ?? location.state?.quiz ?? null;
  const configPayload = location.state?.config ?? null;

  // fallback to sessionStorage if user refreshed
  const storedPayload = JSON.parse(
    sessionStorage.getItem('quizPayload') || 'null'
  );
  const storedConfig = JSON.parse(
    sessionStorage.getItem('quizConfig') || 'null'
  );

  const initialPayload = statePayload || storedPayload;
  const initialConfig = configPayload || storedConfig;

  // normalize to { id, questions, attemptId }
  const normalize = (p) => {
    if (!p) return null;
    if (Array.isArray(p)) return { id: null, questions: p, attemptId: null };
    // assume object with id, questions, and attemptId
    return {
      id: p.id ?? null,
      questions: p.questions ?? null,
      attemptId: p.attemptId ?? null,
    };
  };

  const normalized = normalize(initialPayload);
  const [quizId, setQuizId] = useState(normalized?.id || null);
  const [attemptId, setAttemptId] = useState(normalized?.attemptId || null);
  const [questions, setQuestions] = useState(normalized?.questions || null);

  // Debug logging
  console.log('QuizPage initialized with:', {
    quizId,
    attemptId,
    questionsCount: questions?.length,
  });

  const [currentIndex, setCurrentIndex] = useState(0);
  const [loadingQuestion, setLoadingQuestion] = useState(false);
  const [userAnswers, setUserAnswers] = useState({}); // Store answers by question index
  const [submittedQuestions, setSubmittedQuestions] = useState(new Set()); // Track submitted questions
  const [error, setError] = useState(null);
  const [submittingAnswer, setSubmittingAnswer] = useState(false);

  // persist payload for refresh/resume
  useEffect(() => {
    if (questions) {
      const payload = { id: quizId, questions, attemptId };
      sessionStorage.setItem('quizPayload', JSON.stringify(payload));
    }
  }, [quizId, questions, attemptId]);

  // fetch choices/open-answer for current question when it becomes active
  useEffect(() => {
    async function fetchExtras() {
      if (!questions || !questions[currentIndex]) return;
      const q = questions[currentIndex];

      console.log('fetchExtras called for question:', q.id, 'type:', q.type);
      console.log('Current choices:', q.choices);
      console.log('Current answerText:', q.answerText);

      // Check if we need to fetch choices/answerText
      const needsChoices =
        (q.type === 'multiple_choice' ||
          q.type === 'MULTIPLE_CHOICE' ||
          q.type === 'unique_choice' ||
          q.type === 'UNIQUE_CHOICE' ||
          q.type === 'true_false' ||
          q.type === 'TRUE_FALSE') &&
        (!q.choices || q.choices.length === 0);
      const needsAnswerText =
        (q.type === 'short_answer' || q.type === 'SHORT_ANSWER') &&
        !q.answerText;

      if (!needsChoices && !needsAnswerText) {
        console.log('No need to fetch - already has data');
        return;
      }

      setLoadingQuestion(true);
      setError(null);
      try {
        const user = (await import('../utils/firebase.config')).auth
          .currentUser;
        if (!user) {
          setError('User not authenticated. Please log in again.');
          return;
        }
        const idToken = await user.getIdToken();
        const baseUrl =
          import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

        if (
          q.type === 'multiple_choice' ||
          q.type === 'MULTIPLE_CHOICE' ||
          q.type === 'unique_choice' ||
          q.type === 'UNIQUE_CHOICE'
        ) {
          console.log('Fetching choices for question:', q.id, 'type:', q.type);
          const resp = await fetch(
            `${baseUrl}/api/v1/choices/question/${q.id}`,
            {
              headers: { Authorization: `Bearer ${idToken}` },
            }
          );

          console.log('Response status:', resp.status);

          if (!resp.ok) {
            const errorText = await resp.text();
            console.error('API Error:', errorText);
            throw new Error(
              `Failed to fetch choices: ${resp.status} ${resp.statusText}`
            );
          }

          const choices = await resp.json();
          console.log('Fetched choices raw response:', choices);

          // Handle different response formats - could be array or object with choices property
          const choicesArray = Array.isArray(choices)
            ? choices
            : choices.choices || choices;
          console.log('Processed choices array:', choicesArray);

          // merge choices into questions state
          setQuestions((prev) => {
            const copy = [...prev];
            copy[currentIndex] = {
              ...copy[currentIndex],
              choices: choicesArray,
            };
            console.log('Updated question with choices:', copy[currentIndex]);
            return copy;
          });
        } else if (q.type === 'true_false' || q.type === 'TRUE_FALSE') {
          console.log('Fetching choices for true/false question:', q.id);
          const resp = await fetch(
            `${baseUrl}/api/v1/choices/question/${q.id}`,
            {
              headers: { Authorization: `Bearer ${idToken}` },
            }
          );

          if (!resp.ok) {
            const errorText = await resp.text();
            console.error('API Error for true/false:', errorText);
            throw new Error(
              `Failed to fetch choices: ${resp.status} ${resp.statusText}`
            );
          }

          const choices = await resp.json();
          console.log('Fetched true/false choices:', choices);

          // Handle different response formats
          const choicesArray = Array.isArray(choices)
            ? choices
            : choices.choices || choices;

          // merge choices into questions state
          setQuestions((prev) => {
            const copy = [...prev];
            copy[currentIndex] = {
              ...copy[currentIndex],
              choices: choicesArray,
            };
            return copy;
          });
        } else if (q.type === 'short_answer' || q.type === 'SHORT_ANSWER') {
          console.log('Fetching open answer for question:', q.id);
          const resp = await fetch(
            `${baseUrl}/api/v1/open-answers/question/${q.id}`,
            {
              headers: { Authorization: `Bearer ${idToken}` },
            }
          );

          console.log('Open answer response status:', resp.status);
          if (!resp.ok) {
            const errorText = await resp.text();
            console.error('Failed to fetch open answer:', errorText);
            throw new Error(
              `Failed to fetch open answer: ${resp.status} ${errorText}`
            );
          }

          const answerData = await resp.json();
          console.log('Open answer data received:', answerData);

          // Handle different response formats - could be single object or array
          let answerText = null;
          if (Array.isArray(answerData) && answerData.length > 0) {
            answerText = answerData[0].answerText || answerData[0].answer;
          } else if (answerData.answerText) {
            answerText = answerData.answerText;
          } else if (answerData.answer) {
            answerText = answerData.answer;
          }

          console.log('Extracted answer text:', answerText);

          setQuestions((prev) => {
            const copy = [...prev];
            copy[currentIndex] = {
              ...copy[currentIndex],
              answerText: answerText,
            };
            console.log(
              'Updated question with answerText:',
              copy[currentIndex]
            );
            return copy;
          });
        }
      } catch (e) {
        console.error('Failed to fetch question extras', e);
        setError('Failed to load question details. Please try refreshing.');
      } finally {
        setLoadingQuestion(false);
      }
    }

    fetchExtras();
  }, [currentIndex]); // Only depend on currentIndex to avoid infinite loop when questions state changes

  useEffect(() => {
    if (!questions) {
      // nothing to render, go back to home/selector
      navigate('/', { replace: true });
    }
  }, [questions, navigate]);

  if (!questions || questions.length === 0) return null;

  const current = questions[currentIndex];

  const handleQuestionSubmit = async (answers) => {
    if (!attemptId || !questions || !questions[currentIndex]) {
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
      const user = (await import('../utils/firebase.config')).auth.currentUser;
      if (!user) {
        setError('User not authenticated. Please log in again.');
        return;
      }
      const idToken = await user.getIdToken();
      const baseUrl =
        import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

      const questionType = currentQuestion.type?.toLowerCase();

      if (
        ['multiple_choice', 'unique_choice', 'true_false'].includes(
          questionType
        )
      ) {
        // Submit choice answers
        if (!currentQuestion.choices || answers.length === 0) {
          throw new Error('No choices available or no answers selected');
        }

        // Find choice IDs for selected answers
        const selectedChoices = [];
        for (const answer of answers) {
          const choice = currentQuestion.choices.find((c) => {
            const choiceText =
              typeof c === 'string' ? c : c.choiceText || c.text || String(c);
            return choiceText === answer;
          });
          if (choice && choice.id) {
            selectedChoices.push({
              questionId: currentQuestion.id,
              choiceId: choice.id,
            });
          }
        }

        if (selectedChoices.length === 0) {
          throw new Error('Could not find choice IDs for selected answers');
        }

        console.log('Submitting choice answers:', selectedChoices);

        const response = await fetch(
          `${baseUrl}/api/v1/attempt-choices/batch`,
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${idToken}`,
            },
            body: JSON.stringify({
              attemptId: attemptId,
              selectedChoices: selectedChoices,
            }),
          }
        );

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(
            `Failed to submit choices: ${response.status} ${errorText}`
          );
        }

        console.log('Choice answers submitted successfully');
      } else if (questionType === 'short_answer') {
        // Submit text answer
        if (!answers[0] || answers[0].trim() === '') {
          throw new Error('Text answer is required');
        }

        console.log('Submitting text answer:', answers[0]);

        // Try to submit a new answer first
        let response = await fetch(`${baseUrl}/api/v1/attempt-text-answers`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${idToken}`,
          },
          body: JSON.stringify({
            attemptId: attemptId,
            questionId: currentQuestion.id,
            answerText: answers[0].trim(),
          }),
        });

        // If answer already exists, update it instead
        if (!response.ok && response.status === 500) {
          const errorText = await response.text();
          if (errorText.includes('Text answer already exists')) {
            console.log('Answer already exists, updating instead...');
            response = await fetch(
              `${baseUrl}/api/v1/attempt-text-answers/${attemptId}/${currentQuestion.id}`,
              {
                method: 'PUT',
                headers: {
                  'Content-Type': 'application/json',
                  Authorization: `Bearer ${idToken}`,
                },
                body: JSON.stringify({
                  answerText: answers[0].trim(),
                }),
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
          copy[currentIndex] = {
            ...copy[currentIndex],
            scoringResult: result, // Store the complete scoring result
          };
          return copy;
        });
      }

      // Mark this question as submitted to prevent double submissions
      setSubmittedQuestions((prev) => new Set(prev).add(currentQuestion.id));
    } catch (e) {
      console.error('Failed to submit answer:', e);
      setError(`Failed to submit answer: ${e.message}`);
    } finally {
      setSubmittingAnswer(false);
    }

    // Store user's answers for this question locally for display
    setUserAnswers((prev) => ({
      ...prev,
      [currentIndex]: answers,
    }));
  };

  const isCurrentQuestionAnswered =
    userAnswers.hasOwnProperty(currentIndex) ||
    (questions &&
      questions[currentIndex] &&
      submittedQuestions.has(questions[currentIndex].id));

  function handleNext() {
    console.log(
      'handleNext called - currentIndex:',
      currentIndex,
      'total questions:',
      questions.length
    );
    if (currentIndex < questions.length - 1) {
      setCurrentIndex((i) => i + 1);
    } else {
      // Last question - complete the quiz
      console.log('Finishing quiz - calling completeQuiz()');
      completeQuiz();
    }
  }

  const completeQuiz = async () => {
    console.log('completeQuiz called with attemptId:', attemptId);
    if (!attemptId) {
      console.error('No attemptId available to complete quiz');
      // Still navigate away to avoid user being stuck
      sessionStorage.removeItem('quizPayload');
      navigate('/', { replace: true });
      return;
    }

    try {
      const user = (await import('../utils/firebase.config')).auth.currentUser;
      if (!user) {
        console.error('User not authenticated');
        sessionStorage.removeItem('quizPayload');
        navigate('/', { replace: true });
        return;
      }

      const idToken = await user.getIdToken();
      const baseUrl =
        import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

      console.log('Completing quiz attempt:', attemptId);
      console.log(
        'API URL:',
        `${baseUrl}/api/v1/attempts/${attemptId}/complete`
      );

      const response = await fetch(
        `${baseUrl}/api/v1/attempts/${attemptId}/complete`,
        {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${idToken}`,
          },
        }
      );

      console.log('Complete quiz response status:', response.status);
      if (!response.ok) {
        const errorText = await response.text();
        console.error(
          `Failed to complete quiz: ${response.status} ${errorText}`
        );
        // Don't block navigation even if completion fails
      } else {
        console.log('Quiz completed successfully');
        const result = await response.json();
        console.log('Completion result:', result);
      }
    } catch (e) {
      console.error('Error completing quiz:', e);
      // Don't block navigation even if completion fails
    } finally {
      // Always clean up and navigate
      sessionStorage.removeItem('quizPayload');
      // TODO: Navigate to results page with quiz results
      navigate('/', { replace: true });
    }
  };

  return (
    <>
      {error && (
        <div className='alert alert-danger' role='alert'>
          {error}
        </div>
      )}

      <QuizSettings config={initialConfig} />
      {console.log('Rendering QuizQuestion with:', {
        question: current.question,
        type: current.type,
        answerText: current.answerText,
        scoringResult: current.scoringResult,
      })}
      <QuizQuestion
        index={currentIndex + 1}
        total={questions.length}
        question={current.question}
        type={current.type}
        choices={current.choices}
        correctChoices={current.choices} // Use choices array since it contains isCorrect info
        answerText={current.answerText}
        scoringResult={current.scoringResult} // Pass scoring result for text answers
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
