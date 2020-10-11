import {View} from "react-native";
import * as React from "react";
import {useContext, useState} from "react";
import {Button, Header, Input} from "react-native-elements";
import {AppContext} from "../util/app-context";
import base64 from "react-native-base64";
import {toastError} from "../util/errors";

export const LoginScreen = () => {

    const {url, setAuthenticated} = useContext(AppContext);

    const [username, setUsername] = useState<string>("test");

    const [password, setPassword] = useState<string>("password");

    const login = () => {
        const encoded = base64.encode(`${username}:${password}`);
        fetch(
            `${url}`,
            {
                headers: {
                    authorization: `Basic ${encoded}`
                }
            }
        )
            .then(response => {
                if (response.status === 200) {
                    return response.text();
                }
                return Promise.reject(`Login failed with ${response.status}`)
            })
            .then(response => {
                if (response.endsWith(`${username}!`)) {
                    setAuthenticated(true);
                } else {
                    return Promise.reject(`Login failed with ${response}`);
                }
            })
            .catch(toastError)
    }

    return (
        <View style={{flex: 1}}>
            <Header
                centerComponent={{text: "Login", style: {color: "#fff", fontSize: 18}}}
            />
            <View style={{flex: 1, alignItems: "center", justifyContent: "center"}}>
                <Input
                    label="Username"
                    placeholder="Username"
                    textContentType="username"
                    value={username}
                    onChangeText={setUsername}
                />
                <Input
                    label="Password"
                    placeholder="Password"
                    secureTextEntry
                    textContentType="password"
                    value={password}
                    onChangeText={setPassword}
                />
                <Button
                    onPress={login}
                    title="Login"
                />
            </View>
        </View>
    );
}
