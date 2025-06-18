import React from 'react';
import SearchForm from './components/SearchForm';

function App() {
  return (
    <div style={{ padding: '2rem' }}>
      <h1 style={{
      fontSize: '2.5rem',
      color: '#0077cc',
      textAlign: 'center',
      fontWeight: 'bold',
      marginBottom: '2rem',
      textShadow: '1px 1px 3px rgba(0,0,0,0.2)'
}}>Flight Finder</h1>
      <SearchForm />
    </div>
  );
}

export default App;
