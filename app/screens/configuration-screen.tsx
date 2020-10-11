import {View} from "react-native";
import * as React from "react";
import {Navigation} from "../App";
import {Header} from "../util/header";

export const ConfigurationScreen = ({navigation}: { navigation: Navigation }) => {

    return (
        <View style={{flex: 1}}>
            <Header
                navigation={navigation}
                title="Configuration"
            />
        </View>
    );
}
