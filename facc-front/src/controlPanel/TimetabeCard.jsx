import React, {useState} from 'react';
import _default from '@mui/material/styles/identifier.js';
import {
  Grid, IconButton, MenuItem, Paper, Select, TextField, Typography,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit.js';
import DoneIcon from '@mui/icons-material/Done.js';
import DeleteIcon from '@mui/icons-material/Delete.js';
import CloseIcon from '@mui/icons-material/Close.js';
import PropTypes from 'prop-types';
export const TimetableCard = ({
  id, onId, offId, dayOfWeek, from, to, remove, update, isNew, saveTimeslot,
  editTimeslot, deleteTimeslot,
}) => {
  const [isEdit, setIsEdit] = useState(isNew);
  const [tmpTimeSlot, setTmpTimeSlot] = useState({
    id: id, onId: onId, offId: offId, dayOfWeek: dayOfWeek, from: from, to: to,
    isNew: isNew,
  });

  const getDayOfWeekName = (dayOfWeekNumber) => {
    switch (dayOfWeekNumber) {
      default:
        return 'Ошибка';
      case 0:
        return 'Понедельник';
      case 1:
        return 'Вторник';
      case 2:
        return 'Среда';
      case 3:
        return 'Четверг';
      case 4:
        return 'Пятница';
      case 5:
        return 'Суббота';
      case 6:
        return 'Воскресенье';
      case 7:
        return 'Ежедневно';
    }
  };

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
          xs={isEdit ? 3 : 4}
      >
        {!isEdit ? (<Typography
            color="primary.contrastText"
            variant="body1"
        >
          {getDayOfWeekName(tmpTimeSlot.dayOfWeek)}
        </Typography>) : (<Select
            fullWidth
            size="small"
            value={tmpTimeSlot.dayOfWeek}
            sx={{color: 'primary.contrastText'}}
            onChange={(e) => {
              setTmpTimeSlot({...tmpTimeSlot, dayOfWeek: e.target.value});
            }}
        >
          {[...Array(8).keys()].map((e) => {
            return (<MenuItem
                value={e}
                key={e}
            >
              {getDayOfWeekName(e)}
            </MenuItem>);
          })}
        </Select>)}
      </Grid>
      <Grid
          item
          xs={isEdit ? 6 : 4}
      >
        {!isEdit ? (<Typography
            color="primary.contrastText"
            variant="body1"
        >
          {tmpTimeSlot.from} - {tmpTimeSlot.to}
        </Typography>) : (<Grid
            item
            container
            justifyContent="space-between"
        >
          <Grid
              item
              xs={5.5}
          >
            <TextField
                fullWidth
                variant="outlined"
                type="time"
                inputProps={{style: {color: 'white'}}}
                size="small"
                value={tmpTimeSlot.from}
                onChange={(e) => {
                  setTmpTimeSlot({...tmpTimeSlot, from: e.target.value});
                }}
            />
          </Grid>
          <Grid
              item
              xs={5.5}
          >
            <TextField
                fullWidth
                type="time"
                inputProps={{style: {color: 'white'}}}
                size="small"
                value={tmpTimeSlot.to}
                onChange={(e) => {
                  setTmpTimeSlot({...tmpTimeSlot, to: e.target.value});
                }}
            />
          </Grid>
        </Grid>)}
      </Grid>
      <Grid item>
        {!isEdit ? (<IconButton
            sx={{color: 'primary.contrastText'}}
            onClick={() => {
              setIsEdit(true);
            }}
        >
          <EditIcon/>
        </IconButton>) : (<IconButton
            sx={{color: 'primary.contrastText'}}
            onClick={() => {
              if (isNew) {
                saveTimeslot(tmpTimeSlot);
              } else {
                editTimeslot(tmpTimeSlot);
              }
              setIsEdit(false);
              update(id, {...tmpTimeSlot, isNew: false});
            }}
        >
          <DoneIcon/>
        </IconButton>)}
        {!isEdit ? (<IconButton
            sx={{color: 'primary.contrastText'}}
            onClick={() => {
              deleteTimeslot(tmpTimeSlot);
            }}
        >
          <DeleteIcon/>
        </IconButton>) : (<IconButton
            sx={{color: 'primary.contrastText'}}
            onClick={() => {
              if (isNew) {
                remove(id);
              } else {
                setIsEdit(false);
                setTmpTimeSlot({
                  id: id, dayOfWeek: dayOfWeek, from: from, to: to,
                  isNew: isNew,
                });
              }
            }}
        >
          <CloseIcon/>
        </IconButton>)}
      </Grid>
    </Grid>
  </Paper>);
};

TimetableCard.propTypes = {
  id: PropTypes.string, onId: PropTypes.number, offId: PropTypes.number,
  dayOfWeek: PropTypes.number, from: PropTypes.string, to: PropTypes.string,
  remove: PropTypes.func, update: PropTypes.func, isNew: PropTypes.bool,
  saveTimeslot: PropTypes.func, deleteTimeslot: PropTypes.func,
  editTimeslot: PropTypes.func,
};