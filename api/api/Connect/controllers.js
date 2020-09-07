const { pick } = require('lodash');
const mg = global.mg;

module.exports = async ctx => {
  const jwt = ctx.cookies.get('jwt');

  if (jwt) {
    const payload = await mg.services.jwt.verify(jwt);

    if (payload) {
      const user = await mg.models.User.where({
        id: payload.id
      }).fetch();

      console.log(await user.related('role').fetch());

      if (user) {
        ctx.body = {
          user: Object.assign({
            isAuthenticated: true
          }, pick(user.attributes, ['id', 'firstName', 'lastName', 'username']))
        }

        return;
      }
    }
  }

  ctx.body = {
    user: {
      isAuthenticated: false
    }
  };

  return;
};