module.exports = {
  tableName: 'role',
  columns: {
    type: {
      type: 'string',
      length: 128,
      notNull: true
    },

    name: {
      type: 'string',
      length: 128,
      notNull: true
    }
  },

  relations: [
    {
      type: 'many:many',
      with: 'permission'
    },
    {
      type: 'many:many',
      with: 'class'
    },
    {
      type: 'one:many',
      with: 'user'
    }
  ]
};