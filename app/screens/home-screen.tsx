import {Dimensions, View} from "react-native";
import * as React from "react";
import {useCallback, useContext, useEffect, useState} from "react";
import {Navigation, Route} from "../App";
import {Button, Text} from "react-native-elements";
import {useFocusEffect} from '@react-navigation/native';
import {Header} from "../util/header";
import {createPost, Project, TimeStatus, TimeStatusType} from "../util/model";
import {toastError} from "../util/errors";
import {AppContext} from "../util/app-context";
import {fetchProject, timeAction} from "../util/web-calls";
import Toast from 'react-native-root-toast';

export const HomeScreen = ({navigation, route}: { navigation: Navigation, route: Route<"Home"> }) => {

    const {url} = useContext(AppContext);

    const [project, setProject] = useState<Project>();
    const [timeStatus, setTimeStatus] = useState<TimeStatus>({status: TimeStatusType.NONE});

    const selectProject = (navigation: Navigation) => {
        Toast.show("Select a Project");
        navigation.navigate("Projects");
    }

    const fetchTime = () => {
        fetch(
            `${url}/time/project/${route.params.projectId}/time-status`,
            createPost(undefined)
        )
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else if (response.status === 404) {
                    return Promise.reject("Time is running for another project");
                } else {
                    return Promise.reject(`Failed to fetch time status with ${response.status}`);
                }
            })
            .then(response => {
                setTimeStatus(response as TimeStatus);
            })
            .catch(reason => {
                toastError(reason);
            })
    }

    const modifyTime = (type: string) => {
        timeAction(url, route.params.projectId as number, type, fetchTime);
    }

    const syncTime = () => {
        fetch(
            `${url}/time/project/${project?.id}/synchronize`,
            createPost(undefined)
        )
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else {
                    return Promise.reject(`Failed to start synchronization with ${response.status}`);
                }
            })
            .then(() => {
                Toast.show("Synchronization started in the background");
            })
            .catch(reason => {
                toastError(reason);
            })
    }

    const isNone = () => timeStatus.status === TimeStatusType.NONE;
    const isWork = () => timeStatus.status === TimeStatusType.WORK;
    const isBreak = () => timeStatus.status === TimeStatusType.BREAK;

    useFocusEffect(
        useCallback(() => {
            if (route.params.projectId === undefined) {
                selectProject(navigation);
            } else {
                fetchProject(url, route.params.projectId, retrieved => {
                    setProject(retrieved);
                    if (retrieved === undefined) {
                        selectProject(navigation);
                    }
                });
                fetchTime();
            }
        }, [url, route.params.projectId])
    );

    useEffect(() => {
        const interval = setInterval(() => {
            if (route.params.projectId !== undefined) {
                fetchTime();
            }
        }, 60000);
        return () => clearInterval(interval);
    }, []);

    return (
        <View style={{flex: 1}}>
            <Header
                navigation={navigation}
                title="Home"
            />
            <View style={{flex: 1, justifyContent: "center", alignItems: "center"}}>
                <View style={{justifyContent: "center", flexDirection: "row"}}>
                    <Text style={{fontSize: 30, fontWeight: "bold"}}>{project?.name}</Text>
                </View>
                <View style={{justifyContent: "center", width: Dimensions.get('window').width * 0.85}}>
                    <View style={{justifyContent: "space-around", flexDirection: "row", padding: 30}}>
                        <Text>Work: {timeStatus.runningWorkTotal?.substr(0, 5) ?? "00:00"}</Text>
                        <Text>Break: {timeStatus.runningBreakTotal?.substr(0, 5) ?? "00:00"}</Text>
                    </View>
                    <View style={{justifyContent: "space-around", flexDirection: "row"}}>
                        <Button
                            title={isNone() ? "Start" : isBreak() ? "Resume" : "Pause"}
                            containerStyle={{flexGrow: 1}}
                            icon={{
                                type: "font-awesome-5",
                                name: `${isNone() || isBreak() ? "play" : "pause"}`,
                                color: "white"
                            }}
                            onPress={() => {
                                if (isNone()) {
                                    modifyTime("start-work");
                                } else if (isBreak()) {
                                    modifyTime("stop-break");
                                } else {
                                    modifyTime("start-break");
                                }
                            }}
                        />
                        <Button
                            title={isWork() || isBreak() ? "Stop" : "Sync"}
                            containerStyle={{flexGrow: 1}}
                            icon={{
                                type: "font-awesome-5",
                                name: `${isWork() || isBreak() ? "stop" : "sync"}`,
                                color: "white"
                            }}
                            buttonStyle={{backgroundColor: `${isWork() || isBreak() ? "red" : "green"}`}}
                            onPress={() => {
                                if (isWork() || isBreak()) {
                                    modifyTime("stop-work");
                                } else {
                                    syncTime();
                                }
                            }}
                        />
                    </View>
                </View>
            </View>
        </View>
    );
}
