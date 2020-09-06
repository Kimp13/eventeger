const mg = global.mg;

module.exports = {
  find: async ctx => {
    return;
  },
  count: async ctx => {
    ctx.status = 200;
    ctx.body = {
      count: await mg.models.User.count()
    };

    return;
  }
};