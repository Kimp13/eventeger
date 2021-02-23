const serviceAccount = require("../../../firebase/modern-gymnasium-firebase-adminsdk-okjez-8ffd3d1395.json");
const parsePermissions = require("../../../utils/permissionArrayToObject");
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

        const classes = await mg.query("class").find();
        const roles = await mg.query("role").find(
            {},
            ["permission"]
        );

        let i;

        for (i = 0; i < classes.length; i += 1)
            mg.cache.classes[classes[i].id] = classes[i];

        for (i = 0; i < roles.length; i += 1) {
            mg.cache.roles[roles[i].id] = roles[i];
            roles[i].permissions = parsePermissions(roles[i]._relations.permission);

            if (roles[i].permissions !== true && "announcement" in roles[i].permissions) {
                if ("create" in roles[i].permissions.announcement) {
                    parseAndSort(roles[i].permissions.announcement.create);
                }

                if ("read" in roles[i].permissions.announcement) {
                    parseAndSort(roles[i].permissions.announcement.read);
                }
            }

            delete roles[i]._relations;
        }
    }

    // Creating admin user. UNCOMMENT FOR INITIAL SETUP
    // mg.query("user").create({
    //     firstName: "Георгий",
    //     lastName: "Бердников",
    //     username: "asdfasdf",
    //     password: "asdfasdf",
    // });
    //

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
