import {toastError} from "./errors";
import {createPost, Project} from "./model";

export const fetchProject = (url: string, projectId: number, callback: (project: Project | undefined) => void) => {
    fetch(`${url}/time/project/${projectId}`)
        .then(response => {
            if (response.status === 200) {
                return response.json();
            } else if (response.status === 404) {
                return undefined;
            } else {
                return Promise.reject(`Failed to fetch project with ${response.status}`);
            }
        })
        .then(project => {
            callback(project);
        })
        .catch(reason => {
            toastError(reason);
        })
}

export const timeAction = (url: string, projectId: number, type: string, callback: () => void) => {
    fetch(
        `${url}/time/project/${projectId}/${type}`,
        createPost(undefined)
    )
        .then(response => {
            if (response.status === 200) {
                return response.json();
            } else {
                return Promise.reject(`Failed to ${type} with ${response.status}`);
            }
        })
        .then(() => {
            callback();
        })
        .catch(reason => {
            toastError(reason);
        })
}
