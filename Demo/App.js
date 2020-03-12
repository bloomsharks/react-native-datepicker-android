import React, {useState} from 'react';
import {StatusBar, Text, View} from 'react-native';
import moment from 'moment';
import DatePickerAndroid from 'react-native-datepicker-android';

const App: () => React$Node = () => {
  
  const minDate = moment()
    .subtract(100, 'years')
    .toDate();

  const maxDate = moment()
    .subtract(4, 'years')
    .toDate();

  const [selectedDate, setSelectedDate] = useState(moment(maxDate).format("YYYY-MM-DD"));

  return (
    <View
      style={{
        flex: 1,
        backgroundColor: '#d3d3d3',
        alignItems: 'center',
        justifyContent: 'center',
      }}>
      <StatusBar barStyle="dark-content" />
      <Text style={{color: 'black'}}>{moment(selectedDate).format('YYYY-MM-DD')}</Text>
      {console.log(selectedDate)}
      <DatePickerAndroid
        style={{
          width: '100%',
          height: 150,
          position: 'absolute',
          bottom: 0,
        }}
        value={moment(selectedDate).format('YYYY-MM-DD')}
        minDate={moment(minDate).format('YYYY-MM-DD')}
        maxDate={moment(maxDate).format('YYYY-MM-DD')}
        onValueChange={event => {
          console.log(`onValueChange ${event.date}`)
          setSelectedDate(moment(event.date).format('YYYY-MM-DD'));
        }}
      />
    </View>
  );
};

export default App;
