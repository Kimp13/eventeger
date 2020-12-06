<script context="module">
  import getPermission from "getPermission";
  import { getPreloadApiResponse } from "requests";

  export async function preload(page, session) {
    const permission = getPermission(session.user.permissions, [
      "admin",
      "insert",
    ]);

    if (permission) {
      const models = await getPreloadApiResponse(
        "/admin/api/getModels",
        {},
        this
      );

      return {
        models,
      };
    } else {
      this.error(404, "Это не те дроиды");
    }
  }
</script>

<script>
  import Title from "Title.svelte";

  export let models;
</script>

<style lang="scss">
</style>

<Title caption="Модели" />

<div class="cards">
  {#each models as model}
    <a href="/admin/insert/{model.tableName}">
      {model.tableName}
    </a>
  {/each}
</div>
