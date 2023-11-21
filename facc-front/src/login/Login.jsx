import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import {
	Button,
	FormControl,
	Grid,
	IconButton,
	InputAdornment,
	InputLabel,
	OutlinedInput,
	TextField,
	Typography,
} from '@mui/material';
import React, { useEffect, useState } from 'react';

import { useNavigate } from 'react-router-dom';

export const Login = () => {
	const navigate = useNavigate();

	const [showPassword, setShowPassword] = useState(false);
	const [loginForm, setLoginForm] = useState({
		login: '',
		password: '',
	});
	const [error, setError] = useState('');

	const handleClickShowPassword = () => setShowPassword((show) => !show);

	const handleMouseDownPassword = (event) => {
		event.preventDefault();
	};

	const handleLogin = () => {
		/*here should be authorization request*/
		if (loginForm.login === 'admin' && loginForm.password === 'admin')
			navigate('/');
		else {
			setError('Неверный логин или пароль');
		}
	};

	useEffect(() => {
		document.title = 'Войти';
	});

	return (
		<Grid
			container
			flexDirection='row'
			alignItems='center'
			justifyContent='center'
			sx={{ height: '100%' }}
		>
			<Grid
				item
				xs='2'
			>
				<form
					onSubmit={(e) => {
						e.preventDefault();
						handleLogin();
					}}
				>
					<Grid
						container
						flexDirection='column'
						alignItems='center'
						justifyContent='center'
						spacing={4}
					>
						<Grid
							item
							sx={{ width: '100%' }}
						>
							<TextField
								required
								fullWidth
								variant='outlined'
								label='Логин'
								type='text'
								value={loginForm.login}
								onChange={(e) => {
									setLoginForm({ ...loginForm, login: e.target.value });
								}}
							/>
						</Grid>
						<Grid
							item
							sx={{ width: '100%' }}
						>
							<FormControl
								required
								variant='outlined'
								fullWidth
							>
								<InputLabel htmlFor='outlined-adornment-password'>
									Пароль
								</InputLabel>
								<OutlinedInput
									id='outlined-adornment-password'
									type={showPassword ? 'text' : 'password'}
									value={loginForm.password}
									onChange={(e) => {
										setLoginForm({ ...loginForm, password: e.target.value });
									}}
									endAdornment={
										<InputAdornment position='end'>
											<IconButton
												aria-label='toggle password visibility'
												onClick={handleClickShowPassword}
												onMouseDown={handleMouseDownPassword}
												color={!showPassword ? 'primary' : ''}
												edge='end'
											>
												{showPassword ? <VisibilityOff /> : <Visibility />}
											</IconButton>
										</InputAdornment>
									}
									label='Password'
								/>
							</FormControl>
						</Grid>
						<Grid
							item
							sx={{ width: '60%' }}
						>
							<Button
								fullWidth
								variant='contained'
								type='submit'
							>
								Войти
							</Button>
						</Grid>
						<Grid item>
							<Typography
								variant='caption'
								color='red
                                '
							>
								{error}
							</Typography>
						</Grid>
					</Grid>
				</form>
			</Grid>
		</Grid>
	);
};
