<script context="module">
  import { getPreloadApiResponse } from "requests";

  export async function preload(page, session) {
    const model = await getPreloadApiResponse(
      `/admin/api/getModel/${page.params.tableName}`,
      {},
      this
    );

    if (model.hasOwnProperty("tableName")) {
      return model;
    }

    this.error(404, "Таких моделей у нас нет.");
  }
</script>

<script>
  import TextField from "Textfield.svelte";
  import Title from "Title.svelte";
  import Button from "Button.svelte";
  import { postApi } from "requests";

  export let columns;
  export let tableName;

  let columnValues = {};
  let columnErrors = {};

  for (const key of Object.keys(columns)) {
    columnValues[key] = "";
    columnErrors[key] = false;
  }

  const submit = () => {
    for (const key of Object.keys(columnErrors)) {
      if (columnErrors[key] !== false) {
        return;
      }
    }

    postApi(`/admin/api/insert/${tableName}`, columnValues, true)
      .then(res => {
        console.log(res);
      }, e => {
        console.log(e);
      });
  };
</script>

<style lang="scss">
  .insert-header {
    text-align: center;
  }
</style>

<Title caption={tableName} />

<h2 class="insert-header">{tableName}</h2>

{#each Object.keys(columns) as key}
  hello i am column
{/each}

<Button on:click={submit} class="insert-submit" placeholder="Создать" />