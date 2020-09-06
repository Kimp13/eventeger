<main>
	<slot></slot>
</main>

<style global lang="stylus">
  @font-face
    font-family defaultFont
    font-weight 400
    font-style normal
    src url('fonts/Merriweather-Regular.ttf')

  @font-face
    font-family defaultFont
    font-weight 700
    font-style normal
    src url('fonts/Merriweather-Bold.ttf')

  @font-face
    font-family defaultFont
    font-weight 400
    font-style italic
    src url('fonts/Merriweather-Italic.ttf')

  @font-face
    font-family defaultFont
    font-weight 700
    font-style italic
    src url('fonts/Merriweather-BoldItalic.ttf')

  *
    margin 0
    padding 0
    border 0
    outline 0
    vertical-align baseline
    background transparent
    z-index 0
    box-sizing border-box

  html
    font-size calc(1vw + 2vh)
    font-family defaultFont, serif

  @media all and (orientation landscape)
    html
      font-size calc(1vw + 1vh)
</style>