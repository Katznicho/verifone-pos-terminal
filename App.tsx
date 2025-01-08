import React, { useEffect } from 'react';
import { View, Button, Alert, NativeEventEmitter } from 'react-native';
import { NativeModules } from 'react-native';

const { StabexModule } = NativeModules;
const eventEmitter = new NativeEventEmitter(StabexModule);

const App = () => {
  
  useEffect(() => {
    const eventListeners = [
      eventEmitter.addListener('MSRSuccess', (message) => {
        console.log('Card read successfully:', message);
        Alert.alert('Card Read Success', message);
      }),
      eventEmitter.addListener('MSRError', (message) => {
        console.error('MSR Error:', message);
        Alert.alert('MSR Error', message);
      }),
      eventEmitter.addListener('MSRTimeout', (message) => {
        console.error('MSR Timeout:', message);
        Alert.alert('MSR Timeout', message);
      }),
      eventEmitter.addListener('ScanSuccess', (message) => {
        console.log('Scan success:', message);
        Alert.alert('Scan Success', message);
      }),
      eventEmitter.addListener('ScanError', (message) => {
        console.error('Scan Error:', message);
        Alert.alert('Scan Error', message);
      }),
      eventEmitter.addListener('ScanTimeout', (message) => {
        console.error('Scan Timeout:', message);
        Alert.alert('Scan Timeout', message);
      }),
      eventEmitter.addListener('ScanCancel', (message) => {
        console.log('Scan Cancel:', message);
        Alert.alert('Scan Cancel', message);
      }),
      eventEmitter.addListener('RFCardPass', (message) => {
        console.log('RF Card Pass:', message);
        Alert.alert('RF Card Pass', message);
      }),
      eventEmitter.addListener('RFSearchFail', (message) => {
        console.error('RF Search Fail:', message);
        Alert.alert('RF Search Fail', message);
      }),
      eventEmitter.addListener('RFCardNumber', (message) => {
        console.log('RF Card Number:', message);
        Alert.alert('RF Card Number', message);
      }),
      eventEmitter.addListener('RFCardReaderError', (message) => {
        console.error('RF Card Reader Error:', message);
        Alert.alert('RF Card Reader Error', message);
      }),
      eventEmitter.addListener('RFCardReaderException', (message) => {
        console.error('RF Card Reader Exception:', message);
        Alert.alert('RF Card Reader Exception', message);
      }),
      eventEmitter.addListener('RFCardNumberError', (message) => {
        console.error('RF Card Number Error:', message);
        Alert.alert('RF Card Number Error', message);
      })
    ];

    return () => {
      eventListeners.forEach(listener => listener.remove());
    };
  }, []);

  const doPrint = async () => {
    try {
      await StabexModule.doPrintString();
      Alert.alert('Print Command Sent', 'The print command was sent successfully.');
    } catch (e) {
      console.error('Error printing:', e);
      Alert.alert('Print Error', 'An error occurred while sending the print command.');
    }
  };

  const doMsr = async () => {
    try {
      await StabexModule.doMsr();
    } catch (e) {
      console.error('Error in MSR:', e);
      Alert.alert('MSR Error', 'An error occurred while sending the MSR command.');
    }
  };

  const doScan = async () => {
    try {
      await StabexModule.doScan();
    } catch (e) {
      console.error('Error scanning:', e);
      Alert.alert('Scan Error', 'An error occurred while sending the scan command.');
    }
  };

  const doRFID = async () => {
    try {
      await StabexModule.doRFID();
    } catch (e) {
      console.error('Error RFID:', e);
      Alert.alert('RFID Error', 'An error occurred while sending the RFID command.');
    }
  };

  const readRFIDData = async () => {
    try {
      await StabexModule.readRFData();
      //readRFData();
    } catch (e) {
      console.error('Error reading RFID data:', e);
      Alert.alert('RFID Read Error', 'An error occurred while reading the RFID data.');
    }
  };

  return (
    <View style={{ alignItems: 'center', justifyContent: 'center', flex: 1 }}>
      <Button title="Print" color="#841584" onPress={doPrint} />
      <View style={{ marginVertical: 10 }} />
      <Button title="MSR" color="#841584" onPress={doMsr} />
      <View style={{ marginVertical: 10 }} />
      <Button title="Scan" color="#841584" onPress={doScan} />
      <View style={{ marginVertical: 10 }} />
      <Button title="RFID" color="#841584" onPress={doRFID} />
      <View style={{ marginVertical: 10 }} />
      <Button title="Read RFID Data" color="#841584" onPress={readRFIDData} />
    </View>
  );
};

export default App;
