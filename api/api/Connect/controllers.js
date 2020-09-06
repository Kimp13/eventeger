const mg = global.mg;

module.exports = ctx => {
  let jwt = ctx.cookies.get('jwt');

  if (jwt) {
    
  }

  ctx.body = {
    redirect: true
  };

  return;
};