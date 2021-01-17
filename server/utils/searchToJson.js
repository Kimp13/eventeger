const e = require("express");

module.exports = url => {
  if (typeof url === 'string') {
    const questionMarkIndex = url.indexOf('?');
    const result = {};

    if (questionMarkIndex === -1) {
      url = decodeURI(url);
    } else {
      url = decodeURI(url.substring(questionMarkIndex + 1));
    }

    for (const argument of url.split('&')) {
      const parts = argument.split('=');

      result[parts[0]] = parts[1];
    }

    return result;
  }
  
  return {};
}