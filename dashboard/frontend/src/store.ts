import Vuex, {ActionContext, Module} from 'vuex'
import {Team, User, UserService} from "@/user/UserService";

export function createStore(userService: UserService) {
    let userModule: Module<UserState, RootState> = {
        state: {
            user: null,
            teams: []
        },
        getters: {
            user: state => {
                return state.user
            },
            teams: state => {
                return state.teams;
            }
        },
        mutations: {
            setUser: (context, user) => {
                context.user = user;
            },
            setTeams: (context, teams) => {
                context.teams = teams;
            }
        },
        actions: {
            "login": function (context, payload: any): any {
                return userService.login(payload.username, payload.password).then(user => {
                    context.commit("setUser", user);
                })
            },
            "loadTeams": (context, payload) => {
                return userService.listTeams().then(teams => {
                    context.commit("setTeams", teams);
                })
            }
        }
    };
    return new Vuex.Store({
        modules: {
            userModule
        }
    });
}

interface RootState {

}

interface UserState {
    user: User | null
    teams: Team[]
}

