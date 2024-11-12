import React from 'react';
import { Button, Alert, View } from 'react-native';
import { NativeModules } from 'react-native';

const { NewLandModule, QRCodeScannerModule } = NativeModules;

const App = () => {
  // Define the button press handler for QR code scanning
  const handleQrScanPress = () => {
    QRCodeScannerModule.startScanner((result) => {
      console.log("QR Code Scan Result:", result);
      Alert.alert("QR Code Scan", result);
    });
  };

  // Define the function for printing the receipt
  const handlePrintReceiptPress = () => {
    NewLandModule.initializeTRAPrinter();  // No callback needed here
  };

  return (
    <View style={{ padding: 20 }}>
      <Button title="Start QR Code Scan Android" onPress={handleQrScanPress} />
      <Button title="Print Receipt" onPress={handlePrintReceiptPress} />
    </View>
  );
};

export default App;
