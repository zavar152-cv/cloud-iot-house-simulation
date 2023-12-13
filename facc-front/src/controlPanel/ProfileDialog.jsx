import {
  Dialog, DialogContent, DialogTitle, Grid, IconButton, TextField, Typography,
} from '@mui/material';
import PropTypes from 'prop-types';
import {useState} from 'react';
import CheckIcon from '@mui/icons-material/Check';
import ClearIcon from '@mui/icons-material/Clear';

export const ProfileDialog = ({onClose, open, username, updateUsername, updatePassword}) => {
  const [tmpName, setTmpName] = useState(username);
  const [tmpPassword, setTmpPassword] = useState('');

  return (<Dialog onClose={onClose} open={open} maxWidth="sm"
                  fullWidth>
    <DialogTitle>
      <Typography variant="h6">Настройки профиля</Typography>
    </DialogTitle>
    <DialogContent>
      <Grid container direction='column' rowSpacing={1} sx={{paddingTop: '1%'}}>
        <Grid item container sx={{paddingTop: '1%'}}>
          <Grid item>
            <TextField size="small" type="text" label="Логин" value={tmpName}
                       onChange={e => {setTmpName(e.target.value);}}/>
          </Grid>
          {username !== tmpName ? <>
            <IconButton
                onClick={() => {
                  updateUsername(tmpName);
                }}
                sx={{marginLeft: '2%'}}
            >
              <CheckIcon color="primary"/>
            </IconButton>
            <IconButton
                onClick={() => {
                  setTmpName(username);
                }}
                sx={{marginLeft: '2%'}}
            >
              <ClearIcon color="primary"/>
            </IconButton>
          </> : null}

        </Grid>
        <Grid item container sx={{paddingTop: '1%'}}>
          <Grid item>
            <TextField size="small" type="text" label="Пароль" value={tmpPassword}
                       onChange={e => {setTmpPassword(e.target.value);}}/>
          </Grid>
          {tmpPassword !== '' ? <>
            <IconButton
                onClick={() => {
                  updatePassword(tmpPassword);
                }}
                sx={{marginLeft: '2%'}}
            >
              <CheckIcon color="primary"/>
            </IconButton>
            <IconButton
                onClick={() => {
                  setTmpPassword('');
                }}
                sx={{marginLeft: '2%'}}
            >
              <ClearIcon color="primary"/>
            </IconButton>
          </> : null}

        </Grid>
      </Grid>
    </DialogContent>
  </Dialog>);
};

ProfileDialog.propTypes = {
  onClose: PropTypes.func, open: PropTypes.bool, username: PropTypes.string,
  updateUsername: PropTypes.func, updatePassword: PropTypes.func
};