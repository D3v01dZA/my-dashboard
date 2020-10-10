import {useContext, useEffect} from "react";
import {View} from "react-native";
import * as React from "react";
import {Context} from "../util/context";

export const LogoutScreen = () => {

    let {clearCredentials} = useContext(Context);

    useEffect(() => clearCredentials());

    return (<View/>);
}