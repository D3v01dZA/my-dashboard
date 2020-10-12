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
import {ProjectsScreen} from "./screens/projects-screen";
import {ProjectScreen} from "./screens/project-screen";
import {TimesScreen} from "./screens/times-screen";
import {TimeScreen} from "./screens/time-screen";
import {SynchronizationsScreen} from "./screens/synchronizations-screen";
import {SynchronizationScreen} from "./screens/synchronization-screen";
import DeviceInfo from 'react-native-device-info';
import {View} from "react-native";
import AsyncStorage from '@react-native-community/async-storage';
import {toastError} from "./util/errors";

export type Routes = {
    Login: undefined,
    Logout: undefined,

    Home: {
        projectId: number | undefined
    },
    Projects: undefined,
    Project: {
        projectId: number | undefined
    },
    Times: {
        projectId: number | undefined
    },
    Time: {
        projectId: number | undefined,
        timeId: number | undefined
    },
    Synchronizations: {
        projectId: number | undefined
    },
    Synchronization: {
        projectId: number | undefined,
        synchronizationId: number | undefined
    },
    Configuration: undefined
}

export type Navigation = DrawerNavigationProp<Routes>;
export type Route<Route extends keyof Routes> = RouteProp<Routes, Route>;

const Drawer = createDrawerNavigator<Routes>();

const App = () => {

    const [savedProject, setSavedProject] = useState<{ id: number | undefined }>();

    const [authenticated, setAuthenticated] = useState<boolean>(false);

    useEffect(() => {
        console.log("Fetching");
        AsyncStorage.getItem("savedProject", () => {})
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
    }, [])

    return (
        savedProject === undefined ? <View/> : (
            <AppContext.Provider
                value={{
                    url: DeviceInfo.isEmulatorSync() ? "http://10.0.2.2:8080" : "https://caltona.net/dashboard",
                    authenticated,
                    setAuthenticated
                }}
            >
                <RootSiblingParent>
                    <NavigationContainer>
                        {
                            !authenticated ? (
                                <Drawer.Navigator initialRouteName="Login" screenOptions={{gestureEnabled: false}}>
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
                                        initialParams={{projectId: savedProject.id}}
                                    />
                                    <Drawer.Screen
                                        name="Projects"
                                        component={ProjectsScreen}
                                    />
                                    <Drawer.Screen
                                        name="Project"
                                        component={ProjectScreen}
                                        initialParams={{projectId: savedProject.id}}
                                    />
                                    <Drawer.Screen
                                        name="Times"
                                        component={TimesScreen}
                                        initialParams={{projectId: savedProject.id}}
                                    />
                                    <Drawer.Screen
                                        name="Time"
                                        component={TimeScreen}
                                        initialParams={{projectId: savedProject.id, timeId: undefined}}
                                    />
                                    <Drawer.Screen
                                        name="Synchronizations"
                                        component={SynchronizationsScreen}
                                        initialParams={{projectId: savedProject.id}}
                                    />
                                    <Drawer.Screen
                                        name="Synchronization"
                                        component={SynchronizationScreen}
                                        initialParams={{projectId: savedProject.id, synchronizationId: undefined}}
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
