<script context="module">
  export async function preload(_, session) {
    if (!session.user.isAuthenticated) {
      this.redirect(301, "/auth");
    }
  }
</script>

<script>
  import { stores } from "@sapper/app";

  import Title from "Title.svelte";

  const { session } = stores();
</script>

<style lang="scss">
  @import "colors";

  h1 {
    color: $color-primary;
    text-align: center;
  }
</style>

<Title caption="Главная" />

<h2>
  Ну, здравствуй,
</h2>
<h1>
  {$session.user.firstName || $session.user.username}.
</h1>
