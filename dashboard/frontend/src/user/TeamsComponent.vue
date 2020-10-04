<template>
    <div>
        <div v-for="team in $store.getters.teams">
            <div>
                Name: {{team.name}}
                <router-link to="{name: 'teamDashboard', params: {id: team.id}}"></router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import { Component, Vue } from 'vue-property-decorator';

    @Component
    export default class TeamsComponent extends Vue {

        created() {
            this.$store.dispatch("loadTeams");
        }
    }
</script>

<style scoped>

</style>