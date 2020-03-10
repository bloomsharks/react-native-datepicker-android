// import { requireNativeComponent } from 'react-native';

// const DatePickerAndroid = requireNativeComponent('DatePickerAndroid', {
//   nativeOnly: {onChange: true},
// });

// export default DatePickerAndroid;

import React from 'react';
import {requireNativeComponent} from 'react-native';
import PropTypes from 'prop-types';

class DatePickerAndroid extends React.Component {
  constructor(props) {
    super(props);
    this._onChange = this._onChange.bind(this);
  }
  _onChange(event: Event) {
    if (!this.props.onValueChange) {
      return;
    }
    this.props.onValueChange(event.nativeEvent);
  }
  render() {
    return <RCTMyCustomView {...this.props} onChange={this._onChange} />;
  }
}
DatePickerAndroid.propTypes = {
  /**
   * Callback that is called continuously when the user is dragging the map.
   */
  onChangeMessage: PropTypes.func,
};

const RCTMyCustomView = requireNativeComponent('DatePickerAndroid', {
  nativeOnly: {onChange: true},
});
module.exports = DatePickerAndroid;
