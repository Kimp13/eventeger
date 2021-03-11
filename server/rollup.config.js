import resolve from '@rollup/plugin-node-resolve';
import replace from '@rollup/plugin-replace';
import commonjs from '@rollup/plugin-commonjs';
import babel from '@rollup/plugin-babel';
import json from '@rollup/plugin-json';
import svelte from 'rollup-plugin-svelte';
import postcss from "rollup-plugin-postcss";
import includePaths from 'rollup-plugin-includepaths';
import { terser } from 'rollup-plugin-terser';
import config from 'sapper/config/rollup.js';
import pkg from './package.json';

import path from 'path';
import hash from 'random-hash';

import { readdir, mkdir, writeFile, unlink } from './utils/filesystem';
import glob from './utils/globPromise';
import getRouteName from './utils/getRouteName';

const mode = process.env.NODE_ENV;
const dev = mode === 'development';
const legacy = !!process.env.SAPPER_LEGACY_BUILD;

const onwarn = (warning, onwarn) =>
    (warning.code === 'MISSING_EXPORT' &&
        /'preload'/.test(warning.message)) ||
    (warning.code === 'CIRCULAR_DEPENDENCY' &&
        /[/\\]@sapper[/\\]/.test(warning.message)) ||
    onwarn(warning);

/**
 * svelte-preprocess
 */
const { preprocess } = require('./svelte.config');

const postcssConfig = () => ({
    extensions: [".scss", ".sass"],
    extract: false,
    minimize: true,
    use: [
        [
            "sass",
            {
                includePaths: [
                    "./src/theme",
                    "./node_modules",
                    path.resolve(__dirname, "..", "node_modules")
                ]
            }
        ]
    ]
});

const includePathsOptions = () => ({
    include: {},
    paths: ['utils', 'src/components', 'static'],
    external: [],
    extensions: ['.js']
});

export default {
    client: {
        input: config.client.input(),
        output: config.client.output(),
        plugins: [
            replace({
                'process.browser': true,
                'process.env.NODE_ENV': JSON.stringify(mode)
            }),
            json(),
            svelte({
                dev,
                hydratable: true,
                emitCss: false,
                css: true,
                preprocess,
                onwarn: (warning, handler) => {
                    if (warning.code === 'a11y-label-has-associated-control')
                        return;
                    handler(warning);
                }
            }),
            resolve({
                browser: true,
                dedupe: ['svelte']
            }),
            commonjs(),
            includePaths(includePathsOptions()),
            postcss(postcssConfig()),

            legacy && babel({
                extensions: ['.js', '.mjs', '.html', '.svelte'],
                babelHelpers: 'runtime',
                exclude: ['node_modules/@babel/**'],
                presets: [
                    ['@babel/preset-env', {
                        targets: '> 0.25%, not dead'
                    }]
                ],
                plugins: [
                    '@babel/plugin-syntax-dynamic-import',
                    ['@babel/plugin-transform-runtime', {
                        useESModules: true
                    }]
                ]
            }),

            !dev && terser({
                module: true
            })
        ],
        preserveEntrySignatures: false,
        onwarn
    },

    server: {
        input: config.server.input(),
        output: Object.assign(
            config.server.output(),
            {
                exports: 'auto'
            }
        ),
        plugins: [
            replace({
                'process.browser': false,
                'process.env.NODE_ENV': JSON.stringify(mode)
            }),
            json(),
            svelte({
                generate: 'ssr',
                hydratable: true,
                emitCss: false,
                css: true,
                dev,
                preprocess,
                onwarn: (warning, handler) => {
                    if (warning.code === 'a11y-label-has-associated-control')
                        return;
                    handler(warning);
                }
            }),
            resolve({
                dedupe: ['svelte']
            }),
            postcss(postcssConfig()),
            commonjs(),
            includePaths(includePathsOptions()),
            babel({
                extensions: ['.js', '.mjs', '.html', '.svelte'],
                babelHelpers: 'runtime',
                exclude: ['node_modules/@babel/**'],
                presets: [
                    ['@babel/preset-env', {
                        targets: '> 0.25%, not dead'
                    }]
                ],
                plugins: [
                    '@babel/plugin-syntax-dynamic-import',
                    ['@babel/plugin-transform-runtime', {
                        useESModules: true
                    }]
                ]
            }),

            {
                async buildStart() {
                    const regEx = /^src\/api\/(.+)\/(.+)\..+$/;

                    const writeModel = object => {
                        return files => {
                            for (const file of files) {
                                const executed = regEx.exec(file);
                                const modelName = getRouteName(executed[1]);
                                const fileName = executed[2];
                                const hashed = hash({ length: 16 });

                                this.addWatchFile(file);
                                this.emitFile({
                                    type: 'chunk',
                                    id: file,
                                    fileName: `${fileName}-${hashed}.js`
                                });

                                object[modelName] = hashed;
                            }
                        };
                    };

                    const mg = {
                        routes: {},
                        services: {},
                        controllers: {},
                        models: {},
                        policies: {}
                    };

                    const secondDir = dev ? 'dev' : 'build';

                    glob(path.join(
                        '__sapper__',
                        secondDir,
                        'server',
                        '@(controllers|model|routes|services)-*.js'
                    )).then(files => {
                        for (const file of files) {
                            unlink(file);
                        }
                    });

                    await readdir('__sapper__')
                        .then(null, () => mkdir('__sapper__'));

                    await readdir(`__sapper__/${secondDir}`)
                        .then(null, () => mkdir(`__sapper__/${secondDir}`));

                    await readdir(`__sapper__/${secondDir}/server`)
                        .then(null, () => mkdir(
                            `__sapper__/${secondDir}/server`
                        ));

                    await Promise.all([
                        glob('src/api/*/routes.@(json|js)')
                            .then(writeModel(mg.routes), console.log),

                        glob('src/api/*/services.js')
                            .then(writeModel(mg.services), console.log),

                        glob('src/api/*/controllers.js')
                            .then(writeModel(mg.controllers), console.log),

                        glob('src/api/*/model.js')
                            .then(writeModel(mg.models), console.log),

                        glob('src/config/functions/policies/*.js')
                            .then(files => {
                                for (const file of files) {
                                    const name = /^.*\/(.+).js$/.exec(file)[1];
                                    const hashed = hash({ length: 16 });

                                    this.emitFile({
                                        type: 'chunk',
                                        id: file,
                                        fileName: `${name}-${hashed}.js`
                                    });

                                    mg.policies[name] = hashed;
                                }
                            }),

                        glob('src/config/**/*.js')
                            .then(files => {
                                for (const file of files) {
                                    this.addWatchFile(file);
                                }
                            })
                    ]);

                    await writeFile(
                        `__sapper__/${secondDir}/server/mgSpecs.json`,
                        JSON.stringify(mg)
                    );
                }
            }
        ],
        external: Object.keys(pkg.dependencies).concat(
            require('module').builtinModules
        ),

        preserveEntrySignatures: 'strict',
        onwarn,
    },

    serviceworker: {
        input: config.serviceworker.input(),
        output: config.serviceworker.output(),
        plugins: [
            resolve(),
            replace({
                'process.browser': true,
                'process.env.NODE_ENV': JSON.stringify(mode)
            }),
            commonjs(),
            !dev && terser()
        ],

        preserveEntrySignatures: false,
        onwarn,
    }
};
