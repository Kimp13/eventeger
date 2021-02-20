// .env
import dotenv from 'dotenv';

dotenv.config();

// server dependencies
import express from 'express';
import bodyParser from 'body-parser';
import cookie from 'cookie';
import compression from 'compression';
import http from 'http';
import colors from 'colors/safe';
import * as sapper from '@sapper/server';

// database
import Knex from 'knex';
import addModels, * as db from '../database';

// filesystem
import path from 'path';
import { readFile, access, readdir } from 'filesystem';
import fs from 'fs';

// utilities
import _ from 'lodash';
import getUser from 'getUser';
import getRouteName from 'getRouteName';

// knex instance
const connobj = {
  client: process.env.DB_CLIENT || 'mysql',
};

if (!(process.env.DB_NO_CONNECTION || process.env.DB_CLIENT === 'sqlite')) {
  connobj.connection = {
    host: process.env.DB_HOST || '127.0.0.1',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || 'root',
    database: process.env.DB_DATABASE || 'main',
    charset: process.env.DB_CHARSET || 'utf8'
  };
}

const knex = new Knex(connobj);

// environment
const { PORT = 3000, NODE_ENV = 'development', API_URL = '/api' } = process.env;
const dev = NODE_ENV === 'development';

// working with filesystem
const configPath = path.resolve(process.cwd(), 'src', 'config');
const apiPath = path.resolve(process.cwd(), 'src', 'api');
const functionsPath = path.join(configPath, 'functions');
const environmentsPath = path.join(configPath, 'environments')
const bootstrapPath = path.join(functionsPath, 'bootstrap.js');
const policiesPath = path.join(functionsPath, 'policies');
const commonEnvironmentPath = path.join(environmentsPath, 'common.js');
const specialEnvironmentPath = path.join(environmentsPath,
  dev ? 'development.js' : 'production.js'
);
const chunksPath = path.resolve(
  process.cwd(),
  '__sapper__',
  dev ? 'dev' : 'build',
  'server'
);

let mgSpecs;
global.mg = {};
mg.knex = knex;

function _throw(statusCode = 400, errorMsg = "Error") {
  this.status(statusCode).send({
    error: {
      errorCode: statusCode,
      errorMessage: errorMsg
    }
  });
}

// conditional middleware
const parseBody = bodyParser.json({ extended: true });

/**
 * Main function of the server.
 */
const main = () => {
  // proceed if connected to database
  knex.raw('select 1 + 1 as testValue')
    .then(() => readdir(apiPath))
    .then(files => {
      mg.models = [];
      mg.paths = {};
      mg.services = {};
      mg.cache = {
        connectedUsers: {}
      };

      return Promise.all(
        files.map(file => new Promise((resolve, reject) => {
          const routeName = getRouteName(file);

          const modelPath = path.join(
            chunksPath,
            `route`
          )
          const promiseArray = [];

          if (mgSpecs.routes.hasOwnProperty(routeName)) {
            const routesPath = path.join(
              chunksPath,
              `routes-${mgSpecs.routes[routeName]}.js`
            );
            const controllersPath = path.join(
              chunksPath,
              `controllers-${mgSpecs.controllers[routeName]}.js`
            );

            promiseArray.push(
              access(routesPath)
                .then(() => {
                  const routes = require(routesPath);
                  const controllers = require(controllersPath);

                  for (let j = 0; j < routes.length; j += 1) {
                    routes[j].method = routes[j].method.toUpperCase();

                    if (
                      routes[j].path.charAt(
                        routes[j].path.length - 1
                      ) !== '/'
                    ) {
                      routes[j].path += '/';
                    }

                    const routePath = `/${routeName}${routes[j].path}`;

                    _.set(mg.paths, [
                      routes[j].method,
                      routePath,
                      'policies'
                    ], []);

                    if (
                      routes[j].hasOwnProperty('config') &&
                      routes[j].config.hasOwnProperty('policies')
                    ) {
                      for (const policy of routes[j].config.policies) {
                        mg
                          .paths
                        [routes[j].method]
                        [routePath]
                          .policies
                          .push(mg.policies[policy]);
                      }
                    }

                    _.set(
                      mg.paths,
                      [
                        routes[j].method,
                        routePath,
                        'handler'
                      ],
                      (
                        routes[j].handler === 'default' ?
                          controllers :
                          controllers[routes[j].handler]
                      )
                    );
                  }
                })
            );
          }

          if (mgSpecs.models.hasOwnProperty(routeName)) {
            const modelPath = path.join(
              chunksPath,
              `model-${mgSpecs.models[routeName]}.js`
            );

            promiseArray.push(
              access(modelPath)
                .then(() => {
                  const model = require(modelPath);

                  mg.models.push(model);
                })
            );
          }

          if (mgSpecs.services.hasOwnProperty(routeName)) {
            const servicesPath = path.join(
              chunksPath,
              `services-${mgSpecs.services[routeName]}.js`
            );

            promiseArray.push(
              access(servicesPath)
                .then(() => {
                  mg.services[_.camelCase(file)] = require(servicesPath);
                })
            );
          }

          Promise.all(promiseArray).then(resolve);
        }))
      );
    })
    .then(() => addModels(knex, mg.models))
    .then(() => {
      mg.query = db.query;
      mg.queryAll = db.queryAll;

      const app = express();

      app
        .use((req, res, next) => {
          req.cookies = cookie.parse(req.headers.cookie || '');

          next();
        })

      if (dev) {
        app.use(async (req, res, next) => {
          const start = new Date();

          res.on('finish', () => {
            const ms = colors.underline.bold(String(Date.now() - start.getTime()));
            const url = colors.cyan(req.originalUrl);
            const code = (
              res.statusCode >= 200 ?
                (
                  res.statusCode >= 300 ?
                    (
                      res.statusCode >= 400 ?
                        (
                          res.statusCode >= 500 ?
                            colors.red(res.statusCode) :
                            colors.magenta(res.statusCode)
                        ) :
                        colors.yellow(res.statusCode)
                    ) :
                    colors.green(res.statusCode)
                ) :
                colors.gray(res.statusCode)
            );

            console.log(
              `${start.getHours()}:${start.getMinutes()}:${start.getSeconds()}` +
              ` • ${ms} ms ${code} with ${req.method} on ${url}`
            );
          });

          next();
        });
      }

      app
        .use('/api', (req, res) => parseBody(req, res, async () => {
          req.search = req.url.substring(req.path.length + 1);

          res.throw = _throw;

          const path = (
            req.path[req.path.length - 1] === '/' ?
              req.path :
              req.path + '/'
          );

          if (
            mg.paths.hasOwnProperty(req.method) &&
            mg.paths[req.method].hasOwnProperty(path)
          ) {
            try {
              responseHandling: {
                for (
                  const policy of
                  mg.paths[req.method][path].policies
                ) {
                  await policy(req, res);

                  if (res.headersSent) {
                    break responseHandling;
                  }
                };

                await mg
                  .paths
                [req.method]
                [path]
                  .handler(req, res);
              }
            } catch (e) {
              console.log(e);

              res.throw(500);
            }
          } else {
            res.throw(404);
          }
        }))
        .use(compression({ threshold: 0 }))
        .use(express.static('static'))
        .use(async (req, res, next) => {
          req.user = await getUser(req.cookies.jwt);

          sapper.middleware({
            session: () => {
              return {
                apiUrl: API_URL,
                user: (
                  req.user ?
                    Object.assign({
                      isAuthenticated: true
                    }, {
                      firstName: req.user.firstName,
                      lastName: req.user.lastName,
                      username: req.user.username,
                      permissions: req.user.permissions
                    }) :
                    {
                      isAuthenticated: false
                    }
                )
              };
            }
          })(req, res, next);
        });

      mg.http = http.createServer(app);

      mg.http.listen(PORT, err => {
        if (err) console.log('error', err);
      });

      require(path.join(
        configPath,
        'functions',
        'jacketzip.js'
      ))().then(() => console.log('Jacketzip done!'));
    })
    .catch(console.log);
};

readFile(path.join(chunksPath, 'mgSpecs.json'))
  .then(data => {
    mgSpecs = JSON.parse(data);

    mg.policies = {};

    for (const policyName in mgSpecs.policies) {
      mg.policies[policyName] = require(path.join(
        chunksPath,
        `${policyName}-${mgSpecs.policies[policyName]}.js`
      ));
    }

    return access(configPath, fs.F_OK);
  })
  .then(() => Promise.all([
    access(specialEnvironmentPath).then(() => {
      const specialConfig = require(specialEnvironmentPath);

      if (mg.config) {
        mg.config = _.defaults(specialConfig, mg.config);
      } else {
        mg.config = specialConfig;
      }
    }),
    access(commonEnvironmentPath).then(() => {
      const commonConfig = require(commonEnvironmentPath);

      if (mg.config) {
        mg.config = _.defaults(mg.config, commonConfig);
      } else {
        mg.config = commonConfig;
      }
    })
  ]))
  .then(() => access(bootstrapPath, fs.F_OK))
  .then(require(bootstrapPath))
  .then(main)
  .catch(main);