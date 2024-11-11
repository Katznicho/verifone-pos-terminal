/* eslint-disable prettier/prettier */
import React from 'react';
import { Button } from 'react-native';
import { NativeModules } from 'react-native';

const { NewLandModule } = NativeModules;

const App = () => {
  // Define the button press handler
  const handleButtonPress = () => {
    NewLandModule.initNfcAuth((message) => {
      console.log("NFC Authentication Result:", message);
      // You can use the result (message) to update the UI or perform other actions
    });
  };

  return (
    <Button title="Start NFC Authentication" onPress={handleButtonPress} />
  );
};

export default App;
