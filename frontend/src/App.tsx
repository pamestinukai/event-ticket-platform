import { useEffect, useState } from 'react';
import { getCount, incrementCount } from './api/';

function App() {
  const [count, setCount] = useState<number | null>(null);

  useEffect(() => {
    getCount()
      .then(setCount)
      .catch(() => setCount(null));
  }, []);

  const handleIncrement = () => {
    incrementCount().then(setCount).catch(() => setCount(null));
  };

  return (
    <>
      <p>Count: {count ?? '...'}</p>
      <button onClick={handleIncrement}>Increment</button>
    </>
  );
}

export default App;
