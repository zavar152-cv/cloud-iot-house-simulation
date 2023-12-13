import React, {useEffect, useState} from 'react';
import {Grid} from '@mui/material';
import {SimulationSettings} from './SimulationSettings.jsx';
import {LightSettings} from './LightSettings.jsx';
import {CurtainsSettings} from './CurtainsSettings.jsx';
import {MusicSettings} from './MusicSettings.jsx';
import axios from 'axios';
import {VoiceSettings} from './VoiceSettings.jsx';

export const SystemSettings = () => {
  const [activatedFunctions, setActivatedFunctions] = useState({
    simulation: false, LIGHT_GROUP: false, CURTAINS_GROUP: false,
    MUSIC_GROUP: false, SPEAKERS_GROUP: false,
  });
  const [actions, setActions] = useState([]);

  let getSimulationSettings = () => {
    axios.get(import.meta.env.VITE_API_URL + 'scheduler/simulation/state').
        then((res) => {
          let tmp = {};
          tmp.simulation = res.data.status === 'Enabled';
          axios.get(import.meta.env.VITE_API_URL + 'device/groups').
              then((res) => {
                res.data.forEach((group) => {
                  tmp[group.name] = group.status;
                });
                console.log(activatedFunctions);
                setActivatedFunctions(tmp);
              }).
              catch(() => {
                alert('Данные о статусах групп недоступны!');
              });
        }).
        catch(() => {
          alert('Данные о статусах групп недоступны!');
        });
  };

  let getActions = () => {
    axios.get(import.meta.env.VITE_API_URL + 'device/actions').
        then((res) => {
          setActions(res.data);
        }).
        catch(() => {
          alert('Данные о действиях недоступны!');
        });
  };

  useEffect(() => {
  }, [activatedFunctions]);

  useEffect(() => {
    getSimulationSettings();
    getActions();
  }, []);
  return (<Grid
      item
      container

      direction='row'
      sx={{padding: '4%'}}
      justifyContent="space-between"
      rowSpacing={5}
      columnSpacing={5}
  >
    <Grid
        item
        width="100%"
        xs={3}
    >
      <SimulationSettings
          activatedFunctions={activatedFunctions}
          update={setActivatedFunctions}
      />
    </Grid>
    <Grid
        item
        container
        flexWrap="wrap"
        justifyContent="space-between"
        rowSpacing={2}
        columnSpacing={5}
        xs={9}
    >
      {activatedFunctions.LIGHT_GROUP ? (<Grid
          item
          width="100%"
          xs={5}
      >
        <LightSettings
            actions={actions.filter(action => action.group === 'LIGHT_GROUP')}/>
      </Grid>) : null}
      {activatedFunctions.CURTAINS_GROUP ? (<Grid
          item
          width="100%"
          xs={5}
      >
        <CurtainsSettings actions={actions.filter(
            action => action.group === 'CURTAINS_GROUP')}/>
      </Grid>) : null}
      {activatedFunctions.MUSIC_GROUP ? (<Grid
          item
          width="100%"
          xs={5}
      >
        <MusicSettings
            actions={actions.filter(action => action.group === 'MUSIC_GROUP')}/>
      </Grid>) : null}
      {activatedFunctions.SPEAKERS_GROUP ? (<Grid
          item
          width="100%"
          xs={7}
      >
        <VoiceSettings acts={actions.filter(action => action.group === 'SPEAKERS_GROUP')}/>
      </Grid>) : null}
    </Grid>
  </Grid>);
};