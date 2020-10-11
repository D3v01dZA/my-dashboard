import {toastError} from "./errors";
import {Project} from "./model";

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
