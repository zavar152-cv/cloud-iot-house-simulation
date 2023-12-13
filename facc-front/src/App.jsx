import React from 'react';
import { Outlet, Route, Routes } from 'react-router';
import { BrowserRouter } from 'react-router-dom';
import { Login } from './login/Login';
import {ControlPanel} from './controlPanel/ControlPanel.jsx';

export const App = () => {
	return (
		<BrowserRouter>
			<Routes>
				<Route
					path='/'
					element={<Outlet />}
				>
					<Route
						index
						element={<ControlPanel/>}
					/>
					<Route
						path='login'
						
						element={<Login />}
					/>
				</Route>
			</Routes>
		</BrowserRouter>
	);
};
