module.exports = {
  tableName: 'permission',
  idAttribute: 'id',
  hasTimestamps: false,
  roles: function() {
    return this.belongsToMany('Role');
  }
};