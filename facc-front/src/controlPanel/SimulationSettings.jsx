import AddIcon from '@mui/icons-material/Add';
import {Divider, Fab, Grid, Paper, Switch, Typography} from '@mui/material';
import {v4 as uuidv4} from 'uuid';
import PropTypes from 'prop-types';
import React, {useEffect, useState} from 'react';
import axios from 'axios';
import {TimetableCard} from './TimetabeCard.jsx';
import {generateCronExpression, generateTimeSlot} from '../utils/cron.js';

export const SimulationSettings = ({activatedFunctions, update}) => {
  const [timetable, setTimetable] = useState([]);

  let getTimetable = () => {
    axios.get(import.meta.env.VITE_API_URL + 'scheduler/simulation/schedule').
        then((res) => {
          let table = [];
          res.data.forEach((element) => {
            let timeslot = generateTimeSlot(element.startCronExpression,
                element.endCronExpression);
            table.push({
              id: element.id,
              name: element.name,
              ...timeslot, isNew: false,
            });
            setTimetable(table);
          })
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

  let saveTimeSlot = (timeslot) => {
    let cronExpressions = generateCronExpression(timeslot);
    axios.post(import.meta.env.VITE_API_URL + 'scheduler/simulation/schedule', {
      name: uuidv4(), startCronExpression: cronExpressions[0],
      endCronExpression: cronExpressions[1],
    }).
        then(() => {
          getTimetable();
        }).
        catch(() => {
          alert('Ошибка сохранения!');
        });
  };

  let editTimeSlot = (timeslot) => {
    let cronExpressions = generateCronExpression(timeslot);
    axios.put(
        import.meta.env.VITE_API_URL + 'scheduler/simulation/schedule/' + timeslot.id, {
          startCronExpression: cronExpressions[0],
          endCronExpression: cronExpressions[1]
        }).
        then(() => {
          getTimetable();
        }).
        catch(() => {
          alert('Ошибка сохранения!');
        });
  };

  let deleteTimeSlot = (timeslot) => {
    axios.delete(
        import.meta.env.VITE_API_URL + 'scheduler/simulation/schedule/' + timeslot.id).
        then(() => {
          getTimetable();
        }).
        catch(() => {
          alert('Ошибка удаления!');
        });
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
          container
          flexDirection="row"
          justifyContent="space-between"
          alignItems="center"
          width="94%"
      >
        <Grid item>
          <Typography variant="h6">Режим симуляции</Typography>
        </Grid>
        <Grid item>
          <Switch
              checked={activatedFunctions.simulation}
              onChange={(e) => {
                update({...activatedFunctions, simulation: e.target.checked});
                axios.put(import.meta.env.VITE_API_URL +
                          '/scheduler/simulation?state=' +
                          (e.target.checked ? 'Enabled' : 'Disabled')).
                    then(() => {}).
                    catch(() => {
                      alert('Ошибка переключения режима!');
                      update({
                        ...activatedFunctions, simulation: !e.target.checked,
                      });
                    });
              }}
              color="success"
          />
        </Grid>
      </Grid>

      <Grid
          item
          width="100%"
      >
        <Divider/>
      </Grid>
      <Grid
          item
          width="94%"
      >
        <Typography variant="h6">Используемые функции:</Typography>
      </Grid>
      <Grid
          item
          container
          flexDirection="row"
          alignItems="center"
          // justifyContent='space-between'
          width="94%"
      >
        <Grid
            item
            xs={5}
        >
          <Typography variant="subtitle1">Управление светом</Typography>
        </Grid>
        <Grid item>
          <Switch
              checked={activatedFunctions.LIGHT_GROUP}
              onChange={(e) => {
                update({
                  ...activatedFunctions, LIGHT_GROUP: e.target.checked,
                });
                axios.put(import.meta.env.VITE_API_URL +
                          'device/groups/LIGHT_GROUP?status=' +
                          e.target.checked).
                    then(() => {}).
                    catch(() => {
                      alert('Ошибка переключения режима!');
                      update({
                        ...activatedFunctions, LIGHT_GROUP: !e.target.checked,
                      });
                    });
              }}
              color="success"
          />
        </Grid>
      </Grid>
      <Grid
          item
          container
          flexDirection="row"
          alignItems="center"
          // justifyContent='space-between'
          width="94%"
      >
        <Grid
            item
            xs={5}
        >
          <Typography variant="subtitle1">Управление шторами</Typography>
        </Grid>
        <Grid item>
          <Switch
              checked={activatedFunctions.CURTAINS_GROUP}
              onChange={(e) => {
                update({
                  ...activatedFunctions, CURTAINS_GROUP: e.target.checked,
                });
                axios.put(import.meta.env.VITE_API_URL +
                          'device/groups/CURTAINS_GROUP?status=' +
                          e.target.checked).
                    then(() => {}).
                    catch(() => {
                      alert('Ошибка переключения режима!');
                      update({
                        ...activatedFunctions, CURTAINS_GROUP: !e.target.checked,
                      });
                    });
              }}
              color="success"
          />
        </Grid>
      </Grid>
      <Grid
          item
          container
          flexDirection="row"
          alignItems="center"
          // justifyContent='space-between'
          width="94%"
      >
        <Grid
            item
            xs={5}
        >
          <Typography variant="subtitle1">Управление музыкой</Typography>
        </Grid>
        <Grid item>
          <Switch
              checked={activatedFunctions.MUSIC_GROUP}
              onChange={(e) => {
                update({
                  ...activatedFunctions, MUSIC_GROUP: e.target.checked,
                });
                axios.put(import.meta.env.VITE_API_URL +
                          'device/groups/MUSIC_GROUP?status=' +
                          e.target.checked).
                    then(() => {}).
                    catch(() => {
                      alert('Ошибка переключения режима!');
                      update({
                        ...activatedFunctions, MUSIC_GROUP: !e.target.checked,
                      });
                    });
              }}
              color="success"
          />
        </Grid>
      </Grid>
      <Grid
          item
          container
          flexDirection="row"
          alignItems="center"
          // justifyContent='space-between'
          width="94%"
      >
        <Grid
            item
            xs={5}
        >
          <Typography variant="subtitle1">Подача голосовых
            команд</Typography>
        </Grid>
        <Grid item>
          <Switch
              checked={activatedFunctions.SPEAKERS_GROUP}
              onChange={(e) => {
                update({
                  ...activatedFunctions, SPEAKERS_GROUP: e.target.checked,
                });
                axios.put(import.meta.env.VITE_API_URL +
                          'device/groups/SPEAKERS_GROUP?status=' +
                          e.target.checked).
                    then(() => {}).
                    catch(() => {
                      alert('Ошибка переключения режима!');
                      update({
                        ...activatedFunctions, SPEAKERS_GROUP: !e.target.checked,
                      });
                    });
              }}
              color="success"
          />
        </Grid>
      </Grid>
      <Grid
          item
          width="100%"
      >
        <Divider/>
      </Grid>
      <Grid
          item
          width="94%"
      >
        <Typography variant="h6">Расписание работы:</Typography>
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
                  dayOfWeek: 0, from: '00:00', to: '00:00', isNew: true,
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

SimulationSettings.propTypes = {
  activatedFunctions: PropTypes.object, update: PropTypes.func,
};
