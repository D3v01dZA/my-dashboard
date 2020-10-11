import {Navigation} from "../App";
import Toast from "react-native-root-toast";

export const selectProject = (navigation: Navigation) => {
    Toast.show("Select a Project");
    navigation.navigate("Projects");
}

export const selectTime = (navigation: Navigation, projectId: number) => {
    Toast.show("Select a Time");
    navigation.navigate("Times", {projectId});
}

export const selectSynchronization = (navigation: Navigation, projectId: number) => {
    Toast.show("Select a Synchronization");
    navigation.navigate("Times", {projectId});
}
