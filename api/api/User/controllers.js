const bcrypt = require('bcrypt');
const mg = global.mg;

module.exports = {
  find: async ctx => {
    return;
  },

  count: async ctx => {
    ctx.status = 200;
    ctx.body = {
      count: mg.cache.usersCount
    };

    return;
  },

  signUp: async ctx => {
    const { username, password } = ctx.request.body;

    if (
      /[^0-9a-zA-Z#$*_]/.test(username) ||
      password.length < 8
    ) {
      ctx.status = 400;
      return;
    }

    try {
      if (mg.cache.usersCount === 0) {
        const hash = await bcrypt.hash(password, 10);  
        const user = await (new mg.models.User({
          username,
          password: hash,
          role_id: 1
        }).save());

        const jwt = mg.services.jwt.issue({
          id: user.attributes.id
        });

        if (jwt) {
          ctx.status = 200;
          ctx.body = {
            jwt
          };

          mg.cache.usersCount += 1;

          return;
        }

        console.log(`Jwt test failed! It's ${jwt}`);

        ctx.status = 500;
      } else {

      }
    } catch (e) {
      console.log(e);

      ctx.status = 500;

      return;
    }
  },

  signIn: async ctx => {
    const { username, password } = ctx.request.body;
  }
};