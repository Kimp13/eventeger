<script>
  export let options;
</script>

<style lang="sass">
  @import "../theme/colors"
</style>

<select>
  {#each options as option}
    <option>{option}</option>
  {/each}
</select>