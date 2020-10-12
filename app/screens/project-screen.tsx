import {useCallback, useContext, useState} from "react";
import {AppContext} from "../util/app-context";
import {Navigation, Route} from "../App";
import {Alert, Dimensions, ScrollView, View} from "react-native";
import * as React from "react";
import {Header} from "../util/header";
import {createDelete, createPut, Project} from "../util/model";
import {useFocusEffect} from "@react-navigation/native";
import {fetchProject} from "../util/web-calls";
import {Button, Input, ListItem, Overlay, Text} from "react-native-elements";
import {toastError} from "../util/errors";
import {selectProject} from "../util/nav";
import AsyncStorage from "@react-native-community/async-storage";

export const ProjectScreen = ({navigation, route}: { navigation: Navigation, route: Route<"Project"> }) => {

    const {url} = useContext(AppContext);

    const [project, setProject] = useState<Project>();

    const [editing, setEditing] = useState<{ editing: boolean, project?: Project }>({editing: false});

    const deleteProjectPrompt = () => {
        Alert.alert(
            "Delete?",
            `Are you sure you want to delete ${project?.name}?`,
            [
                {
                    text: "Yes",
                    style: "destructive",
                    onPress: () => deleteProject()
                },
                {
                    text: "No",
                    style: "cancel"
                }
            ]
        )
    }

    const deleteProject = () => {
        fetch(
            `${url}/time/project/${route.params.projectId}`,
            createDelete()
        )
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else {
                    return Promise.reject(`Failed to delete project with ${response.status}`);
                }
            })
            .then(() => {
                navigation.navigate("Projects");
            })
            .catch(reason => {
                toastError(reason);
            })
    }

    const saveProject = () => {
        fetch(
            `${url}/time/project/${route.params.projectId}`,
            createPut(editing.project)
        )
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else {
                    return Promise.reject(`Failed to update project with ${response.status}`);
                }
            })
            .then(project => {
                setEditing({...editing, editing: false})
                setProject(project);
            })
            .catch(reason => {
                toastError(reason);
            })
    }

    const selectPressed = () => {
        navigation.navigate("Home", {projectId: route.params.projectId});
        AsyncStorage.setItem("savedProject", `${route.params.projectId}`, () => {})
            .catch(reason => {
                toastError(reason);
            })
    }

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
            }
        }, [url, route.params.projectId])
    );

    const editingRender = (editingProject: Project) => {
        return <ScrollView>
            <View>
                <View>
                    <Input
                        label="Name"
                        placeholder="Name"
                        value={editingProject.name}
                        onChangeText={name => setEditing({
                            ...editing,
                            project: {
                                ...editingProject,
                                name
                            }
                        })}
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
                    onPress={() => saveProject()}
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
            <Header navigation={navigation} title="Project"/>
            <ScrollView>
                <ListItem>
                    <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                        <Text>ID:</Text>
                        <Text>{project?.id}</Text>
                    </ListItem.Content>
                </ListItem>
                <ListItem>
                    <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                        <Text>Name:</Text>
                        <Text>{project?.name}</Text>
                    </ListItem.Content>
                </ListItem>
            </ScrollView>
            <View style={{flexDirection: "row", flexWrap: "wrap", justifyContent: "space-around"}}>
                <Button
                    title="Select"
                    icon={{
                        type: "font-awesome-5",
                        name: "check",
                        color: "white"
                    }}
                    buttonStyle={{backgroundColor: "green"}}
                    containerStyle={{flexGrow: 1}}
                    onPress={selectPressed}
                />
                <Button
                    title="Time"
                    icon={{
                        type: "font-awesome-5",
                        name: "clock",
                        color: "white"
                    }}
                    containerStyle={{flexGrow: 1}}
                    onPress={() => navigation.navigate("Times", {projectId: route.params.projectId})}
                />
                <Button
                    title="Synchronizations"
                    icon={{
                        type: "font-awesome-5",
                        name: "sync",
                        color: "white"
                    }}
                    buttonStyle={{backgroundColor: "orange"}}
                    containerStyle={{flexGrow: 1}}
                    onPress={() => navigation.navigate("Synchronizations", {projectId: route.params.projectId})}
                />
                <Button
                    title="Edit"
                    icon={{
                        type: "font-awesome-5",
                        name: "edit",
                        color: "white"
                    }}
                    containerStyle={{flexGrow: 1}}
                    onPress={() => setEditing({editing: true, project})}
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
                    onPress={deleteProjectPrompt}
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
                        editing.project === undefined ? undefined : editingRender(editing.project)
                    }
                </View>
            </Overlay>
        </View>
    )

}
