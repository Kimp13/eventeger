import bcrypt from "bcrypt";
import pick from "lodash/pick";
import get from "lodash/get";
import parsePermissions from "permissionArrayToObject";

export default {
  find: async (req, res) => {
    if (Array.isArray(req.query.id)) {
      for (let i = 0; i < req.query.id.length; i += 1) {
        req.query.id[i] = parseInt(req.query.id[i], 10);

        if (isNaN(req.query.id[i])) {
          res.throw(400, []);
          return;
        }
      }

      res.send(await mg.query("user").find(
        { id_in: req.query.id },
        [],
        [
          "id",
          "firstName",
          "lastName",
          "roleId",
          "classId"
        ]
      ));
      return;
    } else {
      const id = parseInt(req.query.id, 10);

      if (!isNaN(id)) {
        res.send(await mg.query("user").findOne({
          id
        }, [], [
          "id",
          "firstName",
          "lastName",
          "roleId",
          "classId"
        ]));
        return;
      }
    }

    res.throw(400);
    return;
  },

  count: async (_, res) => {
    res.send({
      count: mg.cache.usersCount
    });
  },

  me: async (req, res) => {
    res.send({
      jwt: "",
      data: {
        firstName: req.user.firstName,
        lastName: req.user.lastName,
        username: req.user.username,
        id: req.user.id,
        roleId: req.user.roleId,
        classId: req.user.classId,
        permissions: req.user.permissions
      }
    });
  },

  signUp: async (req, res) => {
    const {
      username,
      password,
      firstName = null,
      lastName = null
    } = req.body;

    if (
      !password ||
      password.length < 8 ||
      /[^0-9a-zA-Z#$^*_]/.test(username)
    ) {
      res.throw(400, "Некорректное имя пользователя или пароль");
      return;
    }

    const count = await mg.knex('user').count('*');

    if (count === 0) {
      const hash = await bcrypt.hash(password, 10);
      const user = await mg.knex
        .select("*")
        .from("user")
        .where("id", (
          await mg.knex("user")
            .insert({
              username,
              firstName,
              lastName,
              password: hash
            })
        )[0]);

      const jwt = mg.services.jwt.issue({
        id: user.attributes.id
      });

      if (jwt) {
        res.statusCode = 200;
        res.end(JSON.stringify({
          user: Object.assign({
            isAuthenticated: true
          }, pick(user, [
            "first_name",
            "last_name",
            "username",
            "permissions",
            "role_id",
            "class_id"
          ])),
          jwt
        }));

        return;
      }

      console.log(`Jwt test failed! It's ${jwt}`);

      res.throw(500, "Внутренняя ошибка сервера");
    } else {

    }
  },

  signIn: async (req, res) => {
    const { username, password } = req.body;

    if (
      password &&
      username &&
      password.length >= 8 &&
      !/[^0-9a-zA-Z#$*_]/.test(username)
    ) {
      const user = await mg.query("user").findOne({
        username
      }, ["role.permission"]);

      if (
        user &&
        await bcrypt.compare(password, user.password.toString())
      ) {
        const jwt = mg.services.jwt.issue({
          id: user.id
        });

        user.permissions = parsePermissions(get(
          user,
          [
            "_relations",
            "role",
            "_relations",
            "permission"
          ]
        ));

        if (user.roleId) {
          let insertNeeded = false;

          if (!(user.roleId in mg.cache.roleClassMap)) {
            mg.cache.roleClassMap[user.roleId] = new Set([user.classId]);
            insertNeeded = true;
          } else if (!mg.cache.roleClassMap[user.roleId].has(user.classId)) {
            mg.cache.roleClassMap[user.roleId].add(user.classId);
            insertNeeded = true;
          }

          if (insertNeeded)
            await mg.knex.insert({
              classId: user.classId,
              roleId: user.roleId
            }).into("class_role");
        }

        res.send({
          jwt,
          data: pick(user, [
            "firstName",
            "lastName",
            "username",
            "permissions",
            "roleId",
            "classId"
          ])
        });
        return;
      }

      res.throw(401, "Неправильный логин или пароль");
      return;
    }

    res.statusCode = 400;
    res.end("{}");

    return;
  }
};