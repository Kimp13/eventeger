module.exports = {
  preprocess: sveltePreprocess({
    scss: {
      includePaths: ['src'],
    },
    postcss: {
      plugins: [require('autoprefixer')],
    },
  }),
};