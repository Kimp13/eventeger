const bs = require('binary-search');

module.exports = async jwt => {
  if (jwt) {
    const payload = await mg.services.jwt.verify(jwt);

    if (payload) {
      const user = await mg.query('user').findOne({
        id: payload.id
      });

      user.permissions = mg.cache.roles[bs(
        mg.cache.roles,
        user.roleId,
        function comparator(element, needle) {
          return element.id - needle;
        }
      )].permissions;

      return user;
    }
  }

  return null;
};