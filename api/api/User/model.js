module.exports = {
  tableName: 'user',
  hasTimestamps: true,
  hidden: ['password'],
  role: () => this.belongsTo('role')
};