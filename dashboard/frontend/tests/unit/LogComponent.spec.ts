import {createLocalVue, shallowMount, Wrapper} from '@vue/test-utils'
import Vuex, {Store} from 'vuex'
import LoginComponent from '@/user/LoginComponent.vue'
import {createStore} from "@/store";
import {UserService} from "@/user/UserService";
import {ApiService} from "@/ApiService";
import Vue from "vue";

const localVue = createLocalVue();
localVue.use(Vuex);

const USER_NAME = "anton.ermak";

describe('LoginComponent.vue', () => {
    let store: Store<any>;
    let apiService: ApiService;
    let wrapper: Wrapper<LoginComponent>;
    let $router: any;

    beforeEach(() => {
        apiService = {
            get: jest.fn(), post: jest.fn((url, config) => {
                if ((config.bodyParams as any)["username"] === USER_NAME) {
                    return Promise.resolve<any>({
                        username: USER_NAME
                    });
                } else {
                    return Promise.reject<any>({
                        key: "illegal.credits"
                    });
                }
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
        username.setValue(USER_NAME);
        password.setValue("123892");
        let submitButton = wrapper.find("#submit");
        submitButton.trigger("click");

        await Vue.nextTick();
        expect(store.getters.user).toBeDefined();

        await Vue.nextTick();
        expect($router.push.mock.calls[0][0]).toBe("/dashboard");
        done()
    });

    it('Login form shows error on empty input', async done => {
        let expectedError = "Username or password is not set";

        expect(wrapper.html()).not.toContain(expectedError);

        let submitButton = wrapper.find("#submit");
        submitButton.trigger("click");

        await Vue.nextTick();

        expect(wrapper.html()).toContain(expectedError);

        done();
    });

    it("Login form shows error on illegal input", async done => {
        let expectedError = "Illegal username or password";
        expect(wrapper.html()).not.toContain(expectedError);

        let username = wrapper.find("#username");
        let password = wrapper.find("#password");
        username.setValue("foo");
        password.setValue("bar");
        let submitButton = wrapper.find("#submit");
        submitButton.trigger("click");

        await Vue.nextTick();
        await Vue.nextTick();
        await Vue.nextTick();
        expect(wrapper.html()).toContain(expectedError);
        done()
    });

    afterEach(() => {
        wrapper.destroy();
    })
});
