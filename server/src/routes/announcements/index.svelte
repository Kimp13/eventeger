<script context="module">
    import { getPreloadApiResponse } from "requests";

    export async function preload(page, session) {
        if (session.user.isAuthenticated) {
            return {
                announcements: await getPreloadApiResponse(
                    `${session.apiUrl}/announcements/getMine`,
                    {},
                    this
                ),
            };
        } else {
            this.redirect(301, "/auth");
        }
    }
</script>

<script>
    import { getApiResponse } from "requests";

    import { stores } from "@sapper/app";

    // -------------------------------------------------------------------------

    export let announcements;

    // -------------------------------------------------------------------------

    const { session } = stores();

    const limit = announcements.length;
    let skip = announcements.length;
    let loading = false;
    let thatsit = false;

    // -------------------------------------------------------------------------

    function checkAnnouncements(event) {
        if (
            !loading &&
            !thatsit &&
            ((event.target.scrollY + event.target.clientHeight) / 4) * 3 >
                event.target.scrollHeight
        ) {
            loading = true;

            getApiResponse(
                `${$session.apiUrl}/announcements/getMine`,
                { limit, skip },
                true
            ).then((newAnnouncements) => {
                if (newAnnouncements.length < limit) {
                    thatsit = true;
                }

                skip += newAnnouncements.length;
                announcements = announcements.concat(newAnnouncements);
                loading = false;
            });
        }
    }

    // -------------------------------------------------------------------------
</script>

<svelte:body on:scroll={checkAnnouncements} />

{#if announcements.length === 0}
    <h2>Тут пусто.</h2>
{:else}
    <div class="announcements">
        {#each announcements as announcement (announcement.id)}
            <div class="announcement">
                <h2>Объявление</h2>
                <p>Автор: {announcement.author}</p>
                <p>Текст: {announcement.text}</p>
            </div>
        {/each}
    </div>
{/if}
