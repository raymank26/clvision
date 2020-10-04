import {createLocalVue, shallowMount, Wrapper} from '@vue/test-utils'
import Vuex, {Store} from 'vuex'
import LoginComponent from '@/user/LoginComponent.vue'
import {createStore} from "@/store";
import {UserService} from "@/user/UserService";
import {ApiService} from "@/ApiService";
import Vue from "vue";
import TeamsComponent from "@/user/TeamsComponent.vue";

const localVue = createLocalVue();
localVue.use(Vuex);

describe('TeamComponent.vue', () => {
    let store: Store<any>;
    let apiService: ApiService;
    let wrapper: Wrapper<LoginComponent>;

    beforeEach(() => {
        apiService = {
            post: jest.fn(),
            get: jest.fn((url, config) => {
                if (url === "listTeams") {
                    return Promise.resolve<any>([
                        {
                            "id": "2892",
                            "name": "firstTeam"
                        },
                        {
                            "id": "1829",
                            "name": "secondTeam"
                        },
                    ]);
                } else {
                    return Promise.reject()
                }
            })
        };
        store = createStore(new UserService(apiService));

        const elem = document.createElement('div');
        if (document.body) {
            document.body.appendChild(elem);
        }
        wrapper = shallowMount(TeamsComponent, {
            store: store,
            localVue: localVue,
            attachTo: elem,
            stubs: {
                RouterLink: true
            }
        });
    });

    it('TeamComponent lists teams', async done => {
        await Vue.nextTick();
        expect(store.getters.teams.length).toBe(2);

        expect(wrapper.html()).toContain("firstTeam");
        expect(wrapper.html()).toContain("secondTeam");

        done()
    });

    afterEach(() => {
        wrapper.destroy();
    })
});
