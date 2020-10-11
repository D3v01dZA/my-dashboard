import {View} from "react-native";
import * as React from "react";
import {useCallback, useContext, useState} from "react";
import {Navigation, Route} from "../App";
import {Button, Text} from "react-native-elements";
import {useFocusEffect} from '@react-navigation/native';
import {Header} from "../util/header";
import {createPost, Project, TimeStatus, TimeStatusType} from "../util/model";
import {toastError} from "../util/errors";
import {AppContext} from "../util/app-context";
import {fetchProject} from "../util/web-calls";
import {selectProject} from "../util/nav";
import Toast from 'react-native-root-toast';

export const HomeScreen = ({navigation, route}: { navigation: Navigation, route: Route<"Home"> }) => {

    const {url} = useContext(AppContext);

    const [project, setProject] = useState<Project>();
    const [timeStatus, setTimeStatus] = useState<TimeStatus>({status: TimeStatusType.NONE});

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
        fetch(
            `${url}/time/project/${route.params.projectId}/${type}`,
            createPost(undefined)
        )
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else {
                    return Promise.reject(`Failed to ${type} with ${response.status}`);
                }
            })
            .then(() => {
                fetchTime();
            })
            .catch(reason => {
                toastError(reason);
            })
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

    return (
        <View style={{flex: 1}}>
            <Header
                navigation={navigation}
                title="Home"
            />
            <View style={{flex: 1, justifyContent: "center"}}>
                <View style={{justifyContent: "center", flexDirection: "row"}}>
                    <Text style={{fontSize: 30, fontWeight: "bold"}}>{project?.name}</Text>
                </View>
                <View style={{justifyContent: "space-around", flexDirection: "row"}}>
                    <Text>Worked</Text>
                    <Text>Break</Text>
                </View>
                <View style={{justifyContent: "space-around", flexDirection: "row"}}>
                    <Button
                        title={isNone() || isBreak() ? "Start" : "Pause"}
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
    );
}
