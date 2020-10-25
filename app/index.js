import {AppRegistry, Platform} from 'react-native';
import App from './App';
import {name as appName} from './app.json';
import PushNotification from "react-native-push-notification";
import {Options} from "./util/notification";

AppRegistry.registerComponent(appName, () => App);

if (Platform.OS === 'web') {
    AppRegistry.runApplication(appName, {
        rootTag: document.getElementById('root'),
    });
}

PushNotification.configure(Options);
