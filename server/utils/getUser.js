const bs = require('binary-search');

module.exports = async jwt => {
  if (jwt) {
    const payload = await mg.services.jwt.verify(jwt);

    if (payload) {
      const user = await mg.query('user').findOne({
        id: payload.id
      });

      const role = mg.cache.roles[user.roleId];

      user.permissions = role ? role.permissions : {};

      return user;
    }
  }

  return null;
};