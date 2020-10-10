import {createContext} from "react";

export type Credentials = {
    username: string,
    password: string
}

export type Authentication = {
    url: string,
    credentials?: Credentials,
    setCredentials: (credentials: Credentials) => void,
    clearCredentials: () => void
}

export const Context = createContext<Authentication>({
        url: "",
        credentials: undefined,
        setCredentials: () => {
        },
        clearCredentials: () => {
        }
    }
);