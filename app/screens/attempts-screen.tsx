import {useCallback, useContext, useState} from "react";
import {AppContext} from "../util/app-context";
import {Dimensions, Image, ScrollView, View} from "react-native";
import * as React from "react";
import {Header} from "../util/header";
import {SynchronizationAttempt, TimeType} from "../util/model";
import {useFocusEffect} from "@react-navigation/native";
import {selectProject, selectTime} from "../util/nav";
import {ProjectsNavigation, ProjectsRoute} from "./projects-navigator";
import {toastError} from "../util/errors";
import {ListItem, Overlay, Text} from "react-native-elements";
import Toast from "react-native-root-toast";

export const AttemptsScreen = ({navigation, route}: { navigation: ProjectsNavigation, route: ProjectsRoute<"Attempts"> }) => {

    const {url} = useContext(AppContext);

    const [synchronizationAttempts, setSynchronizationAttempts] = useState<Array<SynchronizationAttempt>>([]);

    const [displayedSynchronizationAttempt, setDisplayedSynchronizationAttempt] = useState<SynchronizationAttempt>();

    const fetchSynchronizationAttempts = () => {
        fetch(`${url}/time/project/${route.params.projectId}/synchronization/${route.params.synchronizationId}/attempt`)
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else if (response.status === 404) {
                    return undefined;
                } else {
                    return Promise.reject(`Failed to fetch attempts with ${response.status}`);
                }
            })
            .then(synchronizationAttempts => {
                setSynchronizationAttempts(synchronizationAttempts);
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
                fetchSynchronizationAttempts();
            }
        }, [url, route.params.projectId, route.params.synchronizationId])
    );

    return (
        <View style={{flex: 1}}>
            <Header navigation={navigation.dangerouslyGetParent()} title="Time"/>
            <ScrollView>
                {
                    synchronizationAttempts.map(synchronizationAttempt => {
                        return (
                            <ListItem
                                key={synchronizationAttempt.id}
                                onPress={() => {
                                    if (synchronizationAttempt.status === "SUCCESS") {
                                        setDisplayedSynchronizationAttempt(synchronizationAttempt)
                                    } else {
                                        Toast.show(`Cannot view attempt with status ${synchronizationAttempt.status}`);
                                    }
                                }}
                            >
                                <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                                    <Text>Status:</Text>
                                    <Text>{synchronizationAttempt.status}</Text>
                                </ListItem.Content>
                            </ListItem>
                        )
                    })
                }
            </ScrollView>

            <Overlay
                isVisible={displayedSynchronizationAttempt !== undefined}
                onBackdropPress={() => setDisplayedSynchronizationAttempt(undefined)}
            >
                <View style={{
                    width: Dimensions.get('window').width * 0.85,
                    maxHeight: Dimensions.get('window').height * 0.85
                }}>
                    <Image
                        style={{
                            width: Dimensions.get('window').width * 0.85,
                            height: Dimensions.get('window').height * 0.85
                        }}
                        source={{uri: `data:image/png;base64,${displayedSynchronizationAttempt?.screenshot.base64}`}}
                    />
                </View>
            </Overlay>
        </View>
    )

}
