import IndexInstructions from '../components/instructions/IndexInstructions';
import QuizSelector from '../components/quiz/QuizSelector';
import { useAuth } from '../contexts/AuthContext';

function HomePage() {
  const { currentUser } = useAuth();

  return (
    <>
      <IndexInstructions />
      {currentUser && (
        <div style={{ marginTop: '96px' }}>
          <QuizSelector />
        </div>
      )}
    </>
  );
}

export default HomePage;
