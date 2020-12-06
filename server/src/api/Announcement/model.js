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
      column: 'author_id'
    },

    {
      with: 'class',
      type: 'many:one',
      column: 'watafuk_fuk_me'
    }
  ]
}