import {Button, View} from "react-native";
import * as React from "react";
import {Navigation} from "../App";

export const ConfigurationScreen = ({navigation}: { navigation: Navigation }) => {

    return (
        <View style={{flex: 1, alignItems: "center", justifyContent: "center"}}>
            <Button onPress={() => navigation.goBack()} title="Go back home"/>
        </View>
    );
}