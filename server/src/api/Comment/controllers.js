import get from 'lodash/get';
import omit from 'lodash/omit';
import { parseDate } from 'chrono';

export default {
    async find(req, res) {
        const id = parseInt(req.query.announcementId, 10);

        if (!isNaN(id)) {
            const announcement = mg.query('announcement').findOne({
                id
            });

            if (await mg.query.announcement.isAvailable(
                announcement,
                req.user
            )) {
                const queryObj = (
                    announcement.authorId === req.user.id ?
                        {} :
                        {
                            hidden: false
                        }
                );

                queryObj.announcementId = id;
                queryObj._limit = 25;

                if (req.query.last) {
                    const date = parseDate(req.query.last);

                    if (!date) {
                        res.throw(400, "Некорректный последний комментарий");
                        return;
                    }

                    queryObj.createdAt_lt = date;
                    queryObj._skip = 1;
                }

                if (req.query.replyTo) {
                    const replyToId = parseInt(req.query.replyTo, 10);

                    if (isNaN(replyToId)) {
                        res.throw(400, "Некорректный id комментария-родителя");
                        return;
                    }

                    const replyTo = await mg.query('comment').findOne({
                        id: replyToId
                    });

                    if (!replyTo || replyTo.announcementId !== announcement.id) {
                        res.throw(400, "Некорректный комментарий-родитель");
                        return;
                    }

                    queryObj.replyTo = replyToId;
                }

                req.send(
                    await mg.query('comment').find(
                        queryObj
                    )
                );
                return;
            }

            req.throw(403, "Вам недоступно это объявление");
            return;
        }

        req.throw(400, "Некорректный id объявления");
    },

    async findOne(req, res) {
        const id = parseInt(req.query.id, 10);

        if (!isNaN(id)) {
            const comment = await mg.query("comment").findOne({
                id
            }, ["announcement"]);

            if (await mg.services.announcement.isAvailable(
                get(comment, ["_related", "announcement"]),
                req.user
            )) {
                if (
                    comment.authorId === req.user.id ||
                    !comment.hidden
                ) {
                    res.send(omit(
                        comment,
                        "_related"
                    ));
                    return;
                }
            }

            res.throw(403, "Вам недоступен этот комментарий.");
            return;
        }

        res.throw(400, "Некорректное поле id");
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