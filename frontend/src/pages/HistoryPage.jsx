import HistoryTable from '../components/history/HistoryTable';
import UserData from '../components/history/UserData';
import { useLocation } from 'react-router-dom';

function HistoryPage() {
  const location = useLocation();

  // Get refresh flag from navigation state (when returning from quiz results)
  const refreshUserData = location.state?.refreshUserData;

  return (
    <div>
      <UserData refreshTrigger={refreshUserData} />
      <HistoryTable refreshTrigger={refreshUserData} />
    </div>
  );
}

export default HistoryPage;
