<script context="module">

  export async function preload (page, session) {
    return {
      user: await (await this.fetch(session.apiUrl + '/users/signin', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          username: 'Kimp13',
          password: 'Iampinkdog!23'
        })
      })).json()
    };
  }

</script>

<script>
  export let user;
</script>

<p>
  {JSON.stringify(user)}
</p>