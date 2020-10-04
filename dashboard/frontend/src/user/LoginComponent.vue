<template>
    <div class="row justify-content-center mt-4">
        <div class="col-4">
            <div class="row justify-content-center col-auto">
                <h3>Login</h3>
            </div>
            <form v-on:submit.prevent="submit">
                <div class="form-group">
                    <label for="username">Username</label>
                    <input id="username" class="form-control" name="username" v-model="username">
                </div>
                <div class="form-group">
                    <label for="username">Password</label>
                    <input id="password" class="form-control" name="password" v-model="password">
                </div>
                <button class="btn btn-primary" id="submit" type="submit">Submit</button>
            </form>
            <div class="mt-2">
                <div v-if="errorType === 'no input'">
                    <div class="alert alert-danger" role="alert">
                        Username or password is not set.
                    </div>
                </div>
                <div v-else-if="errorType === 'mismatch'">
                    <div class="alert alert-danger" role="alert">
                        Illegal username or password.
                    </div>
                </div>
                <div v-else-if="errorType === 'unknown'">
                    <div class="alert alert-danger" role="alert">
                        Unknown error
                    </div>
                </div>
            </div>

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
