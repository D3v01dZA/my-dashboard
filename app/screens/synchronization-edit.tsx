import {
    MaconomyConfiguration,
    NetsuiteConfiguration, OpenairConfiguration, SucceedingConfiguration,
    SynchronizationConfiguration,
    SynchronizationType
} from "../util/model";
import React from "react";
import {View} from "react-native";
import {Input, Text} from "react-native-elements";

export const SynchronizationEdit = ({synchronizationType, synchronizationConfiguration, setSynchronizationConfiguration}: { synchronizationType: SynchronizationType, synchronizationConfiguration: SynchronizationConfiguration, setSynchronizationConfiguration: (configuration: SynchronizationConfiguration) => void }) => {

    switch (synchronizationType) {
        case SynchronizationType.MACONOMY:
            const editingMaconomy = synchronizationConfiguration as MaconomyConfiguration;
            return (
                <View>
                    <Input
                        label="URL"
                        placeholder="URL"
                        textContentType="URL"
                        value={editingMaconomy.url}
                        onChangeText={url => setSynchronizationConfiguration({...editingMaconomy, url})}
                    />
                    <Input
                        label="Username"
                        placeholder="Username"
                        textContentType="username"
                        value={editingMaconomy.username}
                        onChangeText={username => setSynchronizationConfiguration({...editingMaconomy, username})}
                    />
                    <Input
                        label="Password"
                        placeholder="Password"
                        textContentType="password"
                        value={editingMaconomy.password}
                        onChangeText={password => setSynchronizationConfiguration({...editingMaconomy, password})}
                    />
                    <Input
                        label="Project Name"
                        placeholder="Project Name"
                        value={editingMaconomy.projectName}
                        onChangeText={projectName => setSynchronizationConfiguration({...editingMaconomy, projectName})}
                    />
                    <Input
                        label="Task Name"
                        placeholder="Task Name"
                        value={editingMaconomy.taskName}
                        onChangeText={taskName => setSynchronizationConfiguration({...editingMaconomy, taskName})}
                    />
                </View>
            );
        case SynchronizationType.NETSUITE:
            const editingNetsuite = synchronizationConfiguration as NetsuiteConfiguration;
            return (
                <View>
                    <Input
                        label="Username"
                        placeholder="Username"
                        textContentType="username"
                        value={editingNetsuite.username}
                        onChangeText={username => setSynchronizationConfiguration({...editingNetsuite, username})}
                    />
                    <Input
                        label="Password"
                        placeholder="Password"
                        textContentType="password"
                        value={editingNetsuite.password}
                        onChangeText={password => setSynchronizationConfiguration({...editingNetsuite, password})}
                    />
                    <Input
                        label="Project Name"
                        placeholder="Project Name"
                        value={editingNetsuite.project}
                        onChangeText={project => setSynchronizationConfiguration({...editingNetsuite, project})}
                    />
                    <Input
                        label="Task Name"
                        placeholder="Task Name"
                        value={editingNetsuite.task}
                        onChangeText={task => setSynchronizationConfiguration({...editingNetsuite, task})}
                    />
                    <Input
                        label="Security Answers"
                        placeholder="Security Answers Not Editable"
                        disabled={true}
                        value={(editingNetsuite.answers ?? []).map(answer => `${answer.fragment} = ${answer.answer}`).join("; ")}
                    />
                </View>
            );
        case SynchronizationType.OPENAIR:
            const editingOpenair = synchronizationConfiguration as OpenairConfiguration;
            return (
                <View>
                    <Input
                        label="Company"
                        placeholder="Company"
                        value={editingOpenair.companyId}
                        onChangeText={companyId => setSynchronizationConfiguration({...editingOpenair, companyId})}
                    />
                    <Input
                        label="Username"
                        placeholder="Username"
                        textContentType="username"
                        value={editingOpenair.userId}
                        onChangeText={userId => setSynchronizationConfiguration({...editingOpenair, userId})}
                    />
                    <Input
                        label="Password"
                        placeholder="Password"
                        textContentType="password"
                        value={editingOpenair.password}
                        onChangeText={password => setSynchronizationConfiguration({...editingOpenair, password})}
                    />
                    <Input
                        label="Project Name"
                        placeholder="Project Name"
                        value={editingOpenair.project}
                        onChangeText={project => setSynchronizationConfiguration({...editingOpenair, project})}
                    />
                    <Input
                        label="Task Name"
                        placeholder="Task Name"
                        value={editingOpenair.task}
                        onChangeText={task => setSynchronizationConfiguration({...editingOpenair, task})}
                    />
                </View>
            );
        case SynchronizationType.SUCCEEDING:
            const editingSucceeding = synchronizationConfiguration as SucceedingConfiguration;
            return (
                <View>
                    <Input
                        label="Website"
                        placeholder="Website"
                        value={editingSucceeding.website}
                        onChangeText={website => setSynchronizationConfiguration({...editingSucceeding, website})}
                    />
                </View>
            );
        case SynchronizationType.FAILING:
            return <View/>;
        default:
            return (
                <View>
                    <Text>Unknown Synchronization Type</Text>
                </View>
            )
    }

}
