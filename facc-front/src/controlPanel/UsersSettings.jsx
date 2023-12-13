import React, {useEffect, useState} from 'react';
import {
  Button,
  Dialog,
  DialogContent,
  DialogTitle,
  Fab,
  Grid,
  IconButton,
  Paper,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import PropTypes from 'prop-types';
import axios from 'axios';
import {AdminPanelSettings, Person} from '@mui/icons-material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add.js';

const UserCard = ({id, username, isAdmin, deleteUser,grantAdmin, revokeAdmin}) => {
  return (<Paper
      elevation={1}
      sx={{backgroundColor: 'primary.main', padding: '3%'}}
  >
    <Grid
        container
        justifyContent="space-between"
        alignItems="center"
    >
      <Grid
          item
          color="primary.contrastText"
          variant="body1"
      >
        <Typography>{username}</Typography>
      </Grid>
      <Grid
          item
      >
        <Typography color="primary.contrastText"
                    variant="body1">{isAdmin
            ? 'Администратор'
            : 'Пользователь'}</Typography>
      </Grid>
      <Grid
          item
          container
          justifyContent="space-between"
          xs={2}
      >
        <Grid
            item
        >
          {isAdmin ? <Tooltip title="Сделать пользователем">
            <IconButton onClick={() => {revokeAdmin(id)}}>
              <Person sx={{color: 'primary.contrastText'}}/>
            </IconButton>
          </Tooltip> : <Tooltip title="Сделать администратором">
            <IconButton onClick={() => {grantAdmin(id)}}>
              <AdminPanelSettings sx={{color: 'primary.contrastText'}}/>
            </IconButton>
          </Tooltip>}
        </Grid>
        <Grid
            item
        >
          <Tooltip title="Удалить пользователя">
            <IconButton onClick={() => {deleteUser(id)}}>
              <DeleteIcon sx={{color: 'primary.contrastText'}}/>
            </IconButton>
          </Tooltip>
        </Grid>
      </Grid>
    </Grid>
  </Paper>);
};

UserCard.propTypes = {
  id: PropTypes.number, username: PropTypes.string, isAdmin: PropTypes.bool, deleteUser: PropTypes.func, revokeAdmin: PropTypes.func, grantAdmin: PropTypes.func
};

export const UserDialog = ({onClose, open, addUser}) => {
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  return (<Dialog onClose={onClose} open={open} maxWidth="sm"
                  fullWidth>
    <DialogTitle>
      <Typography variant="h6">Добавить пользователя</Typography>
    </DialogTitle>
    <DialogContent>
      <Grid container sx={{paddingTop: '1%'}} direction='column' rowSpacing={2}>
        <Grid item>
          <TextField size="small" type="text" label="Логин" value={name}
                     onChange={e => {setName(e.target.value);}}/>
        </Grid>
        <Grid item>
          <TextField size="small" type="password" label="Пароль"
                     value={password}
                     onChange={e => {setPassword(e.target.value);}}/>
        </Grid>
        <Grid item>
          <Button variant="contained" onClick={() => {
            if (name === '' || password === '' || name.length < 5 ||
                name.length > 25) {
              setError(
                  'Логин и пароль не должны быть пустыми');
            } else {
              addUser(name, password);
              setName('');
              setPassword('');
              onClose();
            }
          }}>
            Добавить пользователя
          </Button>
        </Grid>
        <Grid item>
          <Typography
              variant="caption"
              color="red"
          >
            {error}
          </Typography>
        </Grid>
      </Grid>
    </DialogContent>
  </Dialog>);
};

UserDialog.propTypes = {
  onClose: PropTypes.func, open: PropTypes.bool, addUser: PropTypes.func,
};
export const UsersSettings = () => {
  const [users, setUsers] = useState([]);
  const [isAddUser, setIsAddUser] = useState(false);

  let getUsers = () => {
    axios.get(import.meta.env.VITE_API_URL + 'auth/users').
        then((res) => {
          setUsers(res.data);
        }).
        catch(() => {alert('Ошибка загрузки пользователей');});
  };

  let addUser = (username, password) => {
    axios.post(import.meta.env.VITE_API_URL + 'auth/signUp',
        {username: username, password: password}).
        then(() => {
          getUsers();
        }).
        catch((err) => {alert('Ошибка добавления пользователя:' + err.response.data.violations[0].message);});
  };

  let deleteUser = (id) => {
    axios.delete(import.meta.env.VITE_API_URL + 'auth/users/' + id,).
        then(() => {
          getUsers();
        }).
        catch((err) => {alert('Ошибка добавления пользователя:' + err.response.data.violations[0].message);});
  };
  let grantAdmin = (id) => {
    axios.put(import.meta.env.VITE_API_URL + 'auth/grantAdmin',
        {id: id}).
        then(() => {
          getUsers();
        }).
        catch(() => {alert('Ошибка изменения привилегий')});
  }

  let revokeAdmin = (id) => {
    axios.put(import.meta.env.VITE_API_URL + 'auth/revokeAdmin',
        {id: id}).
        then(() => {
          getUsers();
        }).
        catch(() => {alert('Ошибка изменения привилегий')});
  }
  useEffect(() => {
    getUsers();
  }, []);

  return (<Grid
      item
      container

      sx={{padding: '6%'}}
      width="50%"
      flexWrap="wrap"
      justifyContent="space-around"
      rowSpacing={5}
      columnSpacing={5}
  >
    {users.map((user, index) => {
      return (<Grid item width="94%" key={index}>
        <UserCard id={user.id} username={user.username}
                  isAdmin={user.roles.includes('ROLE_ADMIN')} deleteUser={deleteUser} grantAdmin={grantAdmin} revokeAdmin={revokeAdmin}/>
      </Grid>);
    })}
    <Grid item>
      <Fab
          color="primary"
          onClick={() => {
            setIsAddUser(true);
          }}
      >
        <AddIcon/>
      </Fab>
    </Grid>
    <UserDialog onClose={() => {setIsAddUser(false);}} open={isAddUser}
                addUser={addUser}/>
  </Grid>);
};