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
            let offset;

            if ("offset" in req.query) {
                offset = parseInt(req.query.offset, 10);

                if (isNaN(offset)) {
                    res.throw(400, "Некорректный отступ");
                    return;
                }
            } else {
                offset = 0;
            }

            const rolesIds = getPermission(
                req.user.permissions,
                ["announcement", "read"]
            );

            if (rolesIds === false) {
                res.throw(403, "У Вас нет прав на чтение объявлений");
                return;
            }

            const classesIds = getPermission(
                req.user.permissions,
                ["class", "multiple"]
            );

            if (classesIds === false && req.user.classId === null) {
                res.throw(403, "У Вас нет прав на чтение объявлений");
                return;
            }

            let announcements = mg.knex
                .select("announcement.*")
                .distinct("announcement.id")
                .from("announcement")
                .innerJoin(
                    "announcementClassRole",
                    "announcementClassRole.announcementId",
                    "announcement.id"
                );

            if (Array.isArray(classesIds)) {
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

            announcements = await announcements
                .orderBy("announcement.id", "desc")
                .offset(offset)
                .limit(25);

            const announcementsIds = [];
            const authoredAnnouncementsIds = [];

            for (let i = 0; i < announcements.length; i++) {
                announcementsIds.push(announcements[i].id);

                if (announcements[i].authorId === req.user.id) {
                    authoredAnnouncementsIds.push(announcements[i].id);
                }
            }

            const comments = await mg.knex
                .select("comment.announcementId")
                .count("comment.id as count")
                .from("comment")
                .whereIn("comment.announcementId", announcementsIds)
                .andWhere("comment.replyTo", null)
                .andWhere(function () {
                    this.where("comment.hidden", 0)
                        .orWhere("comment.authorId", req.user.id)
                        .orWhereIn(
                            "comment.announcementId",
                            authoredAnnouncementsIds
                        )
                })
                .orderBy("comment.announcementId", "desc")
                .groupBy("comment.announcementId");

            let i = 0, j = 0;

            for (;
                i < announcements.length && j < comments.length;
                i++) {
                if (announcements[i].id === comments[j].announcementId) {
                    announcements[i].commentsCount = comments[j++].count;
                } else {
                    announcements[i].commentsCount = 0;
                }

                announcements[i].isEvent = announcements[i].isEvent === 1;
            }

            for (;
                i < announcements.length;
                i++) {
                announcements[i].commentsCount = 0;

                announcements[i].isEvent = announcements[i].isEvent === 1;
            }

            res.send(announcements);
        }
    },

    lastEvents: async (req, res) => {
        let offset;

        if ("offset" in req.query) {
            offset = parseInt(req.query.offset, 10);

            if (isNaN(offset)) {
                res.throw(400, "Некорректный отступ");
                return;
            }
        } else {
            offset = 0;
        }

        const rolesIds = getPermission(req.user.permissions, ["announcement", "read"]);

        if (rolesIds === false) {
            res.throw(403, "У Вас нет прав на чтение объявлений");
            return;
        }

        const classesIds = getPermission(req.user.permissions, ["class", "multiple"]);

        if (classesIds === false && req.user.classId === null) {
            res.throw(403, "У Вас нет прав на чтение объявлений");
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
            .where(builder =>
                builder.where("beginsAt", "!=", null).orWhere("endsAt", "!=", null)
            );

        if (Array.isArray(classesIds)) {
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

        res.send(
            await announcements
                .offset(offset)
                .limit(25)
        );
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
                        if (search(
                            usersMap[roleId],
                            parseInt(classId, 10),
                            compFunction
                        ) < 0) {
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

        const { announcementId } = await mg.knex.transaction((t) =>
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
                            announcementId: announcement[0]
                        }));
                })
        );

        const keys = Object.keys(map);
        const _userIds = await mg.knex
            .select('id')
            .from('user')
            .where(function () {
                this
                    .whereIn('roleId', mg.cache.roleSuitable[keys[0]])
                    .andWhere(function () {
                        for (const classId of map[keys[0]]) {
                            this
                                .orWhere('classId', classId)
                                .orWhereIn(
                                    'roleId',
                                    mg.cache.classSuitable[keys[0]]
                                );
                        }
                    });

                for (let i = 1; i < keys.length; i++) {
                    this
                        .orWhereIn('roleId', mg.cache.roleSuitable[keys[i]])
                        .andWhere(function () {
                            for (const classId of map[keys[i]]) {
                                this
                                    .orWhere('classId', classId)
                                    .orWhereIn(
                                        'roleId',
                                        mg.cache.classSuitable[keys[i]]
                                    );
                            }
                        });
                }
            });
        const userIds = [];

        for (let i = 0; i < _userIds.length; i++)
            userIds.push(_userIds[i].id);

        const tokens = await mg.query("pushOptions").find(
            {
                userId_in: userIds,
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
