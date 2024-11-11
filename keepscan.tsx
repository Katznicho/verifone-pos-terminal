/* eslint-disable prettier/prettier */
import React, { useEffect, useState } from 'react';
import { StyleSheet, Text, TouchableOpacity, View, Dimensions, ActivityIndicator } from 'react-native';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { Camera, useCameraDevice, useCameraPermission, useCodeScanner } from 'react-native-vision-camera';

const { width, height } = Dimensions.get('window');

function App() {
    const [cardNumber, setCardNumber] = useState('83270000001901SU');
    const [showCamera, setShowCamera] = useState(false);
    const device = useCameraDevice('back');
    const { hasPermission, requestPermission } = useCameraPermission();
    const [hasScannedCode, setHasScannedCode] = useState(false);
    const [loading, setLoading] = useState(false);

    const [errors, setErrors] = useState({
        cardNumber: '',
    });

    useEffect(() => {
        const requestCameraPermission = async () => {
            if (!hasPermission) {
                await requestPermission();
            }
        };
        requestCameraPermission();
        setHasScannedCode(false);
        setShowCamera(false);
    }, [hasPermission, requestPermission]);

    const codeScanner = useCodeScanner({
        codeTypes: ['qr', 'ean-13'],
        onCodeScanned: codes => {
            if (codes.length > 0) {
                setHasScannedCode(true);
                setShowCamera(false);
                setLoading(true);
                try {
                    console.log(codes[0]?.value);
                } catch (error) {
                    console.log("Error scanning code:", error);
                } finally {
                    setLoading(false);
                }
            } else {
                console.log("Failed to scan code");
            }
        },
    });

    if (showCamera) {
        if (!hasPermission) {
            return (
                <View style={styles.container}>
                    <Text>Camera permission required to use scanner</Text>
                </View>
            );
        }

        if (!device) {
            return (
                <View style={styles.container}>
                    <Text>No camera found on your device</Text>
                </View>
            );
        }

        return (
            <View style={{ flex: 1 }}>
                <Camera
                    style={StyleSheet.absoluteFill}
                    device={device}
                    isActive={true}
                    codeScanner={codeScanner}
                />
                <View style={styles.overlay}>
                    <View style={styles.unfocusedContainer}>
                        <View style={styles.unfocusedArea} />
                        <View style={{ flexDirection: 'row' }}>
                            <View style={styles.unfocusedArea} />
                            <View style={styles.focusedArea} />
                            <View style={styles.unfocusedArea} />
                        </View>
                        <View style={styles.unfocusedArea} />
                    </View>
                </View>
            </View>
        );
    }

    return (
        <View style={styles.container}>
            <View style={styles.profileContainer} />

            <View style={{ justifyContent: "center", alignItems: "center" }}>
                <Text style={styles.scanButtonText}>Scan Card</Text>
            </View>

            <TouchableOpacity
                style={[styles.scanButton, { justifyContent: "center", alignItems: "center" }]}
                onPress={() => setShowCamera(true)}
            >
                <MaterialCommunityIcons name="qrcode-scan" size={80} color={"red"} />
            </TouchableOpacity>

            {loading && (
                <View style={styles.loading}>
                    <ActivityIndicator size="large" color="#0000ff" />
                    <Text>Loading...</Text>
                </View>
            )}
        </View>
    );
}

export default App;

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#fff',
        paddingHorizontal: 16,
    },
    profileContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 20,
        marginTop: 20,
    },
    scanButton: {
        borderWidth: 1,
        borderColor: '#ccc',
        borderRadius: 8,
        marginVertical: 10,
        width: "100%",
        height: 100,
    },
    scanButtonText: {
        fontSize: 16,
        color: "black",
        fontWeight: "bold",
    },
    overlay: {
        ...StyleSheet.absoluteFillObject,
        alignItems: 'center',
        justifyContent: 'center',
    },
    unfocusedContainer: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
    unfocusedArea: {
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        width: width,
        flex: 1,
    },
    focusedArea: {
        width: 300,
        height: 300,
        borderWidth: 2,
        borderColor: 'white',
    },
    loading: {
        position: "absolute",
        top: "50%",
        left: "50%",
        transform: [{ translateX: -50 }, { translateY: -50 }],
        alignItems: "center",
    },
});
