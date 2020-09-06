module.exports = {
  tableName: 'permission',
  hasTimestamps: false,
  roles: () => this.belongsToMany('Role')
};