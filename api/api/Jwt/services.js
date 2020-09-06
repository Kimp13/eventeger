const jwt = require('jsonwebtoken');
const _ = require('lodash');
const mg = global.mg;

module.exports = {
  issue: (payload, jwtOptions = {}) => jwt.sign(
    _.clone(payload),
    _.get(mg, ['config', 'jwtSecret']),
    _.defaults(jwtOptions, pick(mg, ['config', 'jwt']))
  ),
  verify: token => {
    return new Response((resolve, reject) => {
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