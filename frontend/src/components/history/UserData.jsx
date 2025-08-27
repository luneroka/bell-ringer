import { useEffect, useState } from 'react';
import DataCard from './DataCard';
import axios from 'axios';
import { auth } from '../../utils/firebase.config';

function UserData({ refreshTrigger }) {
  const [numberCompleted, setNumberCompleted] = useState(0);
  const [successRate, setSuccessRate] = useState(0);
  const [favorite, setFavorite] = useState('Loading...');
  const [mustImprove, setMustImprove] = useState('Loading...');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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
    const fetchUserData = async () => {
      // If this is a refresh after quiz completion, add a small delay
      // to ensure backend has processed the completion
      if (refreshTrigger) {
        await new Promise((resolve) => setTimeout(resolve, 1000));
      }
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

        // Add timestamp to prevent caching issues
        const timestamp = new Date().getTime();

        // Fetch user stats
        const statsResponse = await axios.get(
          `${baseUrl}/api/v1/attempts/user/${userId}/stats?t=${timestamp}`,
          { headers }
        );
        const stats = statsResponse.data;

        setNumberCompleted(stats.completedAttempts || 0);
        // Use successRate instead of completionRate for the success rate calculation
        setSuccessRate(Math.round((stats.successRate || 0) * 100));

        // Fetch categories to analyze performance
        const categoriesResponse = await axios.get(
          `${baseUrl}/api/v1/categories/roots`,
          { headers }
        );
        const categories = categoriesResponse.data;

        // Analyze performance across categories
        await analyzePerformanceByCategory(
          userId,
          categories,
          headers,
          baseUrl
        );
      } catch (error) {
        console.error('Error fetching user data:', error);
        setError('Failed to load user statistics');
        setFavorite('Error');
        setMustImprove('Error');
      } finally {
        setLoading(false);
      }
    };

    const analyzePerformanceByCategory = async (
      userId,
      categories,
      headers,
      baseUrl
    ) => {
      try {
        // Get all completed attempts for the user
        const completedAttemptsResponse = await axios.get(
          `${baseUrl}/api/v1/attempts/user/${userId}/completed`,
          { headers }
        );
        const completedAttempts = completedAttemptsResponse.data;

        // Count attempts per category and calculate accuracy
        const categoryStatsPromises = categories.map(async (category) => {
          try {
            // Get the effective category IDs (includes children)
            const categoryIdsResponse = await axios.get(
              `${baseUrl}/api/v1/categories/${category.id}/ids`,
              { headers }
            );
            const effectiveIds = categoryIdsResponse.data.effectiveIds;

            // Count completed attempts that belong to any of these category IDs
            let completedCategoryAttempts = 0;

            // For each attempt, fetch the quiz details to get the categoryId
            for (const attempt of completedAttempts) {
              try {
                const quizResponse = await axios.get(
                  `${baseUrl}/api/v1/quizzes/${attempt.quizId}`,
                  { headers }
                );
                const quiz = quizResponse.data;

                if (quiz.categoryId && effectiveIds.includes(quiz.categoryId)) {
                  completedCategoryAttempts++;
                }
              } catch (error) {
                console.error(`Error fetching quiz ${attempt.quizId}:`, error);
              }
            }

            // Get accuracy for this category
            let accuracy = 0;
            try {
              const accuracyResponse = await axios.get(
                `${baseUrl}/api/v1/quizzes/accuracy?userId=${userId}&categoryId=${category.id}`,
                { headers }
              );
              const accuracyData = accuracyResponse.data;
              accuracy =
                (accuracyData.easy + accuracyData.medium + accuracyData.hard) /
                3;
            } catch (error) {
              // No accuracy data available for this category
              accuracy = 0;
            }

            return {
              name: category.name,
              categoryId: category.id,
              attemptCount: completedCategoryAttempts,
              accuracy: accuracy,
            };
          } catch (error) {
            return {
              name: category.name,
              categoryId: category.id,
              attemptCount: 0,
              accuracy: 0,
            };
          }
        });

        const categoryStats = await Promise.all(categoryStatsPromises);

        // Filter categories with data
        const categoriesWithAttempts = categoryStats.filter(
          (cat) => cat.attemptCount > 0
        );

        if (categoriesWithAttempts.length > 0) {
          // Favorite = category with most attempts
          const favoriteCategory = categoriesWithAttempts.reduce(
            (prev, current) =>
              prev.attemptCount > current.attemptCount ? prev : current
          );

          // To work on = category with lowest accuracy (among those with attempts)
          const categoriesWithAccuracy = categoriesWithAttempts.filter(
            (cat) => cat.accuracy > 0
          );

          let worstCategory;
          if (categoriesWithAccuracy.length > 0) {
            worstCategory = categoriesWithAccuracy.reduce((prev, current) =>
              prev.accuracy < current.accuracy ? prev : current
            );
          } else {
            // If no accuracy data, pick the category with fewest attempts
            worstCategory = categoriesWithAttempts.reduce((prev, current) =>
              prev.attemptCount < current.attemptCount ? prev : current
            );
          }

          setFavorite(favoriteCategory.name);
          setMustImprove(worstCategory.name);
        } else {
          setFavorite('No data yet');
          setMustImprove('No data yet');
        }
      } catch (error) {
        console.error('Error analyzing performance:', error);
        setFavorite('No data');
        setMustImprove('No data');
      }
    };

    fetchUserData();
  }, [refreshTrigger]); // Add refreshTrigger to dependency array

  if (error) {
    return (
      <div className='alert alert-danger' role='alert'>
        {error}
      </div>
    );
  }

  return (
    <div
      className='d-flex justify-content-between align-items-end gap-3'
      style={{ marginBottom: '96px' }}
    >
      <DataCard
        cardTitle='COMPLETED'
        cardData={loading ? '...' : numberCompleted}
      />
      <DataCard
        cardTitle='SUCCESS RATE'
        cardData={loading ? '...' : `${successRate}%`}
      />
      <DataCard cardTitle='FAVORITE' cardData={loading ? '...' : favorite} />
      <DataCard
        cardTitle='TO WORK ON'
        cardData={loading ? '...' : mustImprove}
      />
    </div>
  );
}

export default UserData;
