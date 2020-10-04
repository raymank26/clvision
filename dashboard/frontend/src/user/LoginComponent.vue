<template>
    <div>
        <form v-on:submit.prevent="submit">
            <input id="username" name="username" v-model="username">
            <input id="password" name="password" v-model="password">
            <button id="submit" type="submit">Submit</button>
        </form>
        <div v-if="errorType === 'no input'">
            Username or password is not set.
        </div>
        <div v-else-if="errorType === 'mismatch'">
            Illegal username or password.
        </div>
        <div v-else-if="errorType === 'unknown'">
            Unknown error
        </div>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';

@Component
export default class LoginComponent extends Vue {

    username: string = "";
    password: string = "";
    errorType: string = "";

    submit() {
        if (!this.username || !this.password) {
            this.errorType = "no input";
            return;
        }
        this.$store.dispatch("login", {
            "username": this.username,
            "password": this.password
        }).then(() => {
            this.$router.push("/dashboard")
        }).catch(error => {
            if (error.key === "illegal.credits") {
                this.errorType = "mismatch";
            } else {
                this.errorType = "unknown";
            }
        });
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
