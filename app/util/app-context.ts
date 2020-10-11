import {createContext} from "react";

export type Context = {
    url: string,

    authenticated: boolean,
    setAuthenticated: (authenticated: boolean) => void
}

export const AppContext = createContext<Context>({
        url: "",
        authenticated: false,
        setAuthenticated: () => {}
    }
);
