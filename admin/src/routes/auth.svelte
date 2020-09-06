<script context=module>

  export async function preload (page, session) {
    return {
      props: await (await this.fetch(session.apiUrl + '/users/count')).json()
    };
  }

</script>

<svelte:head>
  {#if isSignup}
    <title>Регистрация</title>
  {:else}
    <title>Вход</title>
  {/if}
</svelte:head>

{#if isSignup}
  <form class=signup on:click={signup}>
    <h1>
      Регистрация
    </h1>
  </form>
{:else}
  <form class=signin on:click={signin}>

  </form>
{/if}

<script>
  export let props;

  const isSignup = props.count === 0;

  const signup = e => {

  };
  
  const signin = e => {

  };

</script>

<style lang="stylus">
  @require("styles/variables");
  
  form
    width 100%
    max-width 40rem
    margin 3rem auto

    h1
      color theme_color_black
      text-align center
</style>