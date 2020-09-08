const pick = require('lodash/pick');
const getUser = require('../../../utils/getUser');
const mg = global.mg;

module.exports = async (req, res) => {
  console.log(req.cookies);
  const user = await getUser(req.cookies.jwt);

  if (user) {
    res.status = 200;
    res.end(JSON.stringify({
      user: Object.assign(
        { isAuthenticated: true },
        pick(user, ['first_name', 'last_name', 'username', 'permissions'])
      )
    }));

    return;
  }

  res.end(JSON.stringify({
    user: {
      isAuthenticated: false
    }
  }));

  res.end();
  return;
};