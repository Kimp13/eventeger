import glob from 'glob';

export default pattern => new Promise((resolve, reject) => {
  glob(pattern, (err, files) => {
    if (err) reject(err);
    else resolve(files);
  });
});