import _default from '@mui/material/styles/identifier.js';

const getDayOfWeekName = (dayOfWeekNumber) => {
  switch (dayOfWeekNumber) {
    case _default:
      return 'error';
    case 0:
      return 2;
    case 1:
      return 3;
    case 2:
      return 4;
    case 3:
      return 5;
    case 4:
      return 6;
    case 5:
      return 7;
    case 6:
      return 1;
    case 7:
      return '*';
  }
};

const getDayOfWeekNameReverse = (dayOfWeekNumber) => {
  switch (dayOfWeekNumber) {
    case _default:
      return 'error';
    case "2":
      return 0;
    case "3":
      return 1;
    case "4":
      return 2;
    case "5":
      return 3;
    case "6":
      return 4;
    case "7":
      return 5;
    case "1":
      return 6;
    case '*':
      return 7;
  }
};

export let generateCronExpression = (timetableRow) => {
  return [
    '? ' + timetableRow.from.split(':')[1] + ' ' +
    timetableRow.from.split(':')[0] + ' ? * ' +
    getDayOfWeekName(timetableRow.dayOfWeek),
    timetableRow.to !== undefined ? '? ' + timetableRow.to.split(':')[1] + ' ' + timetableRow.to.split(':')[0] +
    ' ? * ' + getDayOfWeekName(timetableRow.dayOfWeek) : null,
  ];
};

export let generateTimeSlot = (expressionFrom, expressionTo) => {
  let on = expressionFrom.split(' ');
  let off = expressionTo === undefined ? null : expressionTo.split(' ');
  console.log(getDayOfWeekNameReverse(on[5]))
  return {
    dayOfWeek: getDayOfWeekNameReverse(on[5]),
    from: on[2] + ":" + on[1],
    to: expressionTo === undefined ? null : off[2] + ":" + off[1]
  };
};