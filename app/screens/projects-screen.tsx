import {Navigation} from "../App";
import {useCallback, useContext, useState} from "react";
import {AppContext} from "../util/app-context";
import {createPost, Project} from "../util/model";
import {toastError} from "../util/errors";
import {Dimensions, ScrollView, View} from "react-native";
import {Button, Header, Input, ListItem, Overlay, Text} from "react-native-elements";
import {useFocusEffect} from '@react-navigation/native';
import * as React from "react";

export const ProjectsScreen = ({navigation}: { navigation: Navigation }) => {

    const {url} = useContext(AppContext);

    const [adding, setAdding] = useState(false);
    const [addingName, setAddingName] = useState("");

    const [projects, setProjects] = useState<Array<Project>>([]);

    const fetchProjects = () => {
        fetch(`${url}/time/project`)
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else {
                    return Promise.reject(`Failed to fetch projects with ${response.status}`);
                }
            })
            .then(projects => {
                setProjects(projects);
            })
            .catch(reason => {
                toastError(reason);
            })
    }

    const createProject = () => {
        fetch(
            `${url}/time/project`,
            createPost({name: addingName})
        )
            .then(response => {
                if (response.status === 201) {
                    return response.json();
                } else {
                    return Promise.reject(`Failed to create project with ${response.status}`);
                }
            })
            .then(() => {
                setAdding(false);
                fetchProjects();
            })
            .catch(reason => {
                setAdding(false);
                toastError(reason);
            })
    }

    useFocusEffect(
        useCallback(() => {
            fetchProjects();
        }, [url])
    );

    return (
        <View style={{flex: 1}}>
            <Header
                leftComponent={{icon: "menu", color: "#fff", onPress: () => navigation.openDrawer()}}
                centerComponent={{text: "Projects", style: {color: "#fff", fontSize: 18}}}
            />
            <ScrollView>
                {
                    projects.map(project => {
                        return (
                            <ListItem
                                key={project.id}
                                onPress={() => navigation.navigate("Project", {projectId: project.id})}
                            >
                                <ListItem.Content>
                                    <Text>{project.name}</Text>
                                </ListItem.Content>
                            </ListItem>
                        )
                    })
                }
            </ScrollView>
            <View>
                <Button
                    title="Add"
                    icon={{
                        type: "font-awesome-5",
                        name: "plus",
                        color: "white"
                    }}
                    onPress={() => {
                        setAdding(true);
                        setAddingName("");
                    }}
                />
            </View>

            <Overlay
                isVisible={adding}
                onBackdropPress={() => setAdding(false)}
            >
                <View style={{
                    width: Dimensions.get('window').width * 0.85,
                    maxHeight: Dimensions.get('window').height * 0.85
                }}>
                    <ScrollView>
                        <View>
                            <Input
                                label="Name"
                                placeholder="Name"
                                value={addingName}
                                onChangeText={setAddingName}
                            />
                        </View>
                        <View style={{flexDirection: "row", justifyContent: "space-evenly"}}>
                            <Button
                                title="Add"
                                icon={{
                                    type: "font-awesome-5",
                                    name: "plus",
                                    color: "white"
                                }}
                                containerStyle={{flexGrow: 1}}
                                onPress={() => createProject()}
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
                                onPress={() => setAdding(false)}
                            />
                        </View>
                    </ScrollView>
                </View>
            </Overlay>
        </View>
    );
}
