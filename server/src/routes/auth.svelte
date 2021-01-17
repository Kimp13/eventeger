<script>
  import { goto, stores } from "@sapper/app";
  import { setCookie } from "cookies";
  import { deleteCookie } from "cookies";

  import Title from "Title.svelte";
  import Loader from "Loader.svelte";
  import SignIn from "auth/SignIn.svelte";

  const { page, session } = stores();
  const redirectTo = $page.query.redirectTo || "/";

  let redirecting = false;

  const logout = () => {
    deleteCookie("jwt");

    session.update((session) => {
      session.user = {
        isAuthenticated: false,
      };

      return session;
    });
  };

  const signed = (e) => {
    setCookie("jwt", e.detail.jwt, {
      sameSite: "Strict",
      maxAge: 1296000,
    });

    session.update((session) => {
      session.user = e.detail.data;
      session.user.isAuthenticated = true;

      return session;
    });

    redirecting = true;

    goto(redirectTo);
  };
</script>

<style lang="scss">
  @import "colors";

  .already-registered {
    width: 100%;
    max-width: 35rem;
    margin: 0 auto;
    padding: 3rem 0;
    text-align: right;

    h1 {
      font-size: 4em;
    }

    h1,
    h2,
    p {
      text-align: center;
      color: $color-green;
      margin: 0.5em 0;
    }

    a,
    button {
      display: inline-block;
      font-size: 1rem;
      font-family: defaultFont;
      text-decoration: none;
      border-width: 0.15rem;
      border-style: solid;
      margin: 0.5em;
      padding: 0.25rem 0.5rem;
      transition: color 0.3s ease, background-color 0.3s ease;

      &:hover {
        cursor: pointer;
      }
    }

    .logout {
      color: $color-error;
      border-color: $color-error;

      &:hover {
        background-color: $color-error;
        color: white;
      }
    }

    .continue {
      color: $color-blue;
      border-color: $color-blue;

      &:hover {
        background-color: $color-blue;
        color: white;
      }
    }
  }
</style>

<Title caption="Вход" />

{#if redirecting}
  <Loader />
{:else if $session.user.isAuthenticated}
  <div class="already-registered">
    <h1>Снова?</h1>
    <h2>Вы уже зарегистрированы.</h2>
    <p>Если вы хотите войти в другой аккаунт, сначала выйдите из текущего.</p>
    <button class="logout" on:click={logout}> Выйти </button>
    <a class="continue" href={redirectTo}> Продолжить </a>
  </div>
{:else}
  <SignIn on:signed={signed} />
{/if}
