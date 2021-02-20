export default {
    async find(req, res) {
        const id = parseInt(req.query.announcementId, 10);

        if (isNaN(id)) {
            res.throw(400, "Некорректный идентификатор объявления");
            return;
        }

        const announcement = mg.query('announcement').findOne({
            id
        });

        if (!(await mg.query.announcement.isAvailable(
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
            .select("parent.*")
            .count("children.id as childrenCount")
            .from("comment as parent")
            .innerJoin(
                "comment as children",
                "children.replyTo",
                "parent.id"
            )
            .where("parent.announcementId", announcementId);

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
                .andWhere("replyTo", replyToId);
        }

        if (announcement.authorId !== req.user.id) {
            comments = comments
                .andWhere(builder =>
                    builder
                        .where("parent.authorId", req.user.id)
                        .orWhere("parent.hidden", false)
                );
        }

        comments = await comments
            .groupBy("parent.id")
            .offset(offset)
            .limit(25);

        console.log(comments);

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
            .count("child.id as childrenCount")
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
        if (!req.query.text) {
            res.throw(400, "Нет текста объявления");
            return;
        }

        const text = String(req.query.text);

        if (!text) {
            res.throw(400, "Некорректный текст объявления");
            return;
        }

        const announcementId = parseInt(req.query.announcementId, 10);

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
            hidden: Boolean(req.query.hidden)
        };

        if (req.query.replyTo) {
            const replyToId = parseInt(req.query.replyTo, 10);

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

        await mg.query('comment').create(createObj);
    }
}