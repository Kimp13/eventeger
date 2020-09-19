const getUser = require("../../../utils/getUser");
const has = require("lodash/indexOf");

module.exports = async (req, res) => {
  if (req.body.token) {
    const user = await getUser(req.headers.authentication);

    if (user) {
      if (mg.cache.usersTokens.hasOwnProperty(user.id)) {
        if (has(mg.cache.usersTokens[user.id], req.body.token) === -1) {
          mg.cache.usersTokens[user.id].push(
            req.body.token
          );
        }
      } else {
        mg.cache.usersTokens[user.id] = [
          req.body.token
        ];
      }
  
      res.statusCode = 200;
      res.end('{}');
      return;
    }

    res.statusCode = 401;
    res.end('{}');
    return;
  }

  res.statusCode = 400;
  res.end('{}');
  return;
};