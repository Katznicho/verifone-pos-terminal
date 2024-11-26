/* eslint-disable prettier/prettier */
import React from 'react';
import { Button, Alert, View } from 'react-native';
import { NativeModules } from 'react-native';

const { NewLandModule, QRCodeScannerModule } = NativeModules;

const App = () => {
  // QR Code Scan Handler
  const handleQrScanPress = () => {
    QRCodeScannerModule.startScanner((result) => {
      console.log("QR Code Scan Result:", result);
      Alert.alert("QR Code Scan", result);
    });
  };

  // Print Receipt Handler
  const handlePrintReceiptPress = () => {
    NewLandModule.initializeTRAPrinter();
  };

  // Authenticate M1 Card Handler
  const handleAuthenticateCardPress = () => {
    NewLandModule.authenticateM1CardDefault((error, result) => {
      if (error) {
        console.error('NFC Authentication Error:', error);
        Alert.alert('Error', `NFC Authentication Failed: ${error}`);
      } else {
        console.log('NFC Authentication Result:', result);
        Alert.alert('Success', `NFC Authentication Result: ${result}`);
      }
    });
  };

  // Read Block Data Handler
  const handleReadBlockDataPress = () => {
    NewLandModule.readM1CardBlock((error, result) => {
      if (error) {
        console.error('Error Reading Block Data:', error);
        Alert.alert('Error', `Failed to Read Block Data: ${error}`);
      } else {
        console.log('Block Data:', result);
        Alert.alert('Block Data', `Data: ${result}`);
      }
    });
  };

  return (
    <View style={{ padding: 20 }}>
      <View style={{ marginBottom: 10 }}>
        <Button title="Start QR Code Scan Android" onPress={handleQrScanPress} />
      </View>
      
      <View style={{ marginBottom: 10 }}>
        <Button title="Print Receipt" onPress={handlePrintReceiptPress} />
      </View>

      <View style={{ marginBottom: 10 }}>
        <Button title="Authenticate M1 Card" onPress={handleAuthenticateCardPress} />
      </View>

      <View style={{ marginBottom: 10 }}>
        <Button title="Read Block Data" onPress={handleReadBlockDataPress} />
      </View>
    </View>
  );
};

export default App;
