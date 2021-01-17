<script>
  import { stores } from "@sapper/app";

  import { postApi } from "requests";

  import {
    mdiChevronLeft,
    mdiMenu,
    mdiClose,
    mdiChevronRight,
    mdiPencilOutline,
    mdiHome,
  } from "@mdi/js";

  import Icon from "Icon.svelte";

  // ---------------------------------------------------------------------------

  const { session } = stores();

  let slideupToggled = false;
  let slideupHidden = true;

  // ---------------------------------------------------------------------------

  function hideSlideup() {
    if (!slideupToggled) {
      slideupHidden = true;
    }
  }

  function toggleSlideup() {
    if (slideupToggled) {
      slideupToggled = false;
    } else {
      requestAnimationFrame(() => {
        slideupHidden = false;

        requestAnimationFrame(() => {
          slideupToggled = true;
        });
      });
    }
  }

  function back() {
    history.back();
  }

  function forward() {
    history.forward();
  }

  function urlBase64ToUint8Array(base64String) {
    let padding = "=".repeat((4 - (base64String.length % 4)) % 4);
    let base64 = (base64String + padding)
      .replace(/\-/g, "+")
      .replace(/_/g, "/");

    let rawData = window.atob(base64);
    let outputArray = new Uint8Array(rawData.length);

    for (let i = 0; i < rawData.length; ++i) {
      outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
  }

  const updatePushMessaging = (isAuthenticated) =>
    new Promise((resolve, reject) => {
      if (
        isAuthenticated &&
        "serviceWorker" in navigator &&
        "PushManager" in window
      ) {
        const permissionResult = Notification.requestPermission(resolve);

        if (permissionResult) {
          permissionResult.then(resolve, reject);
        }
      } else {
        reject();
      }
    })
      .then((permissionResult) => {
        if (permissionResult === "granted") {
          return navigator.serviceWorker.getRegistration();
        } else {
          throw "Fuck.";
        }
      })
      .then((registration) =>
        registration.pushManager.getSubscription().then((subscription) => {
          if (subscription === null) {
            const subscribeOptions = {
              userVisibleOnly: true,
              applicationServerKey: urlBase64ToUint8Array(
                "BNYKuXiNeEasdlm9LJMUBl9ssPM9TkhsTRDXSnjyIxsq" +
                  "YURjbT74PLW8BwN5henbxSponO2VP_NnqwZodKJAJHI"
              ),
            };

            return registration.pushManager.subscribe(subscribeOptions);
          } else {
            subscription.previouslySubscribed = true;

            return subscription;
          }
        })
      )
      .then((subscription) => {
        if (subscription.previouslySubscribed !== true) {
          postApi(
            `${$session.apiUrl}/notifications/registerEndpoint`,
            subscription,
            true
          );
        }
      })
      .catch(console.log);

  // ---------------------------------------------------------------------------

  if (typeof window !== "undefined") {
    session.subscribe((value) => {
      updatePushMessaging(value.user.isAuthenticated);
    });
    // updatePushMessaging($session);
  }
</script>

<style global lang="scss">
  @import "colors";

  @font-face {
    font-family: defaultFont;
    font-weight: 400;
    font-style: normal;
    src: url("/fonts/Merriweather-Regular.ttf");
  }

  @font-face {
    font-family: defaultFont;
    font-weight: 700;
    font-style: normal;
    src: url("/fonts/Merriweather-Bold.ttf");
  }

  @font-face {
    font-family: defaultFont;
    font-weight: 400;
    font-style: italic;
    src: url("/fonts/Merriweather-Italic.ttf");
  }

  @font-face {
    font-family: defaultFont;
    font-weight: 700;
    font-style: italic;
    src: url("/fonts/Merriweather-BoldItalic.ttf");
  }

  * {
    margin: 0;
    padding: 0;
    border: 0;
    outline: 0;
    vertical-align: baseline;
    background-color: transparent;
    z-index: 0;
    box-sizing: border-box;
    font-family: defaultFont;
  }

  button {
    font-size: 1rem;
  }

  html {
    font-size: calc(1vw + 2vh);
    font-family: defaultFont, serif;
    height: 100%;
    padding: 0;
  }

  body {
    height: 100%;
    margin: 0;
  }

  main {
    padding-bottom: 3rem;
  }

  nav {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;

    .slideup,
    .control {
      background: $color-surface;
    }

    .slideup-container {
      --shadow-size: 0.1rem;
      padding-top: calc(var(--shadow-size) * 2);
      overflow: hidden;

      .slideup {
        transform: translateY(100%);
        transition: transform 0.3s ease;
        max-height: 40vh;
        padding: 0.25rem 0.5rem;
        background-color: $color-primary;
        color: $color-surface;
        box-shadow: 0 calc(var(--shadow-size) * -1) var(--shadow-size)
          $color-primary;
        overflow-y: auto;

        &.toggled {
          transform: none;
        }

        &.hidden * {
          display: none;
        }

        a {
          color: inherit;

          &:active {
            color: inherit;
          }

          &:focus {
            color: inherit;
          }

          &:visited {
            color: inherit;
          }

          &:link {
            color: inherit;
          }
        }
      }
    }

    .control {
      display: flex;
      align-items: center;
      padding: 0.25rem 0.5rem;

      & > * {
        display: flex;
        justify-content: center;
      }

      * {
        flex: 1;
        line-height: 0;
      }

      button,
      a {
        font-size: 2rem;

        &:hover {
          cursor: pointer;
        }
      }
    }
  }

  @media all and (orientation: landscape) {
    html {
      font-size: calc(1vw + 1vh);
    }

    [data-tooltip] {
      position: relative;
      z-index: 1;

      &:before,
      &:after {
        visibility: hidden;
        opacity: 0;
        pointer-events: none;
        transition: transform 0.3s ease-out, opacity 0.4s ease;
        transform: translate(-50%, -0.5rem);
      }

      &:before {
        position: absolute;
        bottom: 100%;
        left: 50%;
        margin-bottom: 0.3rem;
        padding: 0.5rem;
        width: 100%;
        min-width: 8rem;
        max-width: 30rem;
        border-radius: 0.2rem;
        background-color: $color-surface;
        color: $color-primary;
        content: attr(data-tooltip);
        border: 0.2rem solid $color-primary;
        text-align: center;
        font-size: 1rem;
        line-height: 1.2;
      }

      &:after {
        position: absolute;
        bottom: 100%;
        left: 50%;
        width: 0;
        border-top: 0.3rem solid $color-primary;
        border-right: 0.3rem solid transparent;
        border-left: 0.3rem solid transparent;
        content: " ";
        font-size: 0;
        line-height: 0;
      }

      &:hover {
        &:before,
        &:after {
          visibility: visible;
          opacity: 1;
          transform: translate(-50%, 0);
        }
      }
    }
  }
</style>

<main>
  <slot />
</main>
{#if $session.user.isAuthenticated}
  <nav>
    <div class="slideup-container">
      <div
        class="slideup"
        class:toggled={slideupToggled}
        class:hidden={slideupHidden}
        on:transitionend={hideSlideup}>
        <a href="/auth">Авторизация</a>
      </div>
    </div>
    <div class="control">
      <div class="history-container">
        <button
          class="back"
          on:click={back}
          data-tooltip="Назад"
          aria-label="Назад">
          <Icon icon={mdiChevronLeft} />
        </button>
        <button
          class="forward"
          on:click={forward}
          data-tooltip="Вперёд"
          aria-label="Вперёд">
          <Icon icon={mdiChevronRight} />
        </button>
      </div>
      <a href="/" class="home" data-tooltip="На главную">
        <Icon icon={mdiHome} />
      </a>
      <a href="/announce" class="announce" data-tooltip="Объявить">
        <Icon icon={mdiPencilOutline} />
      </a>
      <button
        class="toggle"
        on:click={toggleSlideup}
        data-tooltip={slideupToggled ? 'Закрыть меню' : 'Открыть меню'}
        aria-label="Переключить">
        <Icon icon={slideupToggled ? mdiClose : mdiMenu} />
      </button>
    </div>
  </nav>
{/if}
