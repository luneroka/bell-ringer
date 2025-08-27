import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { auth } from '../../utils/firebase.config';

function QuizSelector({ retryConfig }) {
  const [parentCategories, setParentCategories] = useState([]);
  const [childrenCategories, setChildrenCategories] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [categoriesLoading, setCategoriesLoading] = useState(false);
  const [selectedParentTopic, setSelectedParentTopic] = useState('');
  const [selectedChildTopic, setSelectedChildTopic] = useState('');
  const [selectedQuestions, setSelectedQuestions] = useState('');
  const [quizQuestions, setQuizQuestions] = useState([]);
  const navigate = useNavigate();

  // Handle retry configuration
  useEffect(() => {
    if (retryConfig && retryConfig.isRetry) {
      // Set the form values from retry config
      if (retryConfig.selectedParentTopic) {
        setSelectedParentTopic(retryConfig.selectedParentTopic);
      }
      if (retryConfig.selectedChildTopic) {
        setSelectedChildTopic(retryConfig.selectedChildTopic);
      }
      if (retryConfig.selectedQuestions) {
        setSelectedQuestions(retryConfig.selectedQuestions);
      }

      // Set the categories data if available
      if (retryConfig.parentCategories) {
        setParentCategories(retryConfig.parentCategories);
      }
      if (retryConfig.childrenCategories) {
        setChildrenCategories(retryConfig.childrenCategories);
      }

      // Auto-generate quiz after a short delay to ensure state is set
      const timer = setTimeout(() => {
        if (retryConfig.selectedParentTopic && retryConfig.selectedQuestions) {
          handleGenerateQuiz();
        }
      }, 500);

      return () => clearTimeout(timer);
    }
  }, [retryConfig]);

  useEffect(() => {
    const unsubscribe = auth.onAuthStateChanged((user) => {
      if (user) {
        fetchParentCategories();
      } else {
        // Clear categories and show login message for unauthenticated users
        setParentCategories([]);
        setChildrenCategories([]);
        setError('Please log in to view categories');
      }
    });

    return () => unsubscribe();
  }, []);

  const fetchParentCategories = async () => {
    setCategoriesLoading(true);
    setError(null);
    try {
      const baseUrl =
        import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

      // Get Firebase ID token
      const user = auth.currentUser;

      if (!user) {
        throw new Error('User not authenticated');
      }

      const idToken = await user.getIdToken();

      const response = await axios.get(`${baseUrl}/api/v1/categories/roots`, {
        headers: {
          Authorization: `Bearer ${idToken}`,
        },
      });

      setParentCategories(response.data);
    } catch (error) {
      console.error('Error fetching parent categories:', error);
      setError('Failed to load parent categories');
    } finally {
      setCategoriesLoading(false);
    }
  };

  const fetchChildCategories = async (parentId) => {
    try {
      const user = auth.currentUser;
      if (!user) return;
      const idToken = await user.getIdToken();

      const baseUrl =
        import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
      const response = await axios.get(
        `${baseUrl}/api/v1/categories/${parentId}/children`,
        {
          headers: { Authorization: `Bearer ${idToken}` },
        }
      );

      setChildrenCategories(response.data);
    } catch (error) {
      console.error('Error fetching child categories:', error);
    }
  };

  // Call when parent category changes
  useEffect(() => {
    if (selectedParentTopic) {
      fetchChildCategories(selectedParentTopic);
    } else {
      setChildrenCategories([]);
    }
  }, [selectedParentTopic]);

  // Handle quiz generation
  const handleGenerateQuiz = async () => {
    setLoading(true);
    setError(null);

    try {
      if (!selectedParentTopic) {
        setError('Please select an area.');
        setLoading(false);
        return;
      }

      const rawCategoryId =
        selectedChildTopic && selectedChildTopic !== 'All'
          ? selectedChildTopic
          : selectedParentTopic;

      const categoryId = Number(rawCategoryId);

      const total = parseInt(selectedQuestions, 10);
      if (!total || Number.isNaN(total)) {
        setError('Please select the number of questions.');
        setLoading(false);
        return;
      }

      const user = auth.currentUser;
      if (!user) {
        setError('User not authenticated.');
        setLoading(false);
        return;
      }

      const idToken = await user.getIdToken(/* forceRefresh = */ false);

      const baseUrl =
        import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

      // Resolve backend internal user id (the backend expects the DB user.id)
      let internalUserId = null;
      try {
        const meResp = await axios.get(`${baseUrl}/api/v1/users/me`, {
          headers: { Authorization: `Bearer ${idToken}` },
        });
        internalUserId = meResp?.data?.id || null;
      } catch (e) {
        console.error('Failed to resolve internal user id via /users/me', e);
      }

      if (!internalUserId) {
        setError(
          'Could not resolve internal user id. Ensure a backend user record exists for this Firebase account.'
        );
        setLoading(false);
        return;
      }

      const payload = {
        userId: internalUserId,
        categoryId,
        total,
        modeOverride: null,
      };

      const response = await axios.post(
        `${baseUrl}/api/v1/questions/generate`,
        payload,
        {
          headers: { Authorization: `Bearer ${idToken}` },
        }
      );

      const data = response.data;

      // Extract quiz information and questions from the new response format
      const quizInfo = {
        id: data.quizId,
        attemptId: data.attemptId,
        questions: data.questions,
      };

      // Prepare quiz configuration for display in QuizSettings
      const quizConfig = {
        selectedParentTopic,
        selectedChildTopic,
        selectedQuestions,
        parentCategories,
        childrenCategories,
      };

      setQuizQuestions(quizInfo);
      sessionStorage.setItem('quizQuestions', JSON.stringify(quizInfo));
      sessionStorage.setItem('quizConfig', JSON.stringify(quizConfig));
      navigate('/quiz', {
        state: {
          quiz: quizInfo,
          config: quizConfig,
        },
      });
    } catch (err) {
      console.error('Error while generating the quiz', err);
      const message =
        err?.response?.data?.message ||
        err?.response?.data ||
        err?.message ||
        'Error while generating the quiz, please try again.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {error && (
        <div style={{ color: 'red', marginBottom: '32px' }}>Error: {error}</div>
      )}
      <div className='d-flex justify-content-between align-items-end gap-3'>
        {/* GENERAL TOPIC SELECT */}
        <div className='selector'>
          <p className='selector-p small-text text-muted'>Area</p>
          <select
            name='general-topic'
            id='general-topic-select'
            className='form-select'
            value={selectedParentTopic}
            onChange={(e) => setSelectedParentTopic(e.target.value)}
          >
            <option value='' disabled>
              SELECT
            </option>
            {parentCategories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        {/* SPECIFIC TOPIC SELECT */}
        <div className='selector'>
          <p className='selector-p small-text text-muted'>Topic (optional)</p>
          <select
            name='specific-topic'
            id='specific-topic-select'
            className='form-select'
            value={selectedChildTopic}
            onChange={(e) => setSelectedChildTopic(e.target.value)}
          >
            <option value='All'>All</option>
            {childrenCategories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        {/* QUESTIONS SELECT */}
        <div className='selector'>
          <p className='selector-p small-text text-muted'>
            Select number of questions
          </p>
          <select
            name='questions'
            id='questions-select'
            className='form-select'
            value={selectedQuestions}
            onChange={(e) => setSelectedQuestions(e.target.value)}
          >
            <option value='' disabled>
              SELECT
            </option>
            <option value={5}>5</option>
            <option value={10}>10</option>
            <option value={15}>15</option>
            <option value={20}>20</option>
          </select>
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
          onClick={handleGenerateQuiz}
          disabled={loading}
          type='button'
        >
          {loading ? 'Generating...' : 'Shuffle'}
        </button>
      </div>
    </>
  );
}

export default QuizSelector;
