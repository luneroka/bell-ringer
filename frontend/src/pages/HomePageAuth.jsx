import IndexInstructions from '../components/instructions/IndexInstructions';
import QuizSelector from '../components/quiz/QuizSelector';

function HomePageAuth() {
  return (
    <>
      <IndexInstructions />
      <div style={{ marginTop: '96px' }}>
        <QuizSelector />
      </div>
    </>
  );
}

export default HomePageAuth;
