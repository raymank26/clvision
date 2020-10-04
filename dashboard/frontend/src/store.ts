import Vuex, {ActionContext, Module} from 'vuex'
import {User, UserService} from "@/user/UserService";

export function createStore(userService: UserService) {
    let userModule: Module<UserState, RootState> = {
        state: {
            user: null
        },
        getters: {
            user: state => {
                return state.user
            }
        },
        actions: {
            "login": function (context: ActionContext<UserState, RootState>, payload: any): any {
                return userService.login(payload.username, payload.password).then(user => {
                    context.state.user = user;
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
}

