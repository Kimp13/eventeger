<script context="module">
    import { getPreloadApiResponse } from "requests";

    export async function preload(_, session) {
        if (session.user.isAuthenticated) {
            const map = await getPreloadApiResponse(
                `${session.apiUrl}/roles/mine`,
                {},
                this
            );

            const classesSet = new Set();
            const rolesArray = [];

            for (const roleId in map) {
                rolesArray.push(roleId);

                for (const classId in map[roleId]) {
                    classesSet.add(classId);
                }
            }

            const [roles, classes] = await Promise.all([
                getPreloadApiResponse(
                    `${session.apiUrl}/roles/`,
                    { id: rolesArray },
                    this
                ),
                getPreloadApiResponse(
                    `${session.apiUrl}/class/`,
                    { id: Array.from(classesSet) },
                    this
                ),
            ]);

            return {
                map,
                roles,
                classes,
            };
        } else {
            this.redirect(301, "/auth");
        }
    }
</script>

<script>
    import { slide } from "svelte/transition";
    import { sort } from "timsort";
    import search from "binary-search";
    import { clone } from "lodash";
    import { postApi } from "requests";
    import { stores } from "@sapper/app";

    import { mdiMessageAlertOutline } from "@mdi/js";

    import Title from "Title.svelte";
    import NestedCheckbox from "Checkbox.svelte";
    import Button from "Button.svelte";
    import Loader from "Loader.svelte";

    // -------------------------------------------------------------------------

    export let classes;
    export let roles;
    export let map;

    // -------------------------------------------------------------------------

    const { session } = stores();
    const muted = classes.length === 0 || roles.length === 0;
    const serviceArray = [];

    let gradedClasses;

    let value = "";
    let tried = false;
    let sent = false;
    let error = false;
    let sendPromise;

    // -------------------------------------------------------------------------

    if (!muted)
        (function init() {
            // я функцию сделяль
            // чтобы не оставлять объект в области видимости
            const compFunction = (a, b) => a - b;

            for (const role of roles) {
                const roleArray = [];
                const classesArray = [];

                for (const classEntity of classes) {
                    if (
                        search(map[role.id], classEntity.id, compFunction) >= 0
                    ) {
                        roleArray.push(classEntity);
                    }
                }

                sort(roleArray, function sort(a, b) {
                    return a.grade === b.grade
                        ? a.letter.charCodeAt(0) - b.letter.charCodeAt(0)
                        : a.grade - b.grade;
                });

                let previousGrade = roleArray[0].grade;
                let graded = {
                    label: `${roleArray[0].grade}-я параллель`,
                    shorthand: false,
                    children: [
                        {
                            label: `${roleArray[0].letter} класс`,
                            shorthand: false,
                            id: roleArray[0].id,
                        },
                    ],
                };

                for (let i = 1; i < classes.length; i += 1) {
                    if (roleArray[i].grade !== previousGrade) {
                        classesArray.push(graded);

                        previousGrade = roleArray[i].grade;
                        graded = {
                            label: `${previousGrade}-я параллель`,
                            shorthand: false,
                            children: [],
                        };
                    }

                    graded.children.push({
                        label: `${roleArray[i].letter} класс`,
                        shorthand: false,
                        id: roleArray[i].id,
                    });
                }

                classesArray.push(graded);

                serviceArray.push({
                    label: `Роль: ${role.name}`,
                    shorthand: false,
                    children: classesArray,
                    id: role.id,
                });
            }
        })();

    // -------------------------------------------------------------------------

    function createRequestArray(initial, predefined) {
        const unique = initial.hasOwnProperty("id");
        const strict = typeof initial.shorthand === "boolean";
        let retarr = [];

        function helper(value) {
            for (const child of initial.children) {
                const nested = createRequestArray(child, value);

                if (Array.isArray(nested)) {
                    for (const nest of nested) {
                        retarr.push(nest);
                    }
                } else {
                    retarr.push(nested);
                }
            }
        }

        if (unique) {
            if (strict) {
                if (initial.shorthand) {
                    return initial.id;
                }
            } else {
                helper();
            }
        } else {
            helper(
                predefined === undefined
                    ? strict
                        ? initial.shorthand
                        : undefined
                    : predefined
            );
        }

        return retarr;
    }

    function announce() {
        tried = true;

        requestAnimationFrame(() => {
            if (!disabled) {
                const recipients = serviceArray.every(
                    (child) => child.shorthand
                )
                    ? true
                    : serviceArray.reduce((previous, current) => {
                          previous[current.id] = createRequestArray(current);

                          return previous;
                      }, {});

                sendPromise = postApi(
                    `${$session.apiUrl}/announcements/create`,
                    {
                        text: announcementText,
                        recipients,
                    },
                    true
                );

                sendPromise.then(
                    function onResolved() {
                        sent = true;
                    },

                    function onRejected() {
                        error = true;
                    }
                );
            }
        });
    }

    // -------------------------------------------------------------------------

    $: if (!muted) {
        const serviceClasses = clone(classes);
        gradedClasses = [];

        sort(serviceClasses, function compareFunction(a, b) {
            return a.grade === b.grade
                ? b.letter.charCodeAt(0) - a.letter.charCodeAt(0)
                : b.grade - a.grade;
        });

        let previousGrade = serviceClasses[serviceClasses.length - 1].grade;
        let graded = [serviceClasses[serviceClasses.length - 1]];

        for (let i = serviceClasses.length - 2; i >= 0; i -= 1) {
            if (previousGrade !== serviceClasses[i].grade) {
                gradedClasses.push(graded);
                previousGrade = serviceClasses[i].grade;
                graded = [];
            }

            graded.push(serviceClasses[i]);
        }

        gradedClasses.push(graded);
    }

    $: noRecipients = serviceArray.every((child) => child.shorthand === false);
    $: announcementText = value
        .trim()
        .replace(/  +/g, " ")
        .replace(/\n\n+/g, "\n");
    $: noText = announcementText.length === 0;

    $: disabled = tried && (noRecipients || noText || error);
</script>

<style lang="scss">
    @import "colors";

    .no-recipients,
    a {
        text-align: center;
    }

    a {
        display: block;
        margin-top: 1rem;
    }

    .announce-container {
        padding: 0 0.25rem;
        overflow: hidden;

        h3 {
            padding: 0.25rem 0;
            text-align: center;
            color: $color-secondary;
        }

        .error {
            padding: 0.25rem 0.5rem;
            text-align: center;
            color: $color-error;
        }

        .announce-text,
        .checkbox-container {
            padding: 0.5rem;
        }

        .announce-text {
            display: block;
            width: 100%;
            resize: vertical;
            font-family: defaultFont;
            font-size: 1rem;
            border: 0.2rem double $color-secondary;
            border-radius: 0.4rem;
            min-height: 15rem;
        }
    }
</style>

<Title caption="Объявить" />

{#if muted}
    <h2 class="no-recipients">К сожалению, нет доступных адресатов.</h2>
    <a href="/">На главную</a>
{:else if sent}
    wow
{:else}
    <div class="announce-container">
        <h3>Текст объявления</h3>
        {#if tried && noText}
            <p class="error" transition:slide>Напишите текст объявления.</p>
        {/if}
        <textarea class="announce-text" bind:value />
        <h3>Получатели</h3>
        {#if tried && noRecipients}
            <p class="error" transition:slide>Выберите получателей.</p>
        {/if}
        <div class="checkbox-container">
            <NestedCheckbox
                label="Все получатели"
                shorthand={false}
                bind:children={serviceArray} />
        </div>
        <div class="button-container">
            <Button
                on:click={announce}
                {disabled}
                wide
                secondary
                icon={mdiMessageAlertOutline}>
                Объявить
            </Button>
            {#if sendPromise}
                {#await sendPromise}
                    <Loader />
                {:catch}
                    <p class="error">
                        К сожалению, произошла какая-то ошибка. Попробуйте снова
                        через пару минут или обратитесь к администратору.
                    </p>
                {/await}
            {/if}
        </div>
    </div>
{/if}
