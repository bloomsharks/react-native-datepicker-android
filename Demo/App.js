import React, {useState} from 'react';
import {StatusBar, Text, View} from 'react-native';

import DatePickerAndroid from 'react-native-date-picker-android';

const App: () => React$Node = () => {
  const [selectedDate, setSelectedDate] = useState('empty');
  return (
    <View
      style={{
        flex: 1,
        backgroundColor: '#d3d3d3',
        alignItems: 'center',
        justifyContent: 'center',
      }}>
      <StatusBar barStyle="dark-content" />
      <Text style={{color: 'black'}}>{selectedDate}</Text>
      <DatePickerAndroid
        style={{
          width: '100%',
          height: 150,
          position: 'absolute',
          bottom: 0,
        }}
        value="2014-12-01"
        minDate="2013-01-02"
        maxDate="2015-01-01"
        onValueChange={event => {
          setSelectedDate(event.date);
        }}
      />
    </View>
  );
};

export default App;
