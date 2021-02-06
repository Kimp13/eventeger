module.exports = {
  tableName: 'announcement',
  hasTimestamps: true,

  columns: {
    text: {
      type: 'text'
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