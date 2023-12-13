import {Grid} from '@mui/material';
import React, {useEffect, useState} from 'react';
import {Footer} from './Footer';
import {Header} from './Header';
import axios from 'axios';
import {useNavigate} from 'react-router-dom';
import {SystemSettings} from './SystemSettings.jsx';
import {ProfileDialog} from './ProfileDialog.jsx';
import {UsersSettings} from './UsersSettings.jsx';

export const ControlPanel = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [username, setUsername] = useState('');
  const [userId, setUserId] = useState('');
  const [isAdmin, setIsAdmin] = useState(false);
  const [currentTab, setCurrentTab] = useState('SYSTEM_CONTROL');
  const [isProfileOpen, setIsProfiledOpen] = useState(false);
  const userPages = [
    {
      label: 'Настройки системы', action: () => setCurrentTab('SYSTEM_CONTROL'),
    },
  ];
  const adminPages = [
    {
      label: 'Настройки системы', action: () => setCurrentTab('SYSTEM_CONTROL'),
    },
    {
      label: 'Управление пользователями',
      action: () => setCurrentTab('USERS_CONTROL'),
    },
  ];
  const settings = [
    {
      label: 'Профиль', action: () => {setIsProfiledOpen(true);},
    }, {
      label: 'Выйти', action: () => {
        localStorage.removeItem('accessToken');
        location.reload();
      },
    },
  ];

  let navigate = useNavigate();

  let checkAuth = () => {
    const accessToken = localStorage.accessToken;

    if (accessToken === undefined) {
      navigate('/login', {replace: true});
      return;
    }
    axios.get(import.meta.env.VITE_API_URL + 'auth/auth', {
      headers: {Authorization: 'Bearer ' + accessToken},
    }).then((response) => {
      setIsAuthenticated(true);
      setUsername(response.data.username);
      setUserId(response.data.id);
      setIsAdmin(response.data.roles.includes('ROLE_ADMIN'));
    }).catch(() => {
      navigate('/login', {replace: true});
    });
  };

  let updateUsername = (username) => {
    axios.put(import.meta.env.VITE_API_URL + 'auth/users/' + userId + '/name',
        {username: username}).
        then(() => {
          setUsername(username);
          setIsProfiledOpen(false);
        }).
        catch((err) => {
          console.log(err.response);
          alert('Ошибка обновления логина! ' + err.response.data.violations[0].message);
        });
  };

  let updatePasswrod = (password) => {
    axios.put(import.meta.env.VITE_API_URL + 'auth/users/' + userId + '/password',
        {password: password}).
        then(() => {
          setIsProfiledOpen(false);
        }).
        catch((err) => {
          console.log(err.response);
          alert('Ошибка обновления пароля! ' + err.response.data.violations[0].message);
        });
  }
  let renderPage = (tab) => {
    switch (tab) {
      default:
        return null;
      case 'SYSTEM_CONTROL':
        return <SystemSettings/>;
      case 'USERS_CONTROL':
        return <UsersSettings/>
    }
  };
  useEffect(() => {
    checkAuth();
  }, []);

  return (<>{isAuthenticated ? (<Grid
      container
      flexDirection="column"
      alignItems='center'
  >
    <Grid item width='100%'>
      <Header
          pages={isAdmin ? adminPages : userPages}
          settings={settings}
          username={username}
      />
    </Grid>
    {renderPage(currentTab)}
    <Grid item>
      <Footer/>
    </Grid>
    <ProfileDialog onClose={() => {setIsProfiledOpen(false);}}
                   open={isProfileOpen} username={username}
                   updateUsername={updateUsername} updatePassword={updatePasswrod}/>
  </Grid>) : null}</>);
};
