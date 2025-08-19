import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { auth } from '../../utils/firebase.config';

function QuizSelector() {
  const [parentCategories, setParentCategories] = useState([]);
  const [childrenCategories, setChildrenCategories] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [selectedParentTopic, setSelectedParentTopic] = useState('');
  const [selectedChildTopic, setSelectedChildTopic] = useState('');
  const [selectedQuestions, setSelectedQuestions] = useState('');

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
    setLoading(true);
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
      setLoading(false);
    }
  };

  const fetchChildCategories = async (parentId) => {
    try {
      const user = auth.currentUser;
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

  return (
    <div className='d-flex justify-content-between align-items-end gap-3'>
      {error && <div style={{ color: 'red' }}>Error: {error}</div>}

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
          <option value='frontend'>All</option>
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
      <Link to='/quiz'>
        <button
          className='btn btn-primary button-text'
          style={{
            width: '100%',
            minWidth: '120px',
            maxWidth: '400px',
            height: '48px',
          }}
        >
          Shuffle
        </button>
      </Link>
    </div>
  );
}

export default QuizSelector;
