module.exports = {
  preprocess: require('svelte-preprocess')({
    scss: {
      includePaths: ['src'],
    },
    postcss: {
      plugins: [require('autoprefixer')],
    },
  }),
};