require('dotenv').config();

const Koa = require('koa'),
      app = new Koa(),
      knex = require('knex')({
        client: process.env.DB_CLIENT,
        connection: {
          host: process.env.DB_HOST,
          user: process.env.DB_USER,
          password: process.env.DB_PASSWORD,
          database: process.env.DB_DATABASE,
          charset: process.env.DB_CHARSET
        }
      }),
      bookshelf = require('bookshelf')(knex);

app.use((ctx, next) => {
  const start = new Date();
  next().then(() => {
    const ms = Date.now() - start.getTime();
    console.log(
      `${start.toISOString()} | ${ctx.method} request on ${ctx.url}` +
      'took ' + ms + ' milliseconds'
    );
  });
});

app.use(ctx => {
  ctx.body = {
    message: 'Hello, World!'
  };
});

app.listen(1856);