module.exports = {
  tableName: 'permission',
  hasTimestamps: false,
  roles: function() {
    return this.belongsToMany('Role');
  }
};