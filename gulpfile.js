const gulp = require("gulp"),
  util = require("gulp-util"),
  notifier = require("node-notifier"),
  child = require("child_process"),
  os = require("os"),
  path = require("path");

var server = "null";

function build() {
  var build = child.spawn("go", ["install"]);

  build.stdout.on("data", (data) => {
    console.log(`stdout: ${data}`);
  });

  build.stderr.on("data", (data) => {
    console.error(`stderr: ${data}`);
  });

  return build;
}

function spawn(done) {
  if (server && server != "null") {
    server.kill();
  }

  var path_folder = process.cwd().split(path.sep);
  var length = path_folder.length;
  var app = path_folder[length - parseInt(1)];

  if (os.platform() == "win32") {
    server = child.spawn(app + ".exe");
  } else {
    server = child.spawn(app);
  }

  server.stdout.on("data", (data) => {
    console.log(`stdout: ${data}`);
  });

  server.stderr.on("data", (data) => {
    console.log(`stderr: ${data}`);
  });

  done();
}

const serve = gulp.series(build, spawn);
function watch(done) {
  gulp.watch(["*.go", "**/*.go", "schema.graphql"], serve);
  done();
}

exports.serve = serve;
exports.watch = watch;
exports.default = gulp.parallel(serve, watch);
