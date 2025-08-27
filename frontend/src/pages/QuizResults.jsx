import { useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';

function QuizResults() {
  const location = useLocation();
  const navigate = useNavigate();
  const [retrying, setRetrying] = useState(false);

  // Get quiz results from navigation state
  const { score, totalQuestions, config, quizId } = location.state || {};

  useEffect(() => {
    // If no results data, redirect to home
    if (!score && score !== 0) {
      navigate('/', {
        state: { refreshUserData: true },
        replace: true,
      });
    }
  }, [score, navigate]);

  const handleRetry = async () => {
    if (!quizId) {
      console.error('No quizId available for retry');
      // Fall back to config-based retry
      navigate('/', {
        state: {
          retryConfig: { ...config, isRetry: true },
        },
        replace: true,
      });
      return;
    }

    setRetrying(true);

    try {
      const user = (await import('../utils/firebase.config')).auth.currentUser;
      if (!user) {
        console.error('User not authenticated');
        navigate('/', {
          state: { refreshUserData: true },
          replace: true,
        });
        return;
      }

      const idToken = await user.getIdToken();
      const baseUrl =
        import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

      // Create a new attempt for the same quiz using the retry endpoint
      const response = await fetch(`${baseUrl}/api/v1/attempts/retry`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${idToken}`,
        },
        body: JSON.stringify({
          quizId: quizId,
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error(
          `Failed to start new attempt: ${response.status} ${errorText}`
        );
        throw new Error('Failed to start new attempt');
      }

      const newAttempt = await response.json();

      // Get the quiz data which contains the question IDs
      const quizResponse = await fetch(
        `${baseUrl}/api/v1/quizzes/${quizId}/detailed`,
        {
          headers: {
            Authorization: `Bearer ${idToken}`,
          },
        }
      );

      if (!quizResponse.ok) {
        const errorText = await quizResponse.text();
        console.error(
          `Failed to fetch quiz data: ${quizResponse.status} ${errorText}`
        );
        throw new Error('Failed to fetch quiz data');
      }

      const quizData = await quizResponse.json();

      // Get the actual questions using the question IDs
      if (!quizData.questionIds || quizData.questionIds.length === 0) {
        throw new Error('No question IDs found in quiz data');
      }

      // Fetch all questions in parallel
      const questionPromises = quizData.questionIds.map((questionId) =>
        fetch(`${baseUrl}/api/v1/questions/${questionId}`, {
          headers: {
            Authorization: `Bearer ${idToken}`,
          },
        }).then((res) => {
          if (!res.ok) {
            throw new Error(
              `Failed to fetch question ${questionId}: ${res.status}`
            );
          }
          return res.json();
        })
      );

      const questions = await Promise.all(questionPromises);

      // Navigate to quiz page with the new attempt and questions
      navigate('/quiz', {
        state: {
          quiz: {
            id: quizId,
            questions: questions,
            attemptId: newAttempt.id,
          },
          config: config,
        },
        replace: true,
      });
    } catch (error) {
      console.error('Error during retry:', error);
      // Fall back to config-based retry if API calls fail
      navigate('/', {
        state: {
          retryConfig: { ...config, isRetry: true },
        },
        replace: true,
      });
    } finally {
      setRetrying(false);
    }
  };

  const handleHome = () => {
    // Navigate to home page with refresh flag to update UserData stats
    navigate('/', {
      state: { refreshUserData: true },
    });
  };

  if (!score && score !== 0) {
    return null; // Will redirect in useEffect
  }

  return (
    <div className='d-flex justify-content-center align-items-center'>
      <div className='text-center' style={{ maxWidth: '600px', width: '100%' }}>
        {/* Celebration Header */}
        <div className='mb-4'>
          <span style={{ fontSize: '48px' }}>🎉</span>
          <h1 className='display-4 fw-bold text-dark mt-3'>Well done!</h1>
        </div>

        {/* Score Message */}
        <p className='fs-4 text-muted mb-4'>
          You have completed this quiz. Here is your score:
        </p>

        {/* Score Display */}
        <div className='mb-5'>
          <span
            className='display-1 fw-bold text-dark'
            style={{ fontSize: '4rem' }}
          >
            {score}/{totalQuestions}
          </span>
        </div>

        {/* Action Buttons */}
        <div className='d-flex gap-3 justify-content-center'>
          <button
            className='btn btn-primary button-text'
            onClick={handleRetry}
            disabled={retrying}
            style={{
              minWidth: '150px',
              height: '48px',
            }}
          >
            {retrying ? 'Starting...' : 'Retry'}
          </button>
          <button
            className='btn btn-primary button-text'
            onClick={handleHome}
            style={{
              minWidth: '150px',
              height: '48px',
            }}
          >
            Home
          </button>
        </div>
      </div>
    </div>
  );
}

export default QuizResults;
