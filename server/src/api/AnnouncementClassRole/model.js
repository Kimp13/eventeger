export default {
    tableName: 'announcementClassRole',

    columns: {
        
    },

    relations: [
        {
            with: 'announcement',
            type: 'many:one'
        },

        {
            with: 'class',
            type: 'many:one'
        },

        {
            with: 'role',
            type: 'many:one'
        }
    ]
}