<script context=module>

  export async function preload (page, session) {
    try {
      const init = await (await this.fetch(session.apiUrl + '/connects', {
        credentials: 'include'
      })).json();

      if (init.redirect) {
        this.redirect(307, (init.redirectUrl || '/auth'));
      }
    } catch (e) {
      console.log(e);
    }
  }

</script>

<h1>
  Hello, World!
</h1>

<script>
  import { onMount } from 'svelte';
  import { setCookie } from '../../utils/cookies';

  onMount(() => setCookie('jwt', 'hello'));
</script>

<style lang="stylus">
  h1
    color #aaa
    text-align center
</style>