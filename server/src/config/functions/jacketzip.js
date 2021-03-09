const serviceAccount = require("../../../firebase/eventeger-40e9b-firebase-adminsdk-2u1bm-68241d4499.json");
const parsePermissions = require("../../../utils/permissionArrayToObject");
const getPermission = require("../../../utils/getPermission");
const admin = require("firebase-admin");
const webpush = require("web-push");
const tim = require("timsort");

function parseAndSort(array) {
    for (let i = 0; i < array.length; i += 1) {
        array[i] = parseInt(array[i], 10);
    }

    tim.sort(array, function comparator(a, b) {
        return a - b;
    });
}

module.exports = function jacketzip() {
    async function updateCache() {
        mg.cache.roles = {};
        mg.cache.classes = {};
        mg.cache.roleSuitable = {};
        mg.cache.classSuitable = {};

        const classes = await mg.knex.select('*').from('class');
        const roles = await mg.query("role").find(
            {},
            ["permission"]
        );

        let i;

        for (i = 0; i < classes.length; i++) {
            mg.cache.classSuitable[classes[i].id] = [];
            mg.cache.classes[classes[i].id] = classes[i];
        }

        for (i = 0; i < roles.length; i++) {
            mg.cache.roleSuitable[roles[i].id] = [];
        }

        for (i = 0; i < roles.length; i++) {
            mg.cache.roles[roles[i].id] = roles[i];
            roles[i].permissions = parsePermissions(
                roles[i]._relations.permission
            );

            const annCreate = getPermission(
                roles[i].permissions,
                ["announcement", "create"]
            );
            const annRead = getPermission(
                roles[i].permissions,
                ["announcement", "read"]
            );
            const classMultiple = getPermission(
                roles[i].permissions,
                ["class", "multiple"]
            );

            if (Array.isArray(annCreate)) {
                parseAndSort(roles[i].permissions.announcement.create);
            }

            if (annRead === true) {
                for (let j = 0; j < roles.length; j++) {
                    mg.cache.roleSuitable[roles[j].id].push(roles[i].id);
                }
            } else if (Array.isArray(annRead)) {
                parseAndSort(roles[i].permissions.announcement.read);

                for (
                    const roleId
                    of roles[i].permissions.announcement.create
                ) {
                    mg.cache.roleSuitable[roleId].push(roles[i].id);
                }
            }

            if (classMultiple === true) {
                for (let j = 0; j < classes.length; j++) {
                    mg.cache.classSuitable[classes[j].id].push(roles[i].id);
                }
            } else if (Array.isArray(classMultiple)) {
                parseAndSort(roles[i].permissions.class.multiple);

                for (
                    const classId
                    of roles[i].permissions.class.multiple
                ) {
                    mg.cache.classSuitable[classId].push(roles[i].id);
                }
            }

            delete roles[i]._relations;
        }
    }


    // Creating admin user. UNCOMMENT FOR INITIAL SETUP
    // mg.query("user").create({
    //     firstName: "Объект",
    //     lastName: "Тестовый",
    //     username: "asasasas",
    //     password: "asasasas",
    //     classId: 8
    // });
    

    // Initializing cache
    mg.cache.roleClassMap = {};
    setInterval(updateCache, 300000);
    // EO Initializing cache

    webpush.setVapidDetails(
        "mailto:akunec41@gmail.com",
        "BNYKuXiNeEasdlm9LJMUBl9ssPM9TkhsTRDXSnjyIxsq" +
        "YURjbT74PLW8BwN5henbxSponO2VP_NnqwZodKJAJHI",
        process.env.VAPID_PRIVATE_KEY
    );

    admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
        databaseURL: "https://modern-gymnasium.firebaseio.com",
    });

    mg.fbAdmin = admin;
    mg.webpush = webpush;

    return mg.knex
        .select("*")
        .from("class_role")
        .then((junctions) => {
            for (let i = 0; i < junctions.length; i += 1) {
                if (junctions[i].roleId in mg.cache.roleClassMap)
                    mg.cache.roleClassMap[junctions[i].roleId].add(junctions[i].classId);
                else
                    mg.cache.roleClassMap[junctions[i].roleId] = new Set([
                        junctions[i].classId,
                    ]);
            }
        })
        .then(updateCache);
};
