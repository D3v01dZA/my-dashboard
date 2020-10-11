import * as React from "react";
import {Header as ReactNativeHeader} from "react-native-elements";
import {Navigation} from "../App";

export type HeaderProps = {
    navigation: Navigation,
    title: string
}

export const Header = ({navigation, title}: HeaderProps) => {

    return (
        <ReactNativeHeader
            leftComponent={{icon: "menu", color: "#fff", onPress: () => navigation.openDrawer()}}
            centerComponent={{text: title, style: {color: "#fff", fontSize: 18}}}
        />
    )

}
