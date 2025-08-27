import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { auth } from '../../utils/firebase.config';

function HistoryTable() {
  const [quizResults, setQuizResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [retryingQuizId, setRetryingQuizId] = useState(null);
  const navigate = useNavigate();

  // Helper functions for API calls
  const getAuthenticatedUser = async () => {
    const user = auth.currentUser;
    if (!user) throw new Error('User not authenticated. Please log in again.');
    return user;
  };

  const getApiHeaders = async () => {
    const user = await getAuthenticatedUser();
    const idToken = await user.getIdToken();
    return { Authorization: `Bearer ${idToken}` };
  };

  const getBaseUrl = () => {
    return import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  };

  useEffect(() => {
    const fetchQuizResults = async () => {
      setLoading(true);
      setError(null);

      try {
        const headers = await getApiHeaders();
        const baseUrl = getBaseUrl();

        // Get current user ID
        const userResponse = await axios.get(`${baseUrl}/api/v1/users/me`, {
          headers,
        });
        const userId = userResponse.data.id;

        // Fetch quiz results for the user
        const resultsResponse = await axios.get(
          `${baseUrl}/api/v1/attempts/user/${userId}/results`,
          { headers }
        );

        const results = resultsResponse.data;

        // For each result, we need to fetch additional details (quiz info, category hierarchy)
        const enrichedResults = await Promise.all(
          results.map(async (result) => {
            try {
              // Fetch quiz details to get category information and question count
              const quizResponse = await axios.get(
                `${baseUrl}/api/v1/quizzes/${result.quizId}/detailed`,
                { headers }
              );
              const quiz = quizResponse.data;

              // Get the actual number of questions in the quiz
              const actualQuestionCount = quiz.questionIds
                ? quiz.questionIds.length
                : result.totalQuestions;

              // Fetch category details to get name and hierarchy
              const categoryResponse = await axios.get(
                `${baseUrl}/api/v1/categories/${quiz.categoryId}`,
                { headers }
              );
              const category = categoryResponse.data;

              // Fetch parent category if this is a child category
              let parentCategory = null;
              if (category.parentId) {
                const parentResponse = await axios.get(
                  `${baseUrl}/api/v1/categories/${category.parentId}`,
                  { headers }
                );
                parentCategory = parentResponse.data;
              }

              return {
                ...result,
                quiz,
                category,
                parentCategory,
                area: parentCategory ? parentCategory.name : category.name,
                topic: parentCategory ? category.name : 'General',
                actualQuestionCount, // Use the actual number of questions in the quiz
              };
            } catch (error) {
              console.error(
                `Error enriching result for quiz ${result.quizId}:`,
                error
              );
              return {
                ...result,
                area: 'Unknown',
                topic: 'Unknown',
                quiz: null,
                category: null,
                parentCategory: null,
                actualQuestionCount: result.totalQuestions, // Fallback to original count
              };
            }
          })
        );

        // Sort by completion date (most recent first)
        enrichedResults.sort(
          (a, b) => new Date(b.completedAt) - new Date(a.completedAt)
        );

        setQuizResults(enrichedResults);
      } catch (error) {
        console.error('Error fetching quiz results:', error);
        setError('Failed to load quiz history');
      } finally {
        setLoading(false);
      }
    };

    fetchQuizResults();
  }, []);

  const handleRetry = async (quizId) => {
    if (!quizId) {
      console.error('No quizId provided for retry');
      return;
    }

    setRetryingQuizId(quizId);

    try {
      const headers = await getApiHeaders();
      const baseUrl = getBaseUrl();

      // Create a new attempt for the quiz
      const retryResponse = await axios.post(
        `${baseUrl}/api/v1/attempts/retry`,
        { quizId },
        { headers }
      );

      const newAttempt = retryResponse.data;

      // Fetch the quiz details to get question IDs
      const quizResponse = await axios.get(
        `${baseUrl}/api/v1/quizzes/${quizId}/detailed`,
        { headers }
      );
      const quiz = quizResponse.data;

      // Check if quiz has question IDs
      if (!quiz.questionIds || quiz.questionIds.length === 0) {
        throw new Error('No questions found for this quiz');
      }

      // Fetch questions for the quiz
      const questionPromises = quiz.questionIds.map(async (questionId) => {
        const res = await fetch(`${baseUrl}/api/v1/questions/${questionId}`, {
          headers,
        });
        if (!res.ok) {
          throw new Error(
            `Failed to fetch question ${questionId}: ${res.status}`
          );
        }
        return res.json();
      });

      const questions = await Promise.all(questionPromises);

      // Navigate to quiz page with the new attempt and questions
      navigate('/quiz', {
        state: {
          quiz: {
            id: quizId,
            questions: questions,
            attemptId: newAttempt.id,
          },
          config: {
            selectedParentTopic: '',
            selectedChildTopic: '',
            selectedQuestions: questions.length,
            isRetry: true,
          },
        },
        replace: true,
      });
    } catch (error) {
      console.error('Error during retry:', error);
      setError(`Failed to retry quiz: ${error.message}`);
    } finally {
      setRetryingQuizId(null);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatSuccessRate = (rate) => {
    return Math.round(rate * 100);
  };

  if (loading) {
    return (
      <div
        className='d-flex justify-content-center align-items-center'
        style={{ minHeight: '200px' }}
      >
        <div className='spinner-border' role='status'>
          <span className='visually-hidden'>Loading...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className='alert alert-danger' role='alert'>
        {error}
      </div>
    );
  }

  if (quizResults.length === 0) {
    return (
      <div className='alert alert-info' role='alert'>
        No quiz history found. Take your first quiz to see your progress here!
      </div>
    );
  }

  return (
    <div className='mt-5'>
      <div className='table-responsive'>
        <table className='table table-striped table-bordered table-hover'>
          <thead className='table-light'>
            <tr>
              <th scope='col'>Date</th>
              <th scope='col'>Area</th>
              <th scope='col'>Topic</th>
              <th scope='col'>Questions</th>
              <th scope='col'>Success Rate</th>
              <th scope='col'>Retry</th>
            </tr>
          </thead>
          <tbody>
            {quizResults.map((result) => (
              <tr key={result.attemptId}>
                <td>{formatDate(result.completedAt)}</td>
                <td>
                  <span className='badge bg-primary'>{result.area}</span>
                </td>
                <td>{result.topic}</td>
                <td className='text-center'>{result.actualQuestionCount}</td>
                <td>
                  <span
                    className={`badge ${
                      result.successRate >= 0.8
                        ? 'bg-success'
                        : result.successRate >= 0.6
                        ? 'bg-warning'
                        : 'bg-danger'
                    }`}
                  >
                    {formatSuccessRate(result.successRate)}%
                  </span>
                </td>
                <td>
                  <button
                    className='btn btn-outline-primary btn-sm'
                    onClick={() => handleRetry(result.quizId)}
                    disabled={retryingQuizId === result.quizId}
                  >
                    {retryingQuizId === result.quizId ? (
                      <>
                        <span
                          className='spinner-border spinner-border-sm me-2'
                          role='status'
                          aria-hidden='true'
                        ></span>
                        Retrying...
                      </>
                    ) : (
                      'Retry'
                    )}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default HistoryTable;
