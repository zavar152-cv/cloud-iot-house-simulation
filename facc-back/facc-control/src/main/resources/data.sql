INSERT INTO types (id, name) VALUES
                                 (1, 'SimpleLamp'),
                                 (2, 'SimpleCurtains'),
                                 (3, 'SimpleSpeaker'),
                                 (4, 'SimpleMusic')
                             ON CONFLICT DO NOTHING;

INSERT INTO actions (id, action, action_group, arguments_count) VALUES
                                                                    (1, 'light_on', 'LIGHT_GROUP', 0),
                                                                    (2, 'light_off', 'LIGHT_GROUP', 0),
                                                                    (3, 'play_voice_command', 'SPEAKERS_GROUP', 1),
                                                                    (4, 'curtains_up', 'CURTAINS_GROUP', 0),
                                                                    (5, 'curtains_down', 'CURTAINS_GROUP', 0),
                                                                    (6, 'music_play', 'MUSIC_GROUP', 0),
                                                                    (7, 'music_stop', 'MUSIC_GROUP', 0),
                                                                    (8, 'music_volume', 'MUSIC_GROUP', 1)
                                                                ON CONFLICT DO NOTHING;

INSERT INTO commands_for_actions (id, command, action_id, file_id) VALUES
                                                                       (1, 'Включи свет', 1, null),
                                                                       (2, 'Выключи свет', 2, null),
                                                                       (3, 'Подними шторы', 4, null),
                                                                       (4, 'Опусти шторы', 5, null),
                                                                       (5, 'Включи музыку', 6, null),
                                                                       (6, 'Выключи музыку', 7, null)
                                                                ON CONFLICT DO NOTHING;

INSERT INTO devices (id, name, status, type_id, job_group) VALUES
                                                    ('fdgpiub234bx', 'LightDevice1', false, 1, 'LIGHT_GROUP'),
                                                    ('df14sdf124as', 'CurtainsDevice1', false, 2, 'CURTAINS_GROUP'),
                                                    ('vnbojfdg2425', 'SpeakerDevice1', false, 3, 'SPEAKERS_GROUP'),
                                                    ('plpyiyuob242', 'MusicDevice1', false, 4, 'MUSIC_GROUP')
                                                                ON CONFLICT DO NOTHING;