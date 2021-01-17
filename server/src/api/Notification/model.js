export default {
    tableName: 'pushOptions',

    columns: {
        subscription: {
            type: 'text',
            notNull: true
        },

        expiresAt: {
            type: 'datetime'
        }
    },

    relations: [
        {
            with: 'user',
            type: 'many:one'
        }
    ]
};