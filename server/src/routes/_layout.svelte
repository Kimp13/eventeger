<script context=module>

  export async function preload (page, session) {
    try {
      const user = (await (await this.fetch(session.apiUrl + '/connects', {
        credentials: 'include'
      })).json()).user;

      if (
        !user.isAuthenticated &&
        page.path.substring(0, 5) !== '/auth'
      ) {
        this.redirect(307, ('/auth'));

        return {};
      }

      return {
        user
      };
    } catch (e) {
      console.log(e);
    }
  }

</script>

<main>
	<slot />
</main>

<script>
  import { setContext } from 'svelte';

  export let user;

  setContext('user', user);
</script>

<style global lang="sass">
  @font-face
    font-family: defaultFont
    font-weight: 400
    font-style: normal
    src: url('/fonts/Merriweather-Regular.ttf')

  @font-face
    font-family: defaultFont
    font-weight: 700
    font-style: normal
    src: url('/fonts/Merriweather-Bold.ttf')

  @font-face
    font-family: defaultFont
    font-weight: 400
    font-style: italic
    src: url('/fonts/Merriweather-Italic.ttf')

  @font-face
    font-family: defaultFont
    font-weight: 700
    font-style: italic
    src: url('/fonts/Merriweather-BoldItalic.ttf')

  *
    margin: 0
    padding: 0
    border: 0
    outline: 0
    vertical-align: baseline
    background: transparent
    z-index: 0
    box-sizing: border-box

  html
    font-size: calc(1vw + 2vh)
    font-family: defaultFont, serif
    height: 100%

  body
    height: 100%

  @media all and (orientation: landscape)
    html
      font-size: calc(1vw + 1vh)
</style>