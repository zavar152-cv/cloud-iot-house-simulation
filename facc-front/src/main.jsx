import { GlobalStyles } from '@mui/styled-engine';
import React from 'react';
import ReactDOM from 'react-dom/client';
import { App } from './App.jsx';

const inputGlobalStyles = (
	<GlobalStyles
		styles={() => ({
			//main styles
			'html, body': {
				boxSizing: 'border-box',
				margin: 0,
				height: '100%',
				fontFamily: 'Roboto, sans-serif',
				overflow: 'visible',
				width: '100%',
			},
			'#root': {
				width: '100%',
			},
		})}
	/>
);

ReactDOM.createRoot(document.getElementById('root')).render(
	<React.StrictMode>
		{inputGlobalStyles}
		<App />
	</React.StrictMode>
);
