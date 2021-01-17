module.exports = {
  tableName: 'permission',
  columns: {
    type: {
      type: 'string',
      length: 64,
      notNull: true
    },

    operation: {
      type: 'string',
      length: 64
    },

    target: {
      type: 'string'
    }
  },

  relations: [
    {
      type: 'many:many',
      with: 'role'
    }
  ]
};