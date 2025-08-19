import { Link } from 'react-router-dom';

function IndexInstructions() {
  return (
    <>
      <div className='d-fex flex-column gap-4 bg-light p-4 rounded-3'>
        <p className='font-family-primary fw-semibold'>
          Welcome to Bell Ringer !
        </p>
        <p>
          Bell Ringer is a revision platform designed to make learning web
          development more interactive and enjoyable. <br />
          Whether you’re on your own or with a partner, you can choose a topic,
          enable shuffle mode, and test your knowledge through questions created
          to help you progress.
        </p>
        <p>
          The idea behind Bell Ringer is simple: to offer a user-friendly tool
          suited for both pair revisions (your partner quizzes you) and solo
          practice.
        </p>
        <div>
          <Link to='/login'>Login</Link> or <Link to='/register'>Sign Up</Link>{' '}
          and enjoy learning while having fun!
        </div>
      </div>
    </>
  );
}

export default IndexInstructions;
