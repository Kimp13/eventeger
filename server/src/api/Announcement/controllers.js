import getPermission from "getPermission";
import { parseDate, dateLess, UTC } from "chrono";
import { sort } from "timsort";
import search from "binary-search";

export default {
    find: async (req, res) => {
        if (req.query.id) {
            const id = parseInt(req.query.id, 10);

            if (id) {
                const announcement = mg.query("announcement").findOne({ id });

                if (announcement) {
                    const relation = mg.query("announcementClassRole").findOne({
                        announcementId: announcement.id,
                        roleId: req.user.roleId,
                        classId: req.user.classId,
                    });

                    if (relation) {
                        res.send(announcement);
                        return;
                    }

                    res.throw(403);
                    return;
                }
            }

            res.throw(400);
        } else {
            const offset = parseDate(req.query.offset);

            const roleIds = getPermission(req.user.permissions, [
                "announcement",
                "read",
            ]);

            if (roleIds === false) {
                res.send([]);
                return;
            }

            let announcements = mg.knex
                .select("announcement.*")
                .from("announcement")
                .innerJoin(
                    "announcementClassRole",
                    "announcementClassRole.announcementId",
                    "announcement.id"
                )
                .where("announcementClassRole.classId", req.user.classId);

            if (Array.isArray(roleIds)) {
                for (let i = 0; i < roleIds.length; i += 1) {
                    roleIds[i] = parseInt(roleIds[i], 10);
                }

                announcements = announcements.andWhereIn(
                    "announcementClassRole.roleId",
                    roleIds
                );
            }

            if (offset) {
                res.send(
                    await announcements
                        .andWhere("announcement.createdAt", "<=", offset)
                        .orderBy("announcement.createdAt", "desc")
                        .offset(1)
                        .limit(25)
                );
            } else {
                res.send(
                    await announcements
                        .orderBy("announcement.createdAt", "desc")
                        .limit(25)
                );
            }
        }
    },

    lastEvents: async (req, res) => {
        let offset;

        if ("offset" in req.query) {
            offset = parseDate(req.query.offset);

            if (!offset) {
                res.throw(400, "Некорректная дата последнего комментария");
                return;
            }
        }

        const roleIds = getPermission(user.permissions, ["announcement", "read"]);

        if (roleIds === false) {
            res.send([]);
            return;
        }

        const classesIds = getPermission(user.permissions, ["class", "multiple"]);

        if (classesIds === false && user.classId === null) {
            res.send([]);
            return;
        }

        let announcements = mg.knex
            .select("announcement.*")
            .from("announcement")
            .innerJoin(
                "announcementClassRole",
                "announcementClassRole.announcementId",
                "announcement.id"
            )
            .where((builder) =>
                builder.where("beginsAt", "!=", null).orWhere("endsAt", "!=", null)
            );

        if (Array.isArray(classesIds)) {
            andNeeded = true;

            for (let i = 0; i < classesIds.length; i += 1)
                classesIds[i] = parseInt(classesIds[i], 10);

            announcements = announcements.andWhereIn(
                "announcementClassRole.classId",
                classesIds
            );
        }

        if (Array.isArray(rolesIds)) {
            for (let i = 0; i < rolesIds.length; i += 1)
                rolesIds[i] = parseInt(rolesIds[i], 10);

            announcements = announcements.andWhereIn(
                "announcementClassRole.roleId",
                rolesIds
            );
        }

        if (offset) {
            res.send(
                await announcements
                    .andWhere("announcement.beginsAt", ">=", offset)
                    .orderBy("announcement.beginsAt", "asc")
                    .offset(1)
                    .limit(25)
            );
        } else {
            res.send(
                await announcements
                    .andWhere("announcement.beginsAt", ">=", UTC())
                    .orderBy("announcement.beginsAt", "asc")
                    .limit(25)
            );
        }
    },

    count: async (req, res) => {
        const count = (
            await mg
                .knex("announcement")
                .count("*")
                .innerJoin(
                    "announcementClassRole",
                    "announcementClassRole.announcementId",
                    "announcement.id"
                )
                .where("announcementClassRole.classId", req.user.classId)
                .andWhere("announcementClassRole.roleId", req.user.roleId)
        )[0]["count(*)"];

        res.send(String(count));
    },

    create: async (req, res) => {
        if (!("text" in req.body) || !("recipients" in req.body)) {
            res.throw(400, "Предоставьте текст и получателей объявления");
            return;
        }

        const explicit = req.body.recipients.constructor === Object;
        const usersMap = await mg.services.role.getUsersCreateMap(req.user);
        const isEvent = Boolean(req.body.event);
        let beginsAt;
        let endsAt;

        if (isEvent) {
            if ("beginsAt" in req.body) {
                beginsAt = parseDate(req.body.beginsAt);

                if (!beginsAt) {
                    res.throw(400, "Некорректная дата начала мероприятия");
                }
            } else {
                beginsAt = null;
            }

            if ("endsAt" in req.body) {
                endsAt = parseDate(req.body.endsAt);

                if (!endsAt) {
                    res.throw(400, "Некорректная дата конца мероприятия");
                    return;
                } else if (beginsAt && dateLess(endsAt, beginsAt, false)) {
                    res.throw(
                        400,
                        "Дата конца меньше или совпадает с датой начала мероприятия"
                    );
                    return;
                }
            } else {
                endsAt = null;
            }
        }

        const map =
            req.body.recipients === true
                ? usersMap
                : explicit
                    ? req.body.recipients
                    : null;

        if (map === null) {
            res.throw(400, "Предоставьте получателей объявления.");
            return;
        }

        if (explicit) {
            const compFunction = (a, b) => a - b;

            for (const roleId in map) {
                if (roleId in usersMap) {
                    sort(map[roleId], compFunction);

                    for (const classId of map[roleId]) {
                        if (
                            search(usersMap[roleId], parseInt(classId, 10), compFunction) < 0
                        ) {
                            res.throw(
                                403,
                                "Вы не можете отправлять объвления таким получателям."
                            );
                            return;
                        }
                    }
                } else {
                    res.throw(
                        403,
                        "Вы не можете отправлять объвления таким получателям."
                    );
                    return;
                }
            }
        }

        const { announcementId, recipients } = await mg.knex.transaction((t) =>
            mg
                .query("announcement")
                .create({
                    text: req.body.text,
                    authorId: req.user.id,
                    isEvent,
                    beginsAt: beginsAt || null,
                    endsAt: endsAt || null,
                })
                .then((announcement) => {
                    const recipientsArray = new Array();

                    for (const roleId in map) {
                        const numberRoleId = parseInt(roleId, 10);

                        for (const classId of map[roleId]) {
                            recipientsArray.push({
                                announcementId: announcement[0],
                                roleId: numberRoleId,
                                classId,
                            });
                        }
                    }

                    return t
                        .insert(recipientsArray)
                        .into("announcementClassRole")
                        .then(() => ({
                            announcementId: announcement[0],
                            recipients: recipientsArray,
                        }));
                })
        );

        const userIds = new Set();

        for (const recipient of recipients) {
            for (const role of mg.cache.roles) {
                const permission = getPermission(role.permissions, [
                    "announcement",
                    "create",
                ]);

                if (
                    permission === true ||
                    (Array.isArray(permission) &&
                        search(permission, recipient.roleId, function compare(a, b) {
                            return a - b;
                        }))
                ) {
                    const users = await mg.query("user").find(
                        {
                            classId: recipient.classId,
                        },
                        {},
                        ["id"]
                    );

                    for (const user of users) {
                        userIds.add(user.id);
                    }
                }
            }
        }

        const tokens = await mg.query("pushOptions").find(
            {
                userId_in: Array.from(userIds),
            },
            [],
            ["subscription", "gcm"]
        );

        const notificationTokens = [];

        for (let i = 0; i < tokens.length; i += 1) {
            if (tokens[i].gcm) {
                notificationTokens.push(tokens[i].subscription);
            }
        }

        const createdAt = new Date().toISOString();

        if (isEvent) {
            const beginsAtString = beginsAt ? beginsAt.toISOString() : "null";
            const endsAtString = endsAt ? endsAt.toISOString() : "null";

            await mg.fbAdmin
                .messaging()
                .sendMulticast({
                    data: {
                        text: req.body.text,
                        authorId: String(req.user.id),
                        id: String(announcementId),
                        createdAt,
                        isEvent: "true",
                        beginsAt: beginsAtString,
                        endsAt: endsAtString,
                    },
                    tokens: notificationTokens,
                })
                .catch((e) => {
                    console.log(e);
                });

            if (beginsAt) {
                const notify = () =>
                    mg.fbAdmin
                        .messaging()
                        .sendMulticast({
                            data: {
                                type: "startsSoon",
                                text: req.body.text,
                                authorId: String(req.user.id),
                                id: String(announcementId),
                                createdAt,
                                beginsAt: beginsAtString,
                                endsAt: endsAtString,
                            },
                            tokens: notificationTokens,
                        })
                        .catch((e) => {
                            console.log(e);
                        });

                const pushTime = new Date(beginsAt);
                const now = UTC();
                pushTime.setTime(pushTime.getTime() - 3600000);

                const difference = pushTime.getTime() - now.getTime();

                if (difference >= 0) setTimeout(notify, difference);
            }
        } else {
            await mg.fbAdmin
                .messaging()
                .sendMulticast({
                    data: {
                        text: req.body.text,
                        authorId: String(req.user.id),
                        id: String(announcementId),
                        createdAt,
                        isEvent: "false",
                    },
                    tokens: notificationTokens,
                })
                .catch((e) => {
                    console.log(e);
                });
        }

        res.send({});
    },
};
