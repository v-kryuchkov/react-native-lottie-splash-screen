import { useEffect } from 'react';
import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View } from 'react-native';
import LottieSplashScreen from '@attarchi/react-native-lottie-splash-screen';

export default function App() {
  useEffect(() => {
    // Hide Lottie splash screen immediately; works when setAnimationFinished is true
    LottieSplashScreen?.hide();
  }, []);
  
  return (
    <View style={styles.container}>
      <Text>Open up App.tsx to start working on your app!</Text>
      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
