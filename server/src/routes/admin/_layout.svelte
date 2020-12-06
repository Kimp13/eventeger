<script context="module">
  import getPermission from "getPermission";

  export async function preload(page, session) {
    if (session.user.isAuthenticated) {
      const permission = getPermission(session.user.permissions, [
        'admin'
      ]);

      if (permission) {
        return {};
      }
    }

    this.error(404, "Такой страницы, простите, не существует.");

    return {};
  }
</script>

<script>
</script>

<style lang="scss">
  header {
    position: sticky;
    top: 0;
    height: 3rem;
  }
</style>

<header>
  <a href="/admin/">Главная</a>
  <a href="/admin/home">Дом админа</a>
  <a href="/admin/hello">Hello админа</a>
</header>

<slot />
