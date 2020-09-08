const bcrypt = require('bcrypt');
const mg = global.mg;

module.exports = {
  find: async (req, res) => {
    res.end('OK');

    return;
  },

  count: async (req, res) => {
    res.status = 200;
    res.end(JSON.stringify({
      count: mg.cache.usersCount
    }));

    return;
  },

  signUp: async (req, res) => {
    const { username, password } = req.body;

    if (
      /[^0-9a-zA-Z#$*_]/.test(username) ||
      password.length < 8
    ) {
      res.status = 400;
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
          res.status = 200;
          res.end(JSON.stringify({
            jwt
          }));

          mg.cache.usersCount += 1;

          return;
        }

        console.log(`Jwt test failed! It's ${jwt}`);

        res.status = 500;
      } else {

      }
    } catch (e) {
      console.log(e);

      res.status = 500;

      return;
    }
  },

  signIn: async (req, res) => {
    const { username, password } = req.body;
  }
};