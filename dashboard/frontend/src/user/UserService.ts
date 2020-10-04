import {ApiService} from "@/ApiService";

export class UserService {
    private apiService: ApiService;

    constructor(apiService: ApiService) {
        this.apiService = apiService;
    }

    login(username: String, password: String): Promise<User> {
        return this.apiService.post<User>("login", {
            bodyParams: {
                "username": username,
                "password": password
            }
        });
    }

    listTeams(): Promise<Team[]> {
        return this.apiService.get<Team[]>("listTeams");
    }
}

export interface User {
    id: string
    username: string
}

export interface Team {
    id: string,
    name: string
}