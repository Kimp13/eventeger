module.exports = () => {
  return new Promise((resolve, reject) => {
    console.log('Bootstrap function loaded!');
    resolve();
  });
};