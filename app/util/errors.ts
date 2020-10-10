import SimpleToast from "react-native-simple-toast";

export const toastError = (reason: any) => {
    console.error(reason);
    SimpleToast.show(JSON.stringify(reason));
}