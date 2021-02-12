module.exports = {
  tableName: 'announcement',
  hasTimestamps: true,

  columns: {
    text: {
      type: 'text',
      notNull: true
    },

    beginsAt: {
      type: 'datetime'
    },

    endsAt: {
      type: 'datetime'
    }
  },

  relations: [
    {
      with: 'user',
      type: 'many:one',
      column: 'authorId'
    },
  ]
}