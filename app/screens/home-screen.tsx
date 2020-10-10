import {Button, View} from "react-native";
import * as React from "react";
import {Navigation} from "../App";
import {Header} from "react-native-elements";

export const HomeScreen = ({navigation}: { navigation: Navigation }) => {

    return (
        <View style={{flex: 1}}>
            <Header
                leftComponent={{icon: "menu", color: "#fff", onPress: () => navigation.openDrawer()}}
                centerComponent={{text: "Time", style: {color: "#fff"}}}
            />
            <View style={{flex: 1, alignItems: "center", justifyContent: "center"}}>
                <Button
                    onPress={() => navigation.navigate("Configuration")}
                    title={"Go to notifications"}
                />
            </View>
        </View>
    );
}