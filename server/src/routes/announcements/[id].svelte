<script context="module">
    import { getPreloadApiResponse } from "requests";

    export async function preload(page, session) {
        if (session.user.isAuthenticated) {
            const announcement = await getPreloadApiResponse(
                `${session.apiUrl}/announcements/find`,
                {
                    id: page.params.id,
                },
                this
            );

            return { announcement };
        } else {
            this.redirect(301, "/auth");
        }
    }
</script>

<script>
    import isEmpty from 'lodash/isEmpty';
    
    export let announcement;
</script>

{#if announcement.hasOwnProperty('text')}
    {console.log(announcement)}
{:else}Такого объявления, простите, нет.{/if}
