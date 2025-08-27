import { useLocation } from 'react-router-dom';
import AuthInstructions from '../components/instructions/AuthInstructions';
import IndexInstructions from '../components/instructions/IndexInstructions';
import QuizSelector from '../components/quiz/QuizSelector';
import { useAuth } from '../contexts/AuthContext';
import UserData from '../components/history/UserData';

function HomePage() {
  const { currentUser } = useAuth();
  const location = useLocation();

  // Get retry configuration from navigation state
  const retryConfig = location.state?.retryConfig;
  // Get refresh flag from navigation state (when returning from quiz results)
  const refreshUserData = location.state?.refreshUserData;

  return currentUser ? (
    <>
      <UserData refreshTrigger={refreshUserData} />

      <AuthInstructions />

      <div style={{ marginTop: '96px' }}>
        <QuizSelector retryConfig={retryConfig} />
      </div>
    </>
  ) : (
    <IndexInstructions />
  );
}

export default HomePage;
