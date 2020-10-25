import * as React from "react";
import {useCallback, useContext, useState} from "react";
import {AppContext} from "../util/app-context";
import {Alert, Dimensions, ScrollView, TouchableOpacity, View} from "react-native";
import {Header} from "../util/header";
import {createDelete, createPut, format, Time, TimeType} from "../util/model";
import {useFocusEffect} from "@react-navigation/native";
import {toastError} from "../util/errors";
import {Button, Input, ListItem, Overlay, Text} from "react-native-elements";
import moment from "moment";
import {selectProject, selectTime} from "../util/nav";
import DatePicker from "react-native-date-picker";
import {ProjectsNavigation, ProjectsRoute} from "./projects-navigator";

export const TimeScreen = ({navigation, route}: { navigation: ProjectsNavigation, route: ProjectsRoute<"Time"> }) => {

    const {url} = useContext(AppContext);

    const [time, setTime] = useState<Time>();

    const [editing, setEditing] = useState<{ editing: boolean, time?: Time }>({editing: false});

    const fetchTime = () => {
        fetch(`${url}/time/project/${route.params.projectId}/time/${route.params.timeId}`)
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else if (response.status === 404) {
                    return undefined;
                } else {
                    return Promise.reject(`Failed to fetch time with ${response.status}`);
                }
            })
            .then(time => {
                setTime(time);
            })
            .catch(reason => {
                toastError(reason);
            })
    }

    const saveTime = () => {
        fetch(
            `${url}/time/project/${route.params.projectId}/time/${route.params.timeId}`,
            createPut(editing.time)
        )
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else {
                    return Promise.reject(`Failed to update synchronization with ${response.status}`);
                }
            })
            .then(time => {
                setEditing({...editing, editing: false})
                setTime(time);
            })
            .catch(reason => {
                toastError(reason);
            })
    }

    const deleteTimePrompt = () => {
        Alert.alert(
            "Delete?",
            `Are you sure you want to delete ${time?.id}?`,
            [
                {
                    text: "Yes",
                    style: "destructive",
                    onPress: () => deleteTime()
                },
                {
                    text: "No",
                    style: "cancel"
                }
            ]
        )
    }


    const deleteTime = () => {
        fetch(
            `${url}/time/project/${route.params.projectId}/time/${route.params.timeId}`,
            createDelete()
        )
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else {
                    return Promise.reject(`Failed to delete time with ${response.status}`);
                }
            })
            .then(() => {
                navigation.navigate("Times", {projectId: route.params.projectId});
            })
            .catch(reason => {
                toastError(reason);
            })
    }

    useFocusEffect(
        useCallback(() => {
            if (route.params.projectId === undefined) {
                selectProject(navigation);
            } else if (route.params.timeId === undefined) {
                selectTime(navigation, route.params.projectId);
            } else {
                fetchTime();
            }
        }, [url, route.params.projectId, route.params.timeId])
    );

    const editingRender = (editingTime: Time) => {
        return <ScrollView>
            <View>
                {
                    editingTime.start === undefined ? undefined : (
                        <View>
                            <Text
                                style={{
                                    fontSize: 16,
                                    fontWeight: "bold",
                                    color: "grey",
                                    padding: 10
                                }}
                            >
                                End Time
                            </Text>
                            <DatePicker
                                date={moment(editingTime.start).toDate()}
                                mode="datetime"
                                is24hourSource="device"
                                onDateChange={date => setEditing({...editing, time: {...editingTime, start: moment(date).format("yyyy-MM-DDTHH:mm:ss.SSS")}})}
                            />
                        </View>
                    )
                }
                {
                    editingTime.end === undefined ? undefined : (
                        <View>
                            <Text
                                style={{
                                    fontSize: 16,
                                    fontWeight: "bold",
                                    color: "grey",
                                    padding: 10
                                }}
                            >
                                End Time
                            </Text>
                            <DatePicker
                                date={moment(editingTime.end).toDate()}
                                mode="datetime"
                                onDateChange={date => setEditing({...editing, time: {...editingTime, end: moment(date).format("yyyy-MM-DDTHH:mm:ss.SSS")}})}
                            />
                        </View>
                    )
                }
            </View>
            <View style={{flexDirection: "row"}}>
                <Button
                    title="Save"
                    icon={{
                        type: "font-awesome-5",
                        name: "save",
                        color: "white"
                    }}
                    containerStyle={{flexGrow: 1}}
                    onPress={() => saveTime()}
                />
                <Button
                    title="Cancel"
                    buttonStyle={{
                        backgroundColor: "red"
                    }}
                    icon={{
                        type: "font-awesome-5",
                        name: "arrow-left",
                        color: "white"
                    }}
                    containerStyle={{flexGrow: 1}}
                    onPress={() => setEditing({...editing, editing: false})}
                />
            </View>
        </ScrollView>
    }

    return (
        <View style={{flex: 1}}>
            <Header navigation={navigation.dangerouslyGetParent()} title="Time"/>
            <ScrollView>
                {
                    time?.id === undefined ? undefined : (
                        <ListItem>
                            <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                                <Text>ID:</Text>
                                <Text>{time.id}</Text>
                            </ListItem.Content>
                        </ListItem>
                    )
                }
                {
                    time?.type === undefined ? undefined : (
                        <ListItem>
                            <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                                <Text>Type:</Text>
                                <Text>{time.type === TimeType.BREAK ? "Break" : "Work"}</Text>
                            </ListItem.Content>
                        </ListItem>
                    )
                }
                {
                    time?.start === undefined ? undefined : (
                        <ListItem>
                            <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                                <Text>Start Time:</Text>
                                <Text>{format(moment(time.start))}</Text>
                            </ListItem.Content>
                        </ListItem>
                    )
                }
                {
                    time?.end === undefined ? undefined : (
                        <ListItem>
                            <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                                <Text>End Time:</Text>
                                <Text>{format(moment(time.end))}</Text>
                            </ListItem.Content>
                        </ListItem>
                    )
                }
            </ScrollView>
            <View style={{flexDirection: "row", justifyContent: "space-evenly"}}>
                <Button
                    title="Edit"
                    icon={{
                        type: "font-awesome-5",
                        name: "edit",
                        color: "white"
                    }}
                    containerStyle={{flexGrow: 1}}
                    onPress={() => setEditing({editing: true, time})}
                />
                <Button
                    title="Delete"
                    icon={{
                        type: "font-awesome-5",
                        name: "times",
                        color: "white"
                    }}
                    buttonStyle={{backgroundColor: "red"}}
                    containerStyle={{flexGrow: 1}}
                    onPress={deleteTimePrompt}
                />
            </View>

            <Overlay
                isVisible={editing.editing}
                onBackdropPress={() => setEditing({...editing, editing: false})}
            >
                <View style={{
                    width: Dimensions.get('window').width * 0.85,
                    maxHeight: Dimensions.get('window').height * 0.85
                }}>
                    {
                        editing.time === undefined ? undefined : editingRender(editing.time)
                    }
                </View>
            </Overlay>
        </View>
    )

}
