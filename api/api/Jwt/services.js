const jwt = require('jsonwebtoken');
const _ = require('lodash');
const mg = global.mg;

module.exports = {
  issue: (payload, jwtOptions = {}) => {
    return jwt.sign(
      _.clone(payload),
      _.get(mg, ['config', 'jwtSecret']),
      _.defaults(jwtOptions, _.get(mg, ['config', 'jwtConfig']))
    );
  },
  verify: token => {
    return new Promise((resolve, reject) => {
      jwt.verify(
        token,
        _.get(mg, ['config', 'jwtSecret']),
        {},
        (err, tokenPayload = {}) => {
          if (err) {
            resolve(null);
          }

          resolve(tokenPayload);
        }
      );
    });
  }
};