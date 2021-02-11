export default {
    tableName: "comment",
    hasTimestamps: true,

    columns: {
        text: {
            type: "text",
            notNull: true
        },

        hidden: {
            type: "boolean",
            notNull: true
        }
    },

    relations: [
        {
            type: "many:one",
            with: "user",
            column: "authorId",
            notNull: true
        },
        {
            type: "many:one",
            with: "announcement",
            notNull: true
        },
        {
            type: "many:one",
            with: "comment",
            column: "replyTo"
        }
    ]
}