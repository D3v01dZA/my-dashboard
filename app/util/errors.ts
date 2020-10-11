import Toast from "react-native-root-toast";

export const toastError = (reason: any) => {
    console.error(reason);
    Toast.show(JSON.stringify(reason));
}
