function AuthInstructions() {
  return (
    <>
      <div className='d-fex flex-column gap-4 bg-light p-4 rounded-3'>
        <p>
          Enter your settings below to generate a quiz tailored to your goals.
        </p>
        <div>
          <p>
            <strong>Duo mode:</strong> for each question, the answer can be
            revealed by the person quizzing you.
          </p>
          <p>
            <strong>Solo mode:</strong> simply answer the questions to reveal
            the answer.
          </p>
        </div>
      </div>
    </>
  );
}

export default AuthInstructions;
