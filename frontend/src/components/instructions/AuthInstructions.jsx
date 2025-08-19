function AuthInstructions() {
  return (
    <>
      <div className='d-fex flex-column gap-4 bg-light p-4 rounded-3'>
        <p className='font-family-primary'>
          Enter your settings below to generate a quiz tailored to your goals.
        </p>
        <p className='font-family-primary'>
          Duo mode: for each question, the answer can be revealed by the person
          quizzing you. Solo mode: simply answer the questions to reveal the
          answer.
        </p>
      </div>
    </>
  );
}

export default AuthInstructions;
