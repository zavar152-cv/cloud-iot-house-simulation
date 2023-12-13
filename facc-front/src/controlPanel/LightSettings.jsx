import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';
import DeleteIcon from '@mui/icons-material/Delete';
import DoneIcon from '@mui/icons-material/Done';
import EditIcon from '@mui/icons-material/Edit';
import {
  Fab, Grid, IconButton, MenuItem, Paper, Select, TextField, Typography,
} from '@mui/material';
import _default from '@mui/material/styles/identifier';
import PropTypes from 'prop-types';
import React, {useEffect, useState} from 'react';
import axios from 'axios';
import {v4 as uuidv4} from 'uuid';
import {generateCronExpression, generateTimeSlot} from '../utils/cron.js';
import {TimetableCard} from './TimetabeCard.jsx';

export const LightSettings = ({actions}) => {
  const [timetable, setTimetable] = useState([]);
  const [group, setGroup] = useState("LIGHT_GROUP")
  let getTimetable = () => {
    axios.get(import.meta.env.VITE_API_URL + 'scheduler/timetable-entries').
        then((res) => {
          let data = res.data.filter(el => el.group === group);
          let table = [];
          let handledIds = [];
          data.forEach((element) => {
            console.log(element);
            let uuid = element.name.split('_')[0];
            if (handledIds.includes(uuid)) return;
            let timeExpr = {};
            timeExpr[element.name.split('_')[1]] = element.cronExpression;
            let second = data.filter(
                el => el.name.split('_')[0] === uuid && el != element)[0];
            console.log(second);
            timeExpr[second.name.split('_')[1]] = second.cronExpression;
            let timeslot = generateTimeSlot(
                timeExpr.on0 === undefined ? timeExpr.on : timeExpr.on0,
                timeExpr.off0 === undefined ? timeExpr.off : timeExpr.off0);
            table.push({
              id: uuid, onId: element.cronExpression === (timeExpr.on0 === undefined ? timeExpr.on : timeExpr.on0)
                  ? element.id
                  : second.id, offId: element.cronExpression === (timeExpr.off0 === undefined ? timeExpr.off : timeExpr.off0)
                  ? element.id
                  : second.id, ...timeslot, isNew: false,
            });
            handledIds.push(uuid);
          });
          setTimetable(table);
        }).
        catch((err) => {
          alert(err);
        });
  };
  let removeTimeSlotById = (id) => {
    setTimetable(timetable.filter((t) => t.id != id));
  };

  let updateTimeSlotById = (id, newState) => {
    setTimetable(timetable.map((t, i) => (i === id ? newState : t)));
  };

  let saveCommand = (name, actionId, cronExpression) => {
    axios.post(
        import.meta.env.VITE_API_URL + 'scheduler/timetable-entries/group', {
          name: name, group: group, description: uuidv4(),
          cronExpression: cronExpression, actionId: actionId, arguments: [],
        }).
        then(() => {
          if (name.split('_')[1] === 'off') getTimetable();
        }).
        catch(() => {
          alert('Ошибка сохранения!');
        });
  };

  let editCommand = (id, name, actionId, cronExpression) => {
    axios.put(
        import.meta.env.VITE_API_URL + 'scheduler/timetable-entries/' + id, {
          name: name, description: uuidv4(), cronExpression: cronExpression,
          actionId: actionId, arguments: [],
        }).
        then(() => {
          if (name.split('_')[1] === 'off') getTimetable();
        }).
        catch(() => {
          alert('Ошибка сохранения!');
        });
  };

  let deleteCommand = (id, name) => {
    axios.delete(
        import.meta.env.VITE_API_URL + 'scheduler/timetable-entries/' + id, ).
        then(() => {
          if (name.split('_')[1] === 'off') getTimetable();
        }).
        catch(() => {
          alert('Ошибка удаления!');
        });
  };

  let saveTimeSlot = (timeslot) => {
    let cronExpressions = generateCronExpression(timeslot);
    let id = uuidv4();
    saveCommand(id + '_on',
        actions.filter(action => action.name === 'light_on')[0].id,
        cronExpressions[0]);
    saveCommand(id + '_off',
        actions.filter(action => action.name === 'light_off')[0].id,
        cronExpressions[1]);
    0;
  };

  let editTimeSlot = (timeslot) => {
    let cronExpressions = generateCronExpression(timeslot);
    console.log(timeslot);
    editCommand(timeslot.onId, timeslot.id + '_on',
        actions.filter(action => action.name === 'light_on')[0].id,
        cronExpressions[0]);
    editCommand(timeslot.offId, timeslot.id + '_off',
        actions.filter(action => action.name === 'light_off')[0].id,
        cronExpressions[1]);
  };

  let deleteTimeSlot = (timeslot) => {
    console.log(timeslot);
    deleteCommand(timeslot.onId, timeslot.id + '_on');
    deleteCommand(timeslot.offId,timeslot.id + '_off');
  };

  useEffect(() => {
    getTimetable();
  }, []);

  return (<Paper elevation={1}>
    <Grid
        container
        flexDirection="column"
        alignItems="center"
        spacing={1}
        sx={{paddingBottom: '5%'}}
    >
      <Grid
          item
          width="94%"
      >
        <Typography variant="h6">Расписание работы освещения:</Typography>
      </Grid>
      {timetable.map((e) => {
        return (<Grid
            item
            width="94%"
            key={e.id}
        >
          <TimetableCard
              id={e.id}
              onId={e.onId}
              offId={e.offId}
              dayOfWeek={e.dayOfWeek}
              from={e.from}
              to={e.to}
              remove={removeTimeSlotById}
              update={updateTimeSlotById}
              isNew={e.isNew}
              saveTimeslot={saveTimeSlot}
              editTimeslot={editTimeSlot}
              deleteTimeslot={deleteTimeSlot}
          />
        </Grid>);
      })}
      <Grid item>
        <Fab
            color="primary"
            onClick={() => {
              setTimetable([
                ...timetable, {
                  id: timetable.length === 0 ? 0 : timetable[timetable.length -
                                                             1].id + 1,
                  dayOfWeek: 0, from: '00:00', to: '00:01', isNew: true,
                },
              ]);
            }}
        >
          <AddIcon/>
        </Fab>
      </Grid>
    </Grid>
  </Paper>);
};

LightSettings.propTypes = {
  actions: PropTypes.array,
};
