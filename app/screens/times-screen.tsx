import * as React from "react";
import {useCallback, useContext, useState} from "react";
import {AppContext} from "../util/app-context";
import {Navigation, Route} from "../App";
import {ScrollView, View} from "react-native";
import {Header} from "../util/header";
import {format, Time, TimeType} from "../util/model";
import {useFocusEffect} from "@react-navigation/native";
import SimpleToast from "react-native-simple-toast";
import {ListItem, Text} from "react-native-elements";
import {toastError} from "../util/errors";
import moment from "moment";
import {selectProject} from "../util/nav";

export const TimesScreen = ({navigation, route}: { navigation: Navigation, route: Route<"Times"> }) => {

    const {url} = useContext(AppContext);

    const [times, setTimes] = useState<Array<Time>>([]);

    const fetchTimes = () => {
        fetch(`${url}/time/project/${route.params.projectId}/time?type=CURRENT_WEEK`)
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else {
                    return Promise.reject(`Failed to fetch times with ${response.status}`);
                }
            })
            .then(times => {
                setTimes(times);
            })
            .catch(reason => {
                toastError(reason);
            })
    }

    useFocusEffect(
        useCallback(() => {
            if (route.params.projectId === undefined) {
                selectProject(navigation);
            } else {
                fetchTimes();
            }
        }, [url, route.params.projectId])
    );

    return (
        <View style={{flex: 1}}>
            <Header navigation={navigation} title="Time"/>
            <ScrollView>
                {
                    times.map(time => {
                        const start = format(moment(time.start));
                        const end = time.end === undefined ? undefined : format(moment(time.end));
                        return (
                            <ListItem
                                key={time.id}
                                onPress={() => navigation.navigate("Time", {
                                    projectId: route.params.projectId,
                                    timeId: time.id
                                })}
                            >
                                <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                                    <Text>{time.type === TimeType.WORK ? "Work" : "    Break"}</Text>
                                    <Text>{`${start}${end === undefined ? "" : ` - ${end}`}`}</Text>
                                </ListItem.Content>
                            </ListItem>
                        )
                    })
                }
            </ScrollView>
            <View style={{flexDirection: "row", justifyContent: "space-evenly"}}>

            </View>
        </View>
    )

}
