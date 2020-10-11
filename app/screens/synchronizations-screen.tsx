import {Navigation, Route} from "../App";
import * as React from "react";
import {useCallback, useContext, useState} from "react";
import {AppContext} from "../util/app-context";
import {
    createPost,
    FailingConfiguration,
    MaconomyConfiguration,
    NetsuiteConfiguration,
    OpenairConfiguration,
    SucceedingConfiguration,
    Synchronization,
    SynchronizationConfiguration,
    SynchronizationType
} from "../util/model";
import {toastError} from "../util/errors";
import {Dimensions, ScrollView, View} from "react-native";
import {Button, Header, Input, ListItem, Overlay, Text} from "react-native-elements";
import {useFocusEffect} from '@react-navigation/native';
import {selectProject} from "../util/nav";
import {SynchronizationEdit} from "./synchronization-edit";
import {Picker} from "@react-native-community/picker";

export const SynchronizationsScreen = ({navigation, route}: { navigation: Navigation, route: Route<"Synchronizations"> }) => {

    const emptyMaconomy: MaconomyConfiguration = {url: "", username: "", password: "", projectName: "", taskName: ""};
    const emptyNetsuite: NetsuiteConfiguration = {username: "", password: "", project: "", task: "", answers: []}
    const emptyOpenair: OpenairConfiguration = {companyId: "", userId: "", password: "", project: "", task: ""}
    const emptySucceeding: SucceedingConfiguration = {website: ""}
    const emptyFailing: FailingConfiguration = {}

    const emptyConfigurations: { [key in SynchronizationType]: SynchronizationConfiguration } = {
        MACONOMY: emptyMaconomy,
        NETSUITE: emptyNetsuite,
        OPENAIR: emptyOpenair,
        SUCCEEDING: emptySucceeding,
        FAILING: emptyFailing
    };

    const {url} = useContext(AppContext);

    const [adding, setAdding] = useState(false);
    const [addingSynchronization, setAddingSynchronization] = useState<{ type: SynchronizationType, name: string, configuration: SynchronizationConfiguration }>({
        type: SynchronizationType.MACONOMY,
        name: SynchronizationType.MACONOMY,
        configuration: emptyMaconomy
    })

    const [synchronizations, setSynchronizations] = useState<Array<Synchronization>>([]);

    const fetchSynchronizations = () => {
        fetch(`${url}/time/project/${route.params.projectId}/synchronization`)
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else {
                    return Promise.reject(`Failed to fetch synchronizations with ${response.status}`);
                }
            })
            .then(projects => {
                setSynchronizations(projects);
            })
            .catch(reason => {
                toastError(reason);
            })
    }

    const createSynchronization = () => {
        fetch(
            `${url}/time/project/${route.params.projectId}/synchronization`,
            createPost({
                enabled: true,
                name: addingSynchronization.name,
                service: addingSynchronization.type,
                configuration: addingSynchronization.configuration
            })
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
                fetchSynchronizations();
            })
            .catch(reason => {
                setAdding(false);
                toastError(reason);
            })
    }

    useFocusEffect(
        useCallback(() => {
            if (route.params.projectId === undefined) {
                selectProject(navigation);
            } else {
                fetchSynchronizations();
            }
        }, [url, route.params.projectId])
    );

    return (
        <View style={{flex: 1}}>
            <Header
                leftComponent={{icon: "menu", color: "#fff", onPress: () => navigation.openDrawer()}}
                centerComponent={{text: "Synchronizations", style: {color: "#fff", fontSize: 18}}}
            />
            <ScrollView>
                {
                    synchronizations.map(synchronization => {
                        return (
                            <ListItem
                                key={synchronization.id}
                                onPress={() => navigation.navigate("Synchronization", {
                                    projectId: route.params.projectId,
                                    synchronizationId: synchronization.id
                                })}
                            >
                                <ListItem.Content>
                                    <Text>{synchronization.name}</Text>
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
                            <Picker
                                mode="dropdown"
                                selectedValue={addingSynchronization.type}
                                onValueChange={value => {
                                    let type = value as SynchronizationType;
                                    setAddingSynchronization({
                                        type,
                                        name: type,
                                        configuration: emptyConfigurations[type]
                                    })
                                }}
                            >
                                {
                                    Object.keys(SynchronizationType).map(type => <Picker.Item key={type} label={type}
                                                                                              value={type}/>)
                                }
                            </Picker>
                            <View>
                                <Input
                                    label="Name"
                                    placeholder="Name"
                                    value={addingSynchronization.name}
                                    onChangeText={name => setAddingSynchronization({...addingSynchronization, name})}
                                />
                                <SynchronizationEdit
                                    synchronizationType={addingSynchronization.type}
                                    setSynchronizationConfiguration={configuration => {
                                        setAddingSynchronization({
                                            ...addingSynchronization,
                                            configuration
                                        })
                                    }}
                                    synchronizationConfiguration={addingSynchronization.configuration}
                                />
                            </View>
                        </View>
                        <View style={{flexDirection: "row"}}>
                            <Button
                                title="Add"
                                icon={{
                                    type: "font-awesome-5",
                                    name: "plus",
                                    color: "white"
                                }}
                                containerStyle={{flexGrow: 1}}
                                onPress={() => createSynchronization()}
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
