<template>
    <form v-on:submit.prevent="submit">
        <input id="username" name="username" v-model="username">
        <input id="password" name="password" v-model="password">
        <button id="submit" type="submit">Submit</button>
    </form>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';

@Component
export default class LoginComponent extends Vue {

    username: string = "";
    password: string = "";

    created() {
        console.log("HERE2");
    }

    submit() {
        console.log("SUBMIT");
        this.$store.dispatch("login", {
            "username": this.username,
            "password": this.password
        }).then(() => {
            this.$router.push("/dashboard")
        });
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
