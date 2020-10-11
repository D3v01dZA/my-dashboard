import {useContext, useEffect} from "react";
import {View} from "react-native";
import * as React from "react";
import {AppContext} from "../util/app-context";
import {toastError} from "../util/errors";

export const LogoutScreen = () => {

    const {url, setAuthenticated} = useContext(AppContext);

    const logout = () => {
        fetch(
            `${url}/logout`
        )
            .then(response => {
                if (response.status === 204) {
                    setAuthenticated(false);
                } else {
                    return Promise.reject(`Logout failed with ${response.status}`);
                }
            })
            .catch(toastError)
    }

    useEffect(() => logout());

    return (<View/>);
}
