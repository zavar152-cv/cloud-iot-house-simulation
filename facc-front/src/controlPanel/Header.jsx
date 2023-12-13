import {
	AppBar,
	Avatar,
	Box,
	Button,
	Container,
	IconButton,
	Menu,
	MenuItem,
	Toolbar,
	Tooltip,
	Typography,
} from '@mui/material';
import PropTypes from 'prop-types';
import React from 'react';

export const Header = ({ pages, settings, username }) => {
	const [anchorElUser, setAnchorElUser] = React.useState(null);
	const handleOpenUserMenu = (event) => {
		setAnchorElUser(event.currentTarget);
	};
	const handleCloseUserMenu = () => {
		setAnchorElUser(null);
	};
	function stringToColor(string) {
		let hash = 0;
		let i;

		/* eslint-disable no-bitwise */
		for (i = 0; i < string.length; i += 1) {
			hash = string.charCodeAt(i) + ((hash << 5) - hash);
		}

		let color = '#';

		for (i = 0; i < 3; i += 1) {
			const value = (hash >> (i * 8)) & 0xff;
			color += `00${value.toString(16)}`.slice(-2);
		}
		/* eslint-enable no-bitwise */

		return color;
	}

	function stringAvatar(name) {
		return {
			sx: {
				bgcolor: stringToColor(name),
			},
			children: `${name.split(' ')[0][0]}`,
		};
	}

	return (
		<AppBar position='static'>
			<Container maxWidth='x1'>
				<Toolbar>
					<Box sx={{ flexGrow: 1, display: { xs: 'none', md: 'flex' } }}>
						{pages.map((page) => (
							<Button
								key={page.label}
								sx={{ color: 'white', display: 'block' }}
								onClick={page.action}
							>
								{page.label}
							</Button>
						))}
					</Box>
					<Box sx={{ flexGrow: 0 }}>
						<Tooltip title='Настройки'>
							<IconButton
								onClick={handleOpenUserMenu}
								sx={{ p: 0 }}
							>
								<Avatar {...stringAvatar(username)} />
							</IconButton>
						</Tooltip>
						<Menu
							sx={{ mt: '45px' }}
							id='menu-appbar'
							anchorEl={anchorElUser}
							anchorOrigin={{
								vertical: 'top',
								horizontal: 'right',
							}}
							keepMounted
							transformOrigin={{
								vertical: 'top',
								horizontal: 'right',
							}}
							open={Boolean(anchorElUser)}
							onClose={handleCloseUserMenu}
						>
							{settings.map((setting) => (
								<MenuItem
									key={setting.label}
									onClick={setting.action}
								>
									<Typography textAlign='center'>{setting.label}</Typography>
								</MenuItem>
							))}
						</Menu>
					</Box>
				</Toolbar>
			</Container>
		</AppBar>
	);
};

Header.propTypes = {
	pages: PropTypes.array,
	settings: PropTypes.array,
	username: PropTypes.string,
};
