export default {
    async find(req, res) {
        const id = parseInt(req.query.announcementId, 10);

        if (isNaN(id)) {
            res.throw(400, "Некорректный идентификатор объявления");
            return;
        }

        const announcement = await mg.query('announcement').findOne({
            id
        });

        if (!(await mg.services.announcement.isAvailable(
            announcement,
            req.user
        ))) {
            res.throw(403, "Вам недоступно это объявление");
            return;
        }

        let offset;

        if ('offset' in req.query) {
            offset = parseInt(req.query.offset, 10);

            if (isNaN(offset)) {
                res.throw(400, "Некорректный отступ");
                return;
            }
        } else {
            offset = 0;
        }

        let comments = mg.knex
            .select("comment.*")
            .distinct("comment.id")
            .from("comment")
            .where("comment.announcementId", id);

        if (req.query.replyTo) {
            const replyToId = parseInt(req.query.replyTo, 10);

            if (isNaN(replyToId)) {
                res.throw(400, "Некорректный идентификатор комментария-родителя");
                return;
            }

            const replyTo = await mg.query('comment').findOne({
                id: replyToId
            });

            if (!replyTo || replyTo.announcementId !== announcement.id) {
                res.throw(400, "Некорректный комментарий-родитель");
                return;
            }

            if (
                replyTo.hidden &&
                replyTo.authorId !== req.user.id &&
                announcement.authorId !== req.user.id
            ) {
                res.throw(403, "Вы не имеете доступа к комментарию-родителю");
                return;
            }

            comments = comments
                .andWhere("comment.replyTo", replyToId);
        } else {
            comments = comments.andWhere("comment.replyTo", null);
        }

        if (announcement.authorId !== req.user.id) {
            comments = comments
                .andWhere(builder =>
                    builder
                        .where("comment.authorId", req.user.id)
                        .orWhere("comment.hidden", false)
                );
        }

        comments = await comments
            .orderBy("id", "desc")
            .limit(25)
            .offset(offset);

        const commentsIds = [];

        for (let i = 0; i < comments.length; i++) {
            commentsIds.push(comments[i].id);
        }

        const replies = await mg.knex
            .select("replyTo")
            .count("* as count")
            .from("comment")
            .whereIn("replyTo", commentsIds)
            .orderBy("replyTo", "desc")
            .groupBy("replyTo");

        let i = 0, j = 0;

        for (;
            i < comments.length && j < replies.length;
            i++) {
            if (comments[i].id === replies[j].replyTo) {
                comments[i].commentsCount = replies[j++].count;
            } else {
                comments[i].commentsCount = 0;
            }
        }

        for (;
            i < comments.length;
            i++) {
            comments[i].commentsCount = 0;
        }

        res.send(comments);
    },

    async findOne(req, res) {
        const id = parseInt(req.query.id, 10);

        if (isNaN(id)) {
            res.throw(400, "Некорректный идентификатор");
            return;
        }

        const comment = (await mg.knex
            .select("parent.*")
            .count("child.id as commentCount")
            .from("comment as parent")
            .innerJoin(
                "comment as children",
                "children.replyTo",
                "parent.id"
            )
            .groupBy("parent.id"))[0];

        if (!comment) {
            res.throw(400, "Комментария не существует");
            return;
        }

        const announcement = await mg.query('announcement').findOne({
            id: comment.announcementId
        });

        if (!(
            await mg.services.announcement.isAvailable(
                announcement,
                req.user
            ))) {
            res.throw(403, "Вам недоступно это объявление");
            return;
        }

        if (
            comment.hidden &&
            comment.authorId !== req.user.id &&
            announcement.authorId !== req.user.id
        ) {
            res.throw(403, "Вам недоступен этот комментарий");
            return;
        }

        res.send(comment);
    },

    async create(req, res) {
        if (!req.body.text) {
            res.throw(400, "Нет текста объявления");
            return;
        }

        const text = String(req.body.text);

        if (!text) {
            res.throw(400, "Некорректный текст объявления");
            return;
        }

        const announcementId = parseInt(req.body.announcementId, 10);

        if (isNaN(announcementId)) {
            res.throw(400, "Некорректный id объявления");
            return;
        }

        const announcement = await mg.query('announcement').findOne({
            id: announcementId
        });

        if (!(await mg.services.announcement.isAvailable(
            announcement,
            req.user
        ))) {
            res.throw(403, "Вам недоступно это объявление");
            return;
        }

        const createObj = {
            text,
            hidden: Boolean(req.body.hidden),
            announcementId,
            authorId: req.user.id
        };

        if (req.body.replyTo) {
            const replyToId = parseInt(req.body.replyTo, 10);

            if (isNaN(replyToId)) {
                res.throw(400, "Некорректный id комментария-родителя");
                return;
            }

            const replyTo = await mg.query('comment').findOne({
                id: replyToId
            });

            if (replyTo.announcementId != announcementId) {
                res.throw(400, "Некорректный комментарий-родитель");
                return;
            }

            if (replyTo.hidden && announcement.authorId !== req.user.id) {
                res.throw(403, "Вам недоступен комментарий-родитель");
                return;
            }

            createObj.replyTo = replyToId;
        }

        const something = await mg.query('comment').create(createObj);

        res.send(await mg.query('comment').findOne({
            id: something[0]
        }));
    }
}