<script>
    import { createEventDispatcher } from "svelte";
    import { slide } from "svelte/transition";

    import { mdiChevronUp } from "@mdi/js";

    import Icon from "Icon.svelte";

    // --------------------------------------------

    export let children;
    export let shorthand = false;
    export let label = "";

    // --------------------------------------------

    const parent = Boolean(children);
    const dispatch = createEventDispatcher();

    let checked = false;
    let indeterminate = false;

    let updated = false;

    let togglerValue = false;
    let togglerMatters = false;

    // --------------------------------------------
    // Initial actions (aka constructor) list is yet empty.
    // --------------------------------------------

    function observeChildren(value, index) {
        let checkedCount = 0;
        let indeterminateCount = 0;

        children[index].shorthand = value;

        for (const idk of children) {
            if (idk.shorthand !== false) {
                checkedCount += 1;

                if (idk.shorthand !== true) {
                    indeterminateCount += 1;
                }
            }
        }

        updated = true;
        togglerMatters = false;
        checked = checkedCount > 0;
        indeterminate =
            indeterminateCount > 0 ||
            (checkedCount > 0 && checkedCount < children.length);

        if (indeterminate) {
            shorthand = null;
        } else {
            shorthand = checked;
        }

        dispatch("change", shorthand);
    }

    function updateChildren() {
        if (parent) {
            for (let i = 0; i < children.length; i += 1) {
                children[i].shorthand = shorthand;
            }

            children = children;
        }
    }

    function handleChange(e) {
        shorthand = e.target.checked;

        updateChildren();
        dispatch("change", shorthand);
    }

    function toggle() {
        togglerMatters = true;

        togglerValue = !toggled;
    }

    // --------------------------------------------

    $: if (updated) {
        updated = false;
    } else if (shorthand !== null) {
        togglerMatters = false;
        indeterminate = false;

        checked = shorthand;

        updateChildren();
    }

    $: toggled = togglerMatters ? togglerValue : !indeterminate && checked;
</script>

<style lang="scss">
    .checkbox-container {
        .toggler {
            font-size: 1.25rem;
            vertical-align: bottom;
            line-height: 0;
            transition: transform 0.3s ease;

            &.toggled {
                transform: scaleY(-1);
            }

            &:hover {
                cursor: pointer;
            }
        }
    }

    .children {
        margin-left: 1rem;
    }
</style>

<div class="checkbox-container">
    <label>
        <input
            type="checkbox"
            {checked}
            {indeterminate}
            on:change={handleChange} />
        {label}
    </label>
    {#if parent}
        <button class="toggler" class:toggled on:click={toggle}>
            <Icon icon={mdiChevronUp} />
        </button>
    {/if}
</div>
{#if parent && !toggled}
    <div class="children" transition:slide|local={{ duration: 300 }}>
        {#each children as _child, index (_child.id || _child.label)}
            <svelte:self
                on:change={(event) => observeChildren(event.detail, index)}
                label={_child.label}
                children={_child.children}
                shorthand={_child.shorthand} />
        {/each}
    </div>
{/if}
