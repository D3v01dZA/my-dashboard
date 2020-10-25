import React from "react";
import {createStackNavigator, StackNavigationProp} from "@react-navigation/stack";
import {ProjectScreen} from "./project-screen";
import {TimesScreen} from "./times-screen";
import {TimeScreen} from "./time-screen";
import {SynchronizationsScreen} from "./synchronizations-screen";
import {SynchronizationScreen} from "./synchronization-screen";
import {ProjectsScreen} from "./projects-screen";
import {RouteProp} from "@react-navigation/core";
import {AttemptsScreen} from "./attempts-screen";

export type ProjectsRoutes = {
    Projects: undefined,
    Project: {
        projectId: number
    },
    Times: {
        projectId: number
    },
    Time: {
        projectId: number,
        timeId: number
    },
    Synchronizations: {
        projectId: number
    },
    Synchronization: {
        projectId: number,
        synchronizationId: number
    },
    Attempts: {
        projectId: number,
        synchronizationId: number
    }
}

export type ProjectsRoute<Route extends keyof ProjectsRoutes> = RouteProp<ProjectsRoutes, Route>;
export type ProjectsNavigation = StackNavigationProp<ProjectsRoutes>;

const Stack = createStackNavigator<ProjectsRoutes>();

export const ProjectsNavigator = () => {

    return (
        <Stack.Navigator screenOptions={{headerShown: false}}>
            <Stack.Screen
                name="Projects"
                component={ProjectsScreen}
            />
            <Stack.Screen
                name="Project"
                component={ProjectScreen}
            />
            <Stack.Screen
                name="Times"
                component={TimesScreen}
            />
            <Stack.Screen
                name="Time"
                component={TimeScreen}
            />
            <Stack.Screen
                name="Synchronizations"
                component={SynchronizationsScreen}
            />
            <Stack.Screen
                name="Synchronization"
                component={SynchronizationScreen}
            />
            <Stack.Screen
                name="Attempts"
                component={AttemptsScreen}
            />
        </Stack.Navigator>
    );

}
