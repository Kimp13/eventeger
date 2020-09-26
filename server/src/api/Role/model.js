module.exports = {
  tableName: 'role',
  hasTimestamps: false,
  permissions: function() {
    return this.belongsToMany('Permission');
  },
  users: function() {
    return this.hasMany('User');
  }
};