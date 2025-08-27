import DataCard from './DataCard';

function UserData() {
  const cardData = 12;

  return (
    <div
      className='d-flex justify-content-between align-items-end gap-3'
      style={{ marginBottom: '96px' }}
    >
      <DataCard cardTitle='COMPLETED' cardData={cardData} />
      <DataCard cardTitle='SUCCESS RATE' cardData={cardData} />
      <DataCard cardTitle='FAVORITE' cardData={cardData} />
      <DataCard cardTitle='TO WORK ON' cardData={cardData} />
    </div>
  );
}

export default UserData;
