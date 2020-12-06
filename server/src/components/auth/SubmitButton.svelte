<script>
  import Button from "Button.svelte";

  export let title = "Принять";

</script>

<style global lang="scss">
  
</style>

<Button
  class="submit"
  type="submit"
  {title}
  on:click />
