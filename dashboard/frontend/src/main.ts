import Vue from 'vue'
import App from './App.vue'
import router from './router'
import { BootstrapVue, IconsPlugin } from 'bootstrap-vue'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'
import Vuex from 'vuex'
import {createStore} from "@/store";
import {UserService} from "@/user/UserService";
import {ApiService} from "@/ApiService";

Vue.use(BootstrapVue);
Vue.use(IconsPlugin);

Vue.use(Vuex);

let apiService = new ApiService();
let store = createStore(new UserService(apiService));

Vue.config.productionTip = false;

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app');
