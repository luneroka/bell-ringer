import QuizSelector from '../components/quiz/QuizSelector';
import QuizSettings from '../components/quiz/QuizSettings';

function HomePage() {
  return (
    <>
      <div className='d-fex flex-column gap-4 bg-light p-4 rounded-3'>
        <p className='font-family-primary body-text fw-semibold'>
          Welcome to Bell Ringer !
        </p>
        <p className='font-family-primary body-text'>
          Bell Ringer is a revision platform designed to make learning web
          development more interactive and enjoyable. <br />
          Whether you’re on your own or with a partner, you can choose a topic,
          enable shuffle mode, and test your knowledge through questions created
          to help you progress.
        </p>
        <p className='font-family-primary body-text'>
          The idea behind Bell Ringer is simple: to offer a user-friendly tool
          suited for both pair revisions (your partner quizzes you) and solo
          practice.
        </p>
      </div>

      <div style={{ marginTop: '96px' }}>
        <QuizSelector />
      </div>
      <div style={{ marginTop: '96px' }}>
        <QuizSettings />
      </div>
    </>
  );
}

export default HomePage;
