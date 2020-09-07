module.exports = {
  tableName: 'role',
  idAttribute: 'id',
  hasTimestamps: false,
  permissions: function() {
    return this.belongsToMany('Permission');
  },
  users: function() {
    return this.hasMany('User');
  }
};