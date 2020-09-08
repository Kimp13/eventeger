// server dependencies
import dotenv from 'dotenv';
import sirv from 'sirv';
import polka from 'polka';
import cookie from 'cookie';
import bodyParser from 'body-parser';
import compression from 'compression';
import * as sapper from '@sapper/server';

// database
import Knex from 'knex';
import Bookshelf from 'bookshelf';

// filesystem
import path from 'path';
import fs from 'fs';

// utilities
import _ from 'lodash';
import getRouteName from '../utils/getRouteName';
import { createContext } from 'vm';

dotenv.config();

// bookshelf instance
const knex = new Knex({
  client: process.env.DB_CLIENT || 'mysql',
  connection: {
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || 'root',
    database: process.env.DB_DATABASE || 'main',
    charset: process.env.DB_CHARSET || 'utf8'
  }
});
const bookshelf = new Bookshelf(knex);

// working with filesystem
const srcPath = path.join(process.cwd(), 'src');
const modelsPath = path.join(srcPath, 'api');
const configPath = path.join(srcPath, 'config');

// environment
const { PORT, NODE_ENV, API_URL } = process.env;
const dev = NODE_ENV === 'development';

if (!API_URL) {
  API_URL = 'http://localhost:3000';
}

/**
 * Global variable containing server cache, plugins, models, functions
 * @returns JS object
 */
const mg = new Object();
global.mg = mg;

/**
 * Main function of the server.
 * @returns nothing
 */
const main = () => {
  // proceed if connected to database
  knex.raw('select 1 + 1 as testValue').then(() => {
    // gathering models
    fs.readdir(modelsPath, (err, files) => {
      mg.paths = new Object();
      mg.models = new Object();
      mg.services = new Object();
      mg.cache = new Object();
      
      for (let i = 0; i < files.length; i += 1) {
        let currentPath = path.join(modelsPath, files[i]),
            modelPath = path.join(currentPath, 'model.js'),
            servicesPath = path.join(currentPath, 'services.js'),
            routesPath = path.join(currentPath, 'routes.json'),
            controllersPath = path.join(currentPath, 'controllers.js'),
            routeName = getRouteName(files[i]);
  
        fs.access(modelPath, fs.F_OK, err => {
          if (err) return;
          
          let model = require(modelPath);
  
          knex.schema.hasTable(model.tableName).then(exists => {
            if (exists) {
              mg.models[files[i]] = bookshelf.model(files[i], {
                requireFetch: false,
                ...model
              });

              if (files[i].toLowerCase() === 'user') {
                mg
                  .models[files[i]]
                  .count()
                  .then(count => (mg.cache.usersCount = count));
              }
            }
          });
        });
  
        fs.access(servicesPath, fs.F_OK, err => {
          if (err) return;
  
          mg.services[_.camelCase(files[i])] = require(servicesPath);
        });
  
        fs.access(routesPath, fs.F_OK, err => {
          if (err) return;
  
          fs.access(controllersPath, fs.F_OK, err => {
            let routes = require(routesPath),
                controllers = require(controllersPath);
    
            for (let j = 0; j < routes.length; j += 1) {
              routes[j].method = routes[j].method.toUpperCase();
  
              if (
                routes[j].path.charAt(
                  routes[j].path.length - 1
                ) !== '/'
              ) {
                routes[j].path += '/';
              }
      
              if (!mg.paths.hasOwnProperty(routes[j].method)) {
                mg.paths[routes[j].method] = new Object();
              }
      
              mg.paths[routes[j].method][`/${routeName}${routes[j].path}`] = (
                routes[j].handler === 'default' ?
                  controllers :
                  controllers[routes[j].handler]
              );
            }
          });
        });
      }
    });

    polka()
      .use(compression({ threshold: 0 }))
      .use(sirv('static', { dev }))
      .use(bodyParser.urlencoded({ extended: true }))
      .use(async (req, res, next) => {
        const start = new Date();
    
        await next();
    
        const ms = Date.now() - start.getTime();
    
        console.log(
          `${start.toLocaleString()} | ${req.method} on ` +
          req.url + ' took ' + ms + ' ms'
        );
      })
      .use(async (req, res, next) => {
        const path = /^\/api((\/([\w_\.~-]|(%[\dA-F]))*)+)?(?=\?|$)/.exec(req.url);

        if (path) {
          req.path = path[1] ? (
            path[1].charAt(path[1].length - 1) === '/' ?
            path[1] :
            path[1] + '/'
          ) : '/';

          if (
            mg.paths.hasOwnProperty(req.method) &&
            mg.paths[req.method].hasOwnProperty(req.path)
          ) {
            req.cookies = cookie.parse(req.headers.cookie || '');

            await mg.paths[req.method][req.path](req, res);
          } else {
            res.status = 404;
          }
        } else {
          await next();
        }

        return;
      })
      .use(sapper.middleware({
        session: () => {
          return {
            apiUrl: API_URL
          };
        }
      }))
      .listen(PORT, err => {
        if (err) console.log('error', err);
      });
  });
};

fs.access(configPath, fs.F_OK, err => {
  if (err) main();

  const bootstrapPath = path.join(configPath, 'functions', 'bootstrap.js');
  const commonEnvironmentPath = path.join(configPath, 'environments', 'common.js');
  const specialEnvironmentPath = (
    process.env.NODE_ENV === 'production' ?
      path.join(configPath, 'environments', 'production.js') :
      path.join(configPath, 'environments', 'development.js')
  );

  fs.access(bootstrapPath, fs.F_OK, err => {
    if (err) main();

    require(bootstrapPath)().then(main);
  });

  fs.access(commonEnvironmentPath, fs.F_OK, err => {
    if (err) return;

    let commonConfig = require(commonEnvironmentPath);

    if (mg.config) {
      mg.config = _.defaults(mg.config, commonConfig);
    } else {
      mg.config = commonConfig;
    }
  });

  fs.access(specialEnvironmentPath, fs.F_OK, err => {
    if (err) return;

    let specialConfig = require(specialEnvironmentPath);

    if (mg.config) {
      mg.config = _.defaults(specialConfig, mg.config);
    } else {
      mg.config = specialConfig;
    }
  });
});
