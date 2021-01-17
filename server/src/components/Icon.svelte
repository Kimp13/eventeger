<script>
    let d;

    export { d as icon };

    let path;
</script>

<style lang="scss">
    @import "colors";

    svg {
        font-size: var(--icon-component-font-size, 1em);
        width: 1em;
        height: 1em;
        overflow: visible;
        vertical-align: var(--icon-component-vertical-align, baseline);

        path {
            fill: var(--icon-component-color, $color-primary);
        }
    }
</style>

<svg width="24" height="24" viewBox="0 0 24 24">
    <path {d} bind:this={path} />
</svg>
