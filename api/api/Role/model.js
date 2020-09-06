module.exports = {
  tableName: 'role',
  hasTimestamps: false,
  permissions: () => this.belongsToMany('Permission')
};