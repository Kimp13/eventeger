const _ = require('lodash');

module.exports = {
  tableName: 'user',
  idAttribute: 'id',
  hasTimestamps: true,
  role: function() {
    return this.belongsTo('Role');
  },
  parse: attributes => {
    if (attributes.password instanceof Buffer) {
      attributes.password = attributes.password.toString();
    }

    return _.mapKeys(attributes, (value, key) => {
      return _.camelCase(key);
    });
  }
};