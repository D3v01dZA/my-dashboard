import Toast from "react-native-root-toast";
import {ProjectsNavigation} from "../screens/projects-navigator";

export const selectProject = (navigation: ProjectsNavigation) => {
    Toast.show("Select a Project");
    navigation.navigate("Projects");
}

export const selectTime = (navigation: ProjectsNavigation, projectId: number) => {
    Toast.show("Select a Time");
    navigation.navigate("Times", {projectId});
}

export const selectSynchronization = (navigation: ProjectsNavigation, projectId: number) => {
    Toast.show("Select a Synchronization");
    navigation.navigate("Times", {projectId});
}
