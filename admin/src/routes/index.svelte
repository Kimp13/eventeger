<h1>
  Hello, World!
</h1>

<script>
  import { onMount } from 'svelte';
  import { setCookie } from '../../utils/cookies';

  onMount(() => setCookie('jwt', 'hello'));
</script>

<style lang="sass">
  h1
    color: #aaa
    text-align: center
</style>