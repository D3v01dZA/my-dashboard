import {useCallback, useContext, useState} from "react";
import {AppContext} from "../util/app-context";
import {Navigation, Route} from "../App";
import {Alert, Dimensions, ScrollView, View} from "react-native";
import * as React from "react";
import {Header} from "../util/header";
import {createDelete, createPut, format, Project, Synchronization, SynchronizationType, TimeType} from "../util/model";
import {useFocusEffect} from "@react-navigation/native";
import {Button, Input, ListItem, Overlay, Text} from "react-native-elements";
import {toastError} from "../util/errors";
import {selectProject, selectTime} from "../util/nav";
import {SynchronizationDisplay} from "./synchronization-display";
import {SynchronizationEdit} from "./synchronization-edit";

export const SynchronizationScreen = ({navigation, route}: { navigation: Navigation, route: Route<"Synchronization"> }) => {

    const {url} = useContext(AppContext);

    const [synchronization, setSynchronization] = useState<Synchronization>();

    const [editing, setEditing] = useState<{ editing: boolean, synchronization?: Synchronization }>({editing: false});

    const deleteSynchronizationPrompt = () => {
        Alert.alert(
            "Delete?",
            `Are you sure you want to delete ${synchronization?.name}?`,
            [
                {
                    text: "Yes",
                    style: "destructive",
                    onPress: () => deleteSynchronization()
                },
                {
                    text: "No",
                    style: "cancel"
                }
            ]
        )
    }

    const fetchSynchronization = () => {
        fetch(`${url}/time/project/${route.params.projectId}/synchronization/${route.params.synchronizationId}`)
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else if (response.status === 404) {
                    return undefined;
                } else {
                    return Promise.reject(`Failed to fetch time with ${response.status}`);
                }
            })
            .then(synchronization => {
                setSynchronization(synchronization);
            })
            .catch(reason => {
                toastError(reason);
            })
    }

    const deleteSynchronization = () => {
        fetch(
            `${url}/time/project/${route.params.projectId}/synchronization/${route.params.synchronizationId}`,
            createDelete()
        )
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else {
                    return Promise.reject(`Failed to delete synchronization with ${response.status}`);
                }
            })
            .then(() => {
                navigation.navigate("Synchronizations", {projectId: route.params.projectId});
            })
            .catch(reason => {
                toastError(reason);
            })
    }

    const saveSynchronization = () => {
        fetch(
            `${url}/time/project/${route.params.projectId}/synchronization/${route.params.synchronizationId}`,
            createPut(editing.synchronization)
        )
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else {
                    return Promise.reject(`Failed to update synchronization with ${response.status}`);
                }
            })
            .then(synchronization => {
                setEditing({...editing, editing: false})
                setSynchronization(synchronization);
            })
            .catch(reason => {
                toastError(reason);
            })
    }

    useFocusEffect(
        useCallback(() => {
            if (route.params.projectId === undefined) {
                selectProject(navigation);
            } else if (route.params.synchronizationId === undefined) {
                selectTime(navigation, route.params.projectId);
            } else {
                fetchSynchronization();
            }
        }, [url, route.params.projectId, route.params.synchronizationId])
    );

    const editingRender = (editingSynchronization: Synchronization) => {
        return <ScrollView>
            <View>
                <View>
                    <Input
                        label="Name"
                        placeholder="Name"
                        value={editingSynchronization.name}
                        onChangeText={name => setEditing({
                            ...editing,
                            synchronization: {
                                ...editingSynchronization,
                                name
                            }
                        })}
                    />
                    <SynchronizationEdit
                        synchronizationType={editingSynchronization.service}
                        setSynchronizationConfiguration={configuration => {
                            setEditing({
                                ...editing,
                                synchronization: {
                                    ...editingSynchronization,
                                    configuration: configuration
                                }
                            })
                        }}
                        synchronizationConfiguration={editingSynchronization.configuration}
                    />
                </View>
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
                    onPress={() => saveSynchronization()}
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
            <Header navigation={navigation} title="Time"/>
            <ScrollView>
                {
                    synchronization?.id === undefined ? undefined : (
                        <ListItem>
                            <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                                <Text>ID:</Text>
                                <Text>{synchronization.id}</Text>
                            </ListItem.Content>
                        </ListItem>
                    )
                }
                {
                    synchronization?.name === undefined ? undefined : (
                        <ListItem>
                            <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                                <Text>Name:</Text>
                                <Text>{synchronization.name}</Text>
                            </ListItem.Content>
                        </ListItem>
                    )
                }
                {
                    synchronization?.service === undefined ? undefined : (
                        <ListItem>
                            <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                                <Text>Type:</Text>
                                <Text>{synchronization.service}</Text>
                            </ListItem.Content>
                        </ListItem>
                    )
                }
                {
                    synchronization?.enabled === undefined ? undefined : (
                        <ListItem>
                            <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                                <Text>Enabled:</Text>
                                <Text>{synchronization.enabled ? "Yes" : "No"}</Text>
                            </ListItem.Content>
                        </ListItem>
                    )
                }
                {
                    synchronization?.configuration === undefined || synchronization?.service === undefined ? undefined : (
                        <SynchronizationDisplay synchronizationType={synchronization.service}
                                                synchronizationConfiguration={synchronization.configuration}/>
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
                    onPress={() => setEditing({editing: true, synchronization})}
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
                    onPress={deleteSynchronizationPrompt}
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
                        editing.synchronization === undefined ? undefined : editingRender(editing.synchronization)
                    }
                </View>
            </Overlay>
        </View>
    )

}
