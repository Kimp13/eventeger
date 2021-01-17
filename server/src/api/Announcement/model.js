module.exports = {
  tableName: 'announcement',
  hasTimestamps: true,

  columns: {
    text: {
      type: 'text'
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