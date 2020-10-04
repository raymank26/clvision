import Axios from "axios";

export class ApiService {

    get<T = any>(methodName: String, queryParams?: object): Promise<T> {
        return Axios.get<T>("/api/" + methodName, {
            params: queryParams
        }).then(response => response.data as T);
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