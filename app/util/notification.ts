import PushNotification, {ChannelObject, PushNotificationOptions} from "react-native-push-notification";
import {toastError} from "./errors";
import AsyncStorage from "@react-native-community/async-storage";
import {BroadcastMessage, createPost, BroadcastSynchronizationAttempt, TimeStatus, TimeStatusType} from "./model";
import moment from "moment";

export const showTimeNotification = (timeStatus: TimeStatus) => {
    if (timeStatus.status === "NONE") {
        PushNotification.clearLocalNotification("TimeNotification", 76839126);
    } else {
        let notification = {
            channelId: "TimeNotification",
            message: timeStatus.status,
            ongoing: true,
            id: 76839126,
            tag: "TimeNotification",
            usesChronometer: true,
            showWhen: true,
            playSound: false,
            onlyAlertOnce: true,
            priority: "default",
            importance: "default",
            visibility: "private",
            when: moment().format("x")
        };
        PushNotification.localNotification(notification as any)
    }
}

export const showSynchronizeAttempt = (synchronizationAttempt: BroadcastSynchronizationAttempt) => {
    let notification = {
        channelId: "SynchronizationAttemptNotification",
        message: `Synchronization attempt ${synchronizationAttempt.id} completed with ${synchronizationAttempt.status}`,
        id: Math.floor(Math.random() * Math.floor(10000)),
        tag: "SynchronizationAttemptNotification",
        priority: "default",
        importance: "default",
        visibility: "private"
    };
    PushNotification.localNotification(notification as any)
}

export const createChannels = () => {
    createChannel({
        channelId: "TimeNotification",
        channelName: "Time Notification"
    })
    createChannel({
        channelId: "SynchronizationAttemptNotification",
        channelName: "Synchronization Attempt Notification"
    })
}

export const createChannel = (channel: ChannelObject) => {
    PushNotification.channelExists(channel.channelId, exists => {
        if (!exists) {
            PushNotification.createChannel(channel, created => {
                if (!created) {
                    toastError(`Channel ${channel.channelId} not created`);
                }
            })
        }
    });
}

export const updateFirebaseToken = (url: string) => {
    AsyncStorage.multiGet(["oldSavedToken", "newSavedToken"], () => {
    })
        .then(tokens => {
            console.log(tokens);
            const oldSavedToken = tokens[0][1];
            const newSavedToken = tokens[1][1];
            if (newSavedToken !== null && oldSavedToken !== newSavedToken) {
                return fetch(
                    `${url}/broadcast/update`,
                    createPost({
                        oldBroadcast: oldSavedToken,
                        newBroadcast: newSavedToken
                    })
                )
                    .then(response => {
                        if (response.status === 200) {
                            return AsyncStorage.setItem("oldSavedToken", newSavedToken, () => {
                            });
                        }
                        return Promise.reject(`Failed to update Firebase token with ${response.status}`);
                    })
            }
        })
        .catch(reason => {
            toastError(reason);
        })
}

export const Options: PushNotificationOptions = {

    onRegister: token => {
        AsyncStorage.setItem("newSavedToken", token.token)
            .catch(reason => {
                toastError(reason);
            })
    },

    onRegistrationError: error => {
        toastError(error);
    },

    onNotification: notification => {
        if (!notification.userInteraction) {
            const message: BroadcastMessage = {
                type: (notification.data as any).type,
                message: JSON.parse((notification.data as any).message)
            };
            switch (message.type) {
                case "TIME":
                    showTimeNotification(message.message as TimeStatus);
                    break;
                case "SYNCHRONIZE_ATTEMPT":
                    showSynchronizeAttempt(message.message as BroadcastSynchronizationAttempt);
                    break;
            }
        } else {
            let channelId = (notification as any).channelId;
            switch (channelId) {
                case "TimeNotification":
                    PushNotification.localNotification(notification as any);
                    break;
            }
        }
    }

}
