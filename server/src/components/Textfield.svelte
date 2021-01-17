<script>
  export let placeholder = "Введите текст";
  export let type = "text";

  export let value;

  let autocomplete;

  const handleInput = (e) => {
    value = type.match(/^(number|range)$/) ? +e.target.value : e.target.value;
  };

  $: {
    if (type === "password") {
      autocomplete = "current-password";
    } else {
      autocomplete = true;
    }
  }
</script>

<style lang="scss">
  @import "colors";
</style>

<input {type} {placeholder} {autocomplete} on:input={handleInput} />
