<template>
  <div class="home">
      <login-component/>
  </div>
</template>

<script>
// @ is an alias to /src

import LoginComponent from "@/user/LoginComponent";
export default {
  name: 'Home',
  components: {LoginComponent},
}
</script>
