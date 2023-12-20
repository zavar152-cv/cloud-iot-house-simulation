import AddIcon from '@mui/icons-material/Add';
import {
  Divider,
  Fab,
  Grid,
  IconButton,
  MenuItem,
  Paper,
  Select,
  TextField,
  Typography,
} from '@mui/material';
import PropTypes from 'prop-types';
import React, {useEffect, useState} from 'react';
import axios from 'axios';
import {generateCronExpression, generateTimeSlot} from '../utils/cron.js';
import {v4 as uuidv4} from 'uuid';
import {CloudUpload} from '@mui/icons-material';
import DeleteIcon from '@mui/icons-material/Delete';
import DoneIcon from '@mui/icons-material/Done';
import EditIcon from '@mui/icons-material/Edit';
import CloseIcon from '@mui/icons-material/Close';

export const ActionTimetableCard = ({
  id, name, dayOfWeek, from, remove, update, isNew, saveTimeslot, editTimeslot,
  deleteTimeslot, actions, actionId
}) => {
  const [isEdit, setIsEdit] = useState(isNew);
  const [tmpTimeSlot, setTmpTimeSlot] = useState({
    id: id, name: name, dayOfWeek: dayOfWeek, from: from, isNew: isNew, actionId: actionId
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
          xs={isEdit ? 3 : 4}
      >
        {!isEdit ? (<Typography
            color="primary.contrastText"
            variant="body1"
        >
          {actions.filter(e => tmpTimeSlot.actionId === e.id)[0].command}
        </Typography>) : (<Select
            fullWidth
            size="small"
            value={tmpTimeSlot.actionId}
            sx={{color: 'primary.contrastText'}}
            onChange={(e) => {
              console.log(e.target.value)
              setTmpTimeSlot({...tmpTimeSlot, actionId: e.target.value});
            }}
        >
          {actions.map((e, index) => {
            return (<MenuItem
                value={e.id}
                key={index}
            >
              {e.command}
            </MenuItem>);
          })}
        </Select>)}
      </Grid>
      <Grid
          item
          xs={isEdit ? 4 : 2}
      >
        {!isEdit ? (<Typography
            color="primary.contrastText"
            variant="body1"
        >
          {tmpTimeSlot.from}
        </Typography>) : (<Grid
            item
            container
            justifyContent="center"
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
                  id: id, dayOfWeek: dayOfWeek, from: from, isNew: isNew,
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

ActionTimetableCard.propTypes = {
  id: PropTypes.string, actionId: PropTypes.number, dayOfWeek: PropTypes.number,
  from: PropTypes.string, remove: PropTypes.func, update: PropTypes.func,
  isNew: PropTypes.bool, saveTimeslot: PropTypes.func,
  deleteTimeslot: PropTypes.func, editTimeslot: PropTypes.func, actions: PropTypes.array, name: PropTypes.string
};
const ActionCard = ({commandId, updateActions, command, fileId}) => {
  const [imagePreview, setImagePreview] = useState(null);
  const [imageFile, setImageFile] = useState(null);

  let handleImagePreview = (e) => {
    let image_as_base64 = URL.createObjectURL(e.target.files[0]);
    let image_as_files = e.target.files[0];

    setImagePreview(image_as_base64);
    setImageFile(image_as_files);
  };

  let handleSubmitFile = () => {

    if (imageFile !== null) {

      let formData = new FormData();
      formData.append('file', imageFile);
      // the image field name should be similar to your api endpoint field name
      // in my case here the field name is customFile

      axios.post(import.meta.env.VITE_API_URL + 'device/commands-for-actions/' +
                 commandId + '/file', formData, {
        headers: {
          'Content-type': 'multipart/form-data',
        },
      }).then(res => {
        updateActions();
      }).catch(err => {
        console.log(err);
      });
    }
  };

  let deleteFile = () => {
    axios.delete(import.meta.env.VITE_API_URL + 'device/commands-for-actions/' +
                 commandId + '/file').then(res => {
      updateActions();
    }).catch(err => {
      console.log(err);
    });
  };

  useEffect(() => {
    handleSubmitFile();
  }, [imageFile]);
  return <Paper
      elevation={1}
      sx={{backgroundColor: 'primary.main', padding: '3%'}}
  >
    <Grid
        container
        justifyContent="space-between"
        alignItems="center"
    >
      <Grid item>
        <Typography color="primary.contrastText"
                    variant="body1">{command}</Typography>
      </Grid>
      <Grid item>
        {fileId === null ? <IconButton component="label"
                                       sx={{color: 'primary.contrastText'}}>
          <CloudUpload/>
          <input
              type="file"
              hidden
              onChange={handleImagePreview}
              accept=".mp3,audio/*"
          />
        </IconButton> : <IconButton sx={{color: 'primary.contrastText'}}
                                    onClick={deleteFile}>
          <DeleteIcon/>
        </IconButton>}
      </Grid>
    </Grid>
  </Paper>;
};

ActionCard.propTypes = {
  updateActions: PropTypes.func, command: PropTypes.string,
  fileId: PropTypes.any, commandId: PropTypes.number,
};
export const VoiceSettings = ({acts}) => {
  const [timetable, setTimetable] = useState([

  ]);
  const [actions, setActions] = useState([]);
  const [group, setGroup] = useState('SPEAKERS_GROUP');

  let getActions = () => {
    axios.get(import.meta.env.VITE_API_URL + 'device/commands-for-actions').
        then((res) => {
          setActions(res.data);
          getTimetable();
        }).
        catch((err) => {
          alert(err);
        });
  };
  let getTimetable = () => {
    axios.get(import.meta.env.VITE_API_URL + 'scheduler/timetable-entries').
        then((res) => {
          let data = res.data.filter(el => el.group === group);
          let table = [];
          let handledIds = [];
          data.forEach((element) => {
            let timeslot = generateTimeSlot(element.cronExpression);
            table.push({
              id: element.id, name: element.name,
              actionId: element.arguments[0], ...timeslot, isNew: false,
            });
            setTimetable(table);
          })
        }).
        catch((err) => {
          alert(err);
        });
  }
    let removeTimeSlotById = (id) => {
      setTimetable(timetable.filter((t) => t.id != id));
    };

    let updateTimeSlotById = (id, newState) => {
      setTimetable(timetable.map((t, i) => (i === id ? newState : t)));
    };

    let saveCommand = (name, actionId, cronExpression, argument) => {
      axios.post(
          import.meta.env.VITE_API_URL + 'scheduler/timetable-entries/group', {
            name: name, group: group, description: uuidv4(),
            cronExpression: cronExpression, actionId: actionId,
            arguments: [argument],

          }).
          then(() => {
            getTimetable();
          }).
          catch(() => {
            alert('Ошибка сохранения!');
          });
    };

    let editCommand = (name, id, actionId, cronExpression, argument) => {
      axios.put(
          import.meta.env.VITE_API_URL + 'scheduler/timetable-entries/' + id, {
            name: name, description: uuidv4(), cronExpression: cronExpression,
            actionId: actionId, arguments: [argument],
          }).
          then(() => {
            getTimetable();
          }).
          catch(() => {
            alert('Ошибка сохранения!');
          });
    };

    let deleteCommand = (id) => {
      axios.delete(
          import.meta.env.VITE_API_URL + 'scheduler/timetable-entries/' + id).
          then(() => {
            getTimetable();
          }).
          catch(() => {
            alert('Ошибка удаления!');
          });
    };

    let saveTimeSlot = (timeslot) => {
      let cronExpressions = generateCronExpression(timeslot);
      let id = uuidv4();
      console.log(acts)
      saveCommand(id,
          acts.filter(action => action.name === 'play_voice_command')[0].id,
          cronExpressions[0], timeslot.actionId);
    };

    let editTimeSlot = (timeslot) => {
      let cronExpressions = generateCronExpression(timeslot);
      console.log(timeslot);
      editCommand(timeslot.name, timeslot.id, acts.filter(action => action.name === 'play_voice_command')[0].id,
          cronExpressions[0], timeslot.actionId);
    };

    let deleteTimeSlot = (timeslot) => {
      console.log(timeslot);
      deleteCommand(timeslot.id, timeslot.id + '_on');
    };

    useEffect(() => {
      getActions();
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
          <Typography variant="h6">Настройка голосовых команд:</Typography>
        </Grid>
        <Grid
            item
            width="100%"
        >
          <Divider/>
        </Grid>
        {actions.map((action, index) => {
          return (<Grid
              item
              width="94%"
              key={index}
          >
            <ActionCard
                commandId={action.id}
                updateActions={getActions}
                command={action.command}
                fileId={action.fileId}

            />
          </Grid>);
        })}
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
          <Typography variant="h6">Расписание:</Typography>
        </Grid>
        {timetable.map((e) => {
          return (<Grid
              item
              width="94%"
              key={e.id}
          >
            <ActionTimetableCard
                id={e.id}
                actionId={e.actionId}
                actions={actions}
                dayOfWeek={e.dayOfWeek}
                from={e.from}
                remove={removeTimeSlotById}
                update={updateTimeSlotById}
                isNew={e.isNew}
                saveTimeslot={saveTimeSlot}
                editTimeslot={editTimeSlot}
                deleteTimeslot={deleteTimeSlot}
                name={e.name}
            />
          </Grid>);
        })}
        <Grid item>
          <Fab
              color="primary"
              onClick={() => {
                setTimetable([
                  ...timetable, {
                    id: timetable.length === 0
                        ? 0
                        : timetable[timetable.length - 1].id + 1, dayOfWeek: 0,
                    from: '00:00', isNew: true, actionId: actions[0].id
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

  VoiceSettings.propTypes = {
    acts: PropTypes.array,
  };

