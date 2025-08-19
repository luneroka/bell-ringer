import Navbar from '../components/navbar/Navbar';
import QuizQuestion from '../components/quiz/QuizQuestion';
import QuizSettings from '../components/quiz/QuizSettings';
import { IoIosArrowForward } from 'react-icons/io';

function QuizPage() {
  /*   const questionData = {
    question:
      'What are the two main reasons for using useEffect in a React component?',
    type: 'short_answer',
    choices: [],
    correctChoice : '',
    answerText:
      'The two main reasons to use useEffect in a React component are to run code after rendering (such as API calls or subscriptions) and to clean up those effects when the component unmounts or when dependencies change.',
  }; */

  const questionData = {
    question:
      'Among the following actions, which are common use cases for useEffect in a React component?',
    type: 'unique_choice',
    choices: [
      'Manage the component’s local state',
      'Apply CSS styles',
      'Run code after the component rendered',
      'Define routes in the application',
    ],
    correctChoice: 'Run code after the component rendered',
    answerText: '',
  };

  return (
    <>
      <Navbar />
      <QuizSettings />
      <QuizQuestion
        index={1}
        question={questionData.question}
        type={questionData.type}
        choices={questionData.choices}
        answerText={questionData.answerText}
      />
      <button className='next-btn'>
        <IoIosArrowForward />
      </button>
    </>
  );
}

export default QuizPage;
