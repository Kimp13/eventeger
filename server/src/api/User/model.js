module.exports = {
  tableName: 'user',
  hasTimestamps: true,
  role: function() {
    return this.belongsTo('Role');
  }
};