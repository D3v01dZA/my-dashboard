import {Moment} from "moment";

export const createPost = (body: any) => {
    return {
        method: "POST",
        headers: {
            "content-type": "application/json"
        },
        body: JSON.stringify(body)
    }
};

export const createDelete = () => {
    return {
        method: "DELETE",
        headers: {
            "content-type": "application/json"
        }
    }
};

export const createPut = (body: any) => {
    return {
        method: "PUT",
        headers: {
            "content-type": "application/json"
        },
        body: JSON.stringify(body)
    }
};

export interface Project {
    id: number,
    name: string
}

export enum TimeType {
    WORK = "WORK",
    BREAK = "BREAK"
}

export type Time = {
    id: number,
    type: TimeType,
    start: string,
    end?: string
}

export enum TimeStatusType {
    NONE = "NONE",
    WORK = "WORK",
    BREAK = "BREAK"
}

export type TimeStatus = {
    status: TimeStatusType,
    projectId?: number,
    timeId?: number,
    runningWorkTotal?: string,
    runningBreakTotal?: string
}

export enum SummaryType {
    CURRENT_DAY = "CURRENT_DAY",
    CURRENT_WEEK = "CURRENT_WEEK",
    CURRENT_MONTH = "CURRENT_MONTH",
    PREVIOUS_MONTH = "PREVIOUS_MONTH"
}

export enum SynchronizationType {
    MACONOMY = "MACONOMY",
    NETSUITE = "NETSUITE",
    OPENAIR = "OPENAIR",
    SUCCEEDING = "SUCCEEDING",
    FAILING = "FAILING"
}

export type MaconomyConfiguration = {
    url: string,
    username: string,
    password: string,
    projectName: string,
    taskName: string
}

export type NetsuiteConfiguration = {
    username: string,
    password: string,
    project: string,
    task: string,
    answers: Array<{
        fragment: string,
        answer: string
    }>
}

export type OpenairConfiguration = {
    companyId: string,
    userId: string,
    password: string,
    project: string,
    task: string
}

export type SucceedingConfiguration = {
    website: string
}

export type FailingConfiguration = {

}

export type SynchronizationConfiguration = MaconomyConfiguration | NetsuiteConfiguration | OpenairConfiguration | SucceedingConfiguration | FailingConfiguration;

export type Synchronization = {
    id: number,
    name: string,
    enabled: boolean,
    service: SynchronizationType,
    configuration: SynchronizationConfiguration
}

export const format = (moment: Moment) => {
    return moment.format("ddd YYYY/MM/DD HH:mm")
}
