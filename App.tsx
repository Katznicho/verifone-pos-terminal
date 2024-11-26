/* eslint-disable prettier/prettier */
import React, { useState } from 'react';
import { View, Text, Button, StyleSheet, Alert } from 'react-native';
import { NativeModules } from 'react-native';

const { NewLandModule } = NativeModules;

export default function App() {
  const [authStatus, setAuthStatus] = useState(''); // Display authentication status
  const [blockData, setBlockData] = useState(''); // Display block data

  // Function to initialize NFC authentication
  const handleNfcAuth = async () => {
    try {
      const result = await NewLandModule.initNfcAuth2();
      setAuthStatus('Authentication Successful');
      setBlockData(result); // Assuming the result contains block data
      Alert.alert('Success', result);
    } catch (error) {
      setAuthStatus('Authentication Failed');
      Alert.alert('Error', error.message || 'Operation failed');
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>NFC Card Reader</Text>

      {/* Button to handle NFC authentication */}
      <Button title="Start NFC Authentication" onPress={handleNfcAuth} />
      <Text style={styles.statusText}>{authStatus}</Text>
      <Text style={styles.dataText}>Block Data: {blockData}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    justifyContent: 'center',
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 20,
  },
  statusText: {
    fontSize: 16,
    color: 'green',
    marginVertical: 10,
    textAlign: 'center',
  },
  dataText: {
    fontSize: 16,
    color: 'blue',
    marginVertical: 10,
    textAlign: 'center',
  },
});
