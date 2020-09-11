const pick = require('lodash/pick');
const { set } = require('lodash');

module.exports = permissionsArray => {
  let permissionsObject = new Object();

  for (let permission of permissionsArray) {
    set(permissionsObject, permission.name.split('_'), true);
  }

  return permissionsObject;
}