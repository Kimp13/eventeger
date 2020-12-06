<script>
  let className;
  export {className as class};
  export let title, type;


</script>

<style lang="scss">
  @import "colors";
</style>

<button on:click {type} class={className}>
  {title}
</button>
