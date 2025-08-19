import AuthInstructions from '../components/instructions/AuthInstructions';
import IndexInstructions from '../components/instructions/IndexInstructions';
import QuizSelector from '../components/quiz/QuizSelector';
import { useAuth } from '../contexts/AuthContext';

function HomePage() {
  const { currentUser } = useAuth();

  return currentUser ? (
    <>
      <AuthInstructions />

      <div style={{ marginTop: '96px' }}>
        <QuizSelector />
      </div>
    </>
  ) : (
    <IndexInstructions />
  );
}

export default HomePage;
