<template>
    <div class="container">
        <router-view/>
    </div>
</template>

<script lang="ts">
    import { Component, Vue } from 'vue-property-decorator';

    @Component
    export default class ContainerComponent extends Vue {
    }
</script>

<style scoped>

</style>