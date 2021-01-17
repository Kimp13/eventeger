import fs from 'fs';

const functionConstructor = (name) =>
  (path, options) => new Promise((resolve, reject) => {
    fs[name](path, options, (err, data) => {
      if (err) reject(err);
      else resolve(data);
    });
  });

const functionConstructorNoOptions = (name) =>
  (path) => new Promise((resolve, reject) => {
    fs[name](path, (err, data) => {
      if (err) reject(err);
      else resolve(data);
    });
  });

export const readdir = functionConstructor('readdir');

export const mkdir = functionConstructor('mkdir');

export const readFile = functionConstructor('readFile');

export const access = functionConstructor('access');

export const unlink = functionConstructorNoOptions('unlink');

export const writeFile = (path, data, options) => new Promise((resolve, reject) => {
  fs.writeFile(path, data, options, (err) => {
    if (err) reject(err);
    else resolve();
  });
});