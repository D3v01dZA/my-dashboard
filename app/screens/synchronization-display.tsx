import {
    MaconomyConfiguration,
    NetsuiteConfiguration, OpenairConfiguration, SucceedingConfiguration,
    SynchronizationConfiguration,
    SynchronizationType
} from "../util/model";
import React from "react";
import {View} from "react-native";
import {Input, ListItem, Text} from "react-native-elements";

export const SynchronizationDisplay = ({synchronizationType, synchronizationConfiguration}: { synchronizationType: SynchronizationType, synchronizationConfiguration: SynchronizationConfiguration}) => {

    switch (synchronizationType) {
        case SynchronizationType.MACONOMY:
            const maconomy = synchronizationConfiguration as MaconomyConfiguration;
            return (
                <React.Fragment>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>URL:</Text>
                            <Text>{maconomy.url}</Text>
                        </ListItem.Content>
                    </ListItem>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Username:</Text>
                            <Text>{maconomy.username}</Text>
                        </ListItem.Content>
                    </ListItem>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Password:</Text>
                            <Text>{maconomy.password}</Text>
                        </ListItem.Content>
                    </ListItem>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Project Name:</Text>
                            <Text>{maconomy.projectName}</Text>
                        </ListItem.Content>
                    </ListItem>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Task Name:</Text>
                            <Text>{maconomy.taskName}</Text>
                        </ListItem.Content>
                    </ListItem>
                </React.Fragment>
            );
        case SynchronizationType.NETSUITE:
            const netsuite = synchronizationConfiguration as NetsuiteConfiguration;
            return (
                <React.Fragment>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Username:</Text>
                            <Text>{netsuite.username}</Text>
                        </ListItem.Content>
                    </ListItem>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Password:</Text>
                            <Text>{netsuite.password}</Text>
                        </ListItem.Content>
                    </ListItem>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Project Name:</Text>
                            <Text>{netsuite.project}</Text>
                        </ListItem.Content>
                    </ListItem>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Task Name:</Text>
                            <Text>{netsuite.task}</Text>
                        </ListItem.Content>
                    </ListItem>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Security Answers:</Text>
                            <Text>Security Answers Not Viewable</Text>
                        </ListItem.Content>
                    </ListItem>
                </React.Fragment>
            );
        case SynchronizationType.OPENAIR:
            const openair = synchronizationConfiguration as OpenairConfiguration;
            return (
                <React.Fragment>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Company:</Text>
                            <Text>{openair.companyId}</Text>
                        </ListItem.Content>
                    </ListItem>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Username:</Text>
                            <Text>{openair.userId}</Text>
                        </ListItem.Content>
                    </ListItem>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Password:</Text>
                            <Text>{openair.password}</Text>
                        </ListItem.Content>
                    </ListItem>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Project Name:</Text>
                            <Text>{openair.project}</Text>
                        </ListItem.Content>
                    </ListItem>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Task Name:</Text>
                            <Text>{openair.task}</Text>
                        </ListItem.Content>
                    </ListItem>
                </React.Fragment>
            );
        case SynchronizationType.SUCCEEDING:
            const succeeding = synchronizationConfiguration as SucceedingConfiguration;
            return (
                <React.Fragment>
                    <ListItem>
                        <ListItem.Content style={{flexDirection: "row", justifyContent: "space-between"}}>
                            <Text>Website:</Text>
                            <Text>{succeeding.website}</Text>
                        </ListItem.Content>
                    </ListItem>
                </React.Fragment>
            );
        case SynchronizationType.FAILING:
            return <React.Fragment/>;
        default:
            return (
                <View>
                    <Text>Unknown Synchronization Type</Text>
                </View>
            )
    }

}
