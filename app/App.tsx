import "react-native-gesture-handler";
import * as React from "react";
import {useEffect, useState} from "react";
import {createDrawerNavigator, DrawerNavigationProp} from "@react-navigation/drawer";
import {RouteProp} from "@react-navigation/core";
import {NavigationContainer} from "@react-navigation/native";
import {RootSiblingParent} from "react-native-root-siblings";
import {LoginScreen} from "./screens/login-screen";
import {HomeScreen} from "./screens/home-screen";
import {ConfigurationScreen} from "./screens/configuration-screen";
import {LogoutScreen} from "./screens/logout-screen";
import {AppContext} from "./util/app-context";
import DeviceInfo from 'react-native-device-info';
import {View} from "react-native";
import AsyncStorage from '@react-native-community/async-storage';
import {toastError} from "./util/errors";
import {createChannels} from "./util/notification";
import {ProjectsNavigator} from "./screens/projects-navigator";

export type Routes = {
    Login: undefined,
    Logout: undefined,

    Home: {
        projectId: number | undefined
    },
    Projects: undefined,
    Configuration: undefined
}

export type Navigation = DrawerNavigationProp<Routes>;
export type Route<Route extends keyof Routes> = RouteProp<Routes, Route>;

const Drawer = createDrawerNavigator<Routes>();

const App = () => {

    const [url, setUrl] = useState<string>();

    const [authenticated, setAuthenticated] = useState<boolean>(false);

    const [savedProject, setSavedProject] = useState<{ id: number | undefined }>();

    useEffect(() => {
        AsyncStorage.getItem("savedProject", () => {
        })
            .then(result => {
                if (result === undefined || result === null) {
                    setSavedProject({id: undefined});
                } else {
                    setSavedProject({id: parseInt(result)});
                }
            })
            .catch(reason => {
                toastError(reason)
            })
    }, []);

    useEffect(() => {
        DeviceInfo.isEmulator()
            .then(emulator => {
                if (emulator) {
                    setUrl("http://10.0.2.2:8080");
                } else {
                    setUrl("https://time.caltona.net");
                }
            })
            .catch(reason => {
                toastError(reason);
            });
    }, []);

    useEffect(() => {
        createChannels();
    }, []);

    return (
        savedProject === undefined || url === undefined ? <View/> : (
            <AppContext.Provider
                value={{
                    url,
                    authenticated,
                    setAuthenticated
                }}
            >
                <RootSiblingParent>
                    <NavigationContainer>
                        {
                            !authenticated ? (
                                <Drawer.Navigator initialRouteName="Login"
                                                  screenOptions={{gestureEnabled: false}}>
                                    <Drawer.Screen
                                        name="Login"
                                        component={LoginScreen}
                                    />
                                </Drawer.Navigator>
                            ) : (
                                <Drawer.Navigator initialRouteName="Home">
                                    <Drawer.Screen
                                        name="Home"
                                        component={HomeScreen}
                                        initialParams={{projectId: savedProject?.id}}
                                    />
                                    <Drawer.Screen
                                        name="Projects"
                                        component={ProjectsNavigator}
                                    />
                                    <Drawer.Screen
                                        name="Configuration"
                                        component={ConfigurationScreen}
                                    />
                                    <Drawer.Screen
                                        name="Logout"
                                        component={LogoutScreen}
                                    />
                                </Drawer.Navigator>
                            )
                        }
                    </NavigationContainer>
                </RootSiblingParent>
            </AppContext.Provider>
        )
    );
}

export default App;
