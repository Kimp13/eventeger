<script>
  export let placeholder = "Введите текст";
  export let type = "text";

  export let value;

  const handleInput = e => {
    value = type.match(/^(number|range)$/)
      ? +e.target.value
      : e.target.value;
  };
</script>

<style lang="scss">
  @import "colors";

</style>

<input {type} {placeholder} on:input={handleInput} />