import {createLocalVue, shallowMount, Wrapper} from '@vue/test-utils'
import Vuex, {Store} from 'vuex'
import LoginComponent from '@/user/LoginComponent.vue'
import {createStore} from "@/store";
import {UserService} from "@/user/UserService";
import {ApiService} from "@/ApiService";
import Vue from "vue";
import {VueRouter} from "vue-router/types/router";

const localVue = createLocalVue();

localVue.use(Vuex);

describe('RegistrationComponent.vue', () => {
    let store: Store<any>;
    let apiService: ApiService;
    let wrapper: Wrapper<LoginComponent>;
    let $router: any;

    beforeEach(() => {
        apiService = {
            get: jest.fn(), post: jest.fn(() => {
                return Promise.resolve<any>({
                    username: "anton.ermak"
                });
            })
        };
        store = createStore(new UserService(apiService));

        const elem = document.createElement('div');
        if (document.body) {
            document.body.appendChild(elem);
        }
        $router = {
            "push": jest.fn()
        } as any;
        wrapper = shallowMount(LoginComponent, {
            store: store,
            localVue: localVue,
            attachTo: elem,
            mocks: {
                $router
            }
        });
    });

    it('Login form submits content', async done => {
        let username = wrapper.find("#username");
        let password = wrapper.find("#password");
        username.setValue("anton.ermak");
        password.setValue("123892");
        let submitButton = wrapper.find("#submit");
        submitButton.trigger("click");

        await Vue.nextTick();
        expect(store.getters.user).toBeDefined();

        await Vue.nextTick();
        expect($router.push.mock.calls[0][0]).toBe("/dashboard");
        done()
    });

    afterEach(() => {
        wrapper.destroy();
    })
});
