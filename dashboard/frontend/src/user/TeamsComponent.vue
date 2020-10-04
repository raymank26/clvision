<template>
    <div>
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