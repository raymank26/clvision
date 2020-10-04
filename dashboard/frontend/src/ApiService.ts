import Axios from "axios";

export class ApiService {

    get(methodName: String, queryParams: object) {
        return Axios.get("/api/" + methodName, {
            params: queryParams
        });
    }

    post<T = any>(methodName: String, config: PostConfig): Promise<T> {
        return Axios.post<T>("/api/" + methodName, config.bodyParams, {
            params: config.queryParams
        }).then(response => response.data as T);
    }
}

export class PostConfig {
    queryParams?: object;
    bodyParams?: object
}