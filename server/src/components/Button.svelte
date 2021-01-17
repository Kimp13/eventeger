<script>
  import Icon from "./Icon.svelte";

  // ------------------------------------

  let className;
  export { className as class };
  export let type = "button";
  export let disabled = false;
  export let secondary = false;
  export let wide = false;
  export let icon;

  // ------------------------------------
</script>

<style lang="scss">
  @import "colors";

  button {
    // SCSS variables to keep it dry
    $clr: var(--button-component-text-color, white);
    $primclr: var(--button-component-background-color, $color-primary);
    $scndclr: var(--button-component-background-color, $color-secondary);

    // Element properties
    color: $clr;
    background-color: $primclr;
    padding: 0.25rem 0.5rem;
    transition: transform 0.3s ease;

    // Icon properties
    --icon-component-color: #{$clr};
    --icon-component-vertical-align: -20%;
    --icon-component-font-size: 1.5rem;

    &.secondary {
      background-color: $scndclr;
    }

    &.wide {
      display: block;
      width: 100%;
    }

    &.disabled {
      opacity: 0.8;
      cursor: not-allowed;
    }

    &:not(.disabled) {
      &:hover {
        cursor: pointer;
      }

      &:active {
        transform: scale(0.975);
      }
    }
  }
</style>

<button
  on:click
  {type}
  {disabled}
  class={className}
  class:wide
  class:secondary
  class:disabled>
  {#if icon}
    <Icon {icon} />
  {/if}
  <slot />
</button>
