function DataCard({ cardTitle, cardData }) {
  return (
    <div className='data-card selector'>
      <div className='text-light text-center'>{cardTitle}</div>
      <div className='button-text text-center fw-bold'>{cardData}</div>
    </div>
  );
}

export default DataCard;
