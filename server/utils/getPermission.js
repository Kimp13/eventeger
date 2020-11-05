module.exports = (permissions, required) => {
  if (permissions['*']) return true;

  let i = 0;
  let current = permissions[required[i++]];

  while (required[i] && Boolean(current) && current.constructor === Object) {
    current = current[required[i++]];
  }

  return current;
};