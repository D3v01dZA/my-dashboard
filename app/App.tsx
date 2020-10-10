import "react-native-gesture-handler";
import * as React from "react";
import {createDrawerNavigator, DrawerNavigationProp} from "@react-navigation/drawer";
import {NavigationContainer} from "@react-navigation/native";
import {useState} from "react";
import {LoginScreen} from "./screens/login-screen";
import {HomeScreen} from "./screens/home-screen";
import {ConfigurationScreen} from "./screens/configuration-screen";
import {LogoutScreen} from "./screens/logout-screen";
import {Credentials, Context} from "./util/context";

export type Routes = {
    Login: undefined,
    Logout: undefined,

    Home: undefined,
    Configuration: undefined,
    TimeList: {
        page: number
    }
}

export type Navigation = DrawerNavigationProp<Routes>;

const Drawer = createDrawerNavigator<Routes>();

const App = () => {

    const [credentials, setCredentials] = useState<Credentials | undefined>(undefined);
    const clearCredentials = () => setCredentials(undefined);

    return (
        <Context.Provider
            value={{
                url: "http://10.0.2.2:8080",
                credentials,
                setCredentials,
                clearCredentials
            }}
        >
            <NavigationContainer>
                {
                    !credentials ? (
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
        </Context.Provider>
    );
}

export default App;