import React, {useEffect, useState} from 'react';
import {View, Text, TouchableOpacity, StyleSheet, Alert} from 'react-native';
import NfcManager, {NfcTech, NfcEvents, Ndef} from 'react-native-nfc-manager';

NfcManager.start();

function App() {
  const [isReading, setIsReading] = useState(false);

  const readNfcCard = async () => {
    if (isReading) {
      console.log('Another NFC request is in progress');
      return;
    }

    setIsReading(true);

    try {
      // Start NFC event listener to detect tags
      await NfcManager.registerTagEvent();
      console.log('NFC Tag Event Listener Registered');
    } catch (ex) {
      console.warn('Error reading NFC card:', ex);
      Alert.alert(
        'Error',
        `Could not read NFC card. Details: ${ex.message || ex}`,
      );
    }
  };

  const handleTagDiscovered = async tag => {
    console.log('Tag discovered:', tag);

    // If NDEF data is available, decode and display it
    if (tag.ndefMessage) {
      const ndefData = Ndef.text.decodePayload(tag.ndefMessage[0].payload);
      console.log('NDEF Data:', ndefData);
      Alert.alert('NDEF Data', ndefData);
    }
    // If the tag supports MIFARE Classic, handle it accordingly
    else if (tag.techTypes.includes(NfcTech.MifareClassic)) {
      console.log('Attempting MIFARE Classic read');
      await handleMifareClassicCard();
    } else {
      console.log('Unsupported NFC card type detected');
      Alert.alert('NFC Card Type', 'This card type is not supported.');
    }

    // Cancel the event to prepare for the next scan
    await NfcManager.unregisterTagEvent();
    setIsReading(false);
  };

  const handleMifareClassicCard = async () => {
    try {
      console.log('Requesting MifareClassic technology...');
      await NfcManager.requestTechnology(NfcTech.MifareClassic);

      // Authenticate and read from block 4
      await NfcManager.mifareClassicHandlerAndroid.mifareClassicAuthenticateA(
        4,
        [0xff, 0xff, 0xff, 0xff, 0xff, 0xff],
      );
      console.log('Authentication successful for block 4');

      // Read data from block 4
      const blockData =
        await NfcManager.mifareClassicHandlerAndroid.mifareClassicReadBlock(4);
      const cardNumber = Buffer.from(blockData).toString('hex');
      console.log('Card Number (Hex):', cardNumber);
      Alert.alert('Card Number', `Block 4: ${cardNumber}`);
    } catch (ex) {
      console.warn('Error handling MIFARE Classic card:', ex);
      Alert.alert(
        'Error',
        `Could not read MIFARE 1K card. Details: ${ex.message || ex}`,
      );
    } finally {
      NfcManager.cancelTechnologyRequest();
    }
  };

  useEffect(() => {
    NfcManager.setEventListener(NfcEvents.DiscoverTag, handleTagDiscovered);

    return () => {
      NfcManager.setEventListener(NfcEvents.DiscoverTag, null);
    };
  }, []);

  return (
    <View style={styles.wrapper}>
      <TouchableOpacity onPress={readNfcCard}>
        <Text>Scan NFC Card</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
});

export default App;
