import Vue from 'vue'
import VueRouter, {RouteConfig} from 'vue-router'
import LoginComponent from "@/user/LoginComponent.vue";
import TeamsComponent from '@/user/TeamsComponent.vue';
import {store} from "@/main";

Vue.use(VueRouter);

const routes: Array<RouteConfig> = [
    {
        path: "/",
        name: "teams",
        component: TeamsComponent,
        beforeEnter: (to, from, next) => {
            if (!store.getters.user) {
                next({name: "login"})
            } else {
                next()
            }
        }
    },
    {
        path: '/login',
        name: 'login',
        component: LoginComponent
    },
    {
        path: '/about',
        name: 'About',
        // route level code-splitting
        // this generates a separate chunk (about.[hash].js) for this route
        // which is lazy-loaded when the route is visited.
        component: () => import(/* webpackChunkName: "about" */ '../views/About.vue')
    }
];

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
});

export default router
