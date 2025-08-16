function App() {
  return (
    <>
      <div className='container py-4 px-3 mx-auto'>
        <h1>Hello, Bootstrap and Vite!</h1>
        <button className='btn btn-primary'>Primary button</button>
        <button className='btn btn-secondary ms-2'>Secondary button</button>

        {/* Test Bootstrap components */}
        <div className='mt-4'>
          <div className='alert alert-success' role='alert'>
            Bootstrap is working! 🎉
          </div>

          <div className='card' style={{ width: '18rem' }}>
            <div className='card-body'>
              <h5 className='card-title'>Bootstrap Card</h5>
              <p className='card-text'>
                This card proves that Bootstrap CSS and components are working
                correctly.
              </p>
              <button className='btn btn-outline-primary'>Learn More</button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default App;
