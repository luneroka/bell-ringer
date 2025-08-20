import { useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import QuizQuestion from '../components/quiz/QuizQuestion';
import QuizSettings from '../components/quiz/QuizSettings';
import { IoIosArrowForward } from 'react-icons/io';

function QuizPage() {
  const location = useLocation();
  const navigate = useNavigate();

  // location.state may contain either:
  // - an array of questions (quizQuestions)
  // - an object { id: <quizId>, questions: [...] }
  const statePayload =
    location.state?.quizQuestions ?? location.state?.quiz ?? null;

  // fallback to sessionStorage if user refreshed
  const storedPayload = JSON.parse(
    sessionStorage.getItem('quizPayload') || 'null'
  );

  const initialPayload = statePayload || storedPayload;

  // normalize to { id, questions }
  const normalize = (p) => {
    if (!p) return null;
    if (Array.isArray(p)) return { id: null, questions: p };
    // assume object with id and questions
    return { id: p.id ?? null, questions: p.questions ?? null };
  };

  const normalized = normalize(initialPayload);
  const [quizId, setQuizId] = useState(normalized?.id || null);
  const [questions, setQuestions] = useState(normalized?.questions || null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [loadingQuestion, setLoadingQuestion] = useState(false);
  const [userAnswers, setUserAnswers] = useState({}); // Store answers by question index
  const [error, setError] = useState(null);

  // persist payload for refresh/resume
  useEffect(() => {
    if (questions) {
      const payload = { id: quizId, questions };
      sessionStorage.setItem('quizPayload', JSON.stringify(payload));
    }
  }, [quizId, questions]);

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
          const resp = await fetch(
            `${baseUrl}/api/v1/open-answers/question/${q.id}`,
            {
              headers: { Authorization: `Bearer ${idToken}` },
            }
          );
          const answerData = await resp.json();
          // expected shape: { answerText: '...' }
          setQuestions((prev) => {
            const copy = [...prev];
            copy[currentIndex] = {
              ...copy[currentIndex],
              answerText: answerData.answerText,
            };
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

  const handleQuestionSubmit = (answers) => {
    // Store user's answers for this question
    setUserAnswers((prev) => ({
      ...prev,
      [currentIndex]: answers,
    }));
  };

  const isCurrentQuestionAnswered = userAnswers.hasOwnProperty(currentIndex);

  function handleNext() {
    if (currentIndex < questions.length - 1) {
      setCurrentIndex((i) => i + 1);
    } else {
      // finished the quiz: clear stored payload and go to results or home
      sessionStorage.removeItem('quizPayload');
      // Show final results or navigate to results page
      // navigate to home for now, could be /quiz/results with quizId
      navigate('/', { replace: true });
    }
  }

  return (
    <>
      {error && (
        <div className='alert alert-danger' role='alert'>
          {error}
        </div>
      )}

      <QuizSettings />
      <QuizQuestion
        index={currentIndex + 1}
        total={questions.length}
        question={current.question}
        type={current.type}
        choices={current.choices}
        correctChoices={current.choices} // Use choices array since it contains isCorrect info
        answerText={current.answerText}
        loading={loadingQuestion}
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
            'Finish Quiz'
          )}
        </button>
      </div>
    </>
  );
}

export default QuizPage;
