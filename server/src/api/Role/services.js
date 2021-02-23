import getPermission from "getPermission";

export default {
    findUsersCreateRolesIds(user) {
        const permission = getPermission(user.permissions, [
            'announcement',
            'create'
        ]);

        const retarr = [];

        if (permission === true)
            for (const roleId in mg.cache.roles)
                retarr.push(parseInt(roleId, 10));
        else if (Array.isArray(permission))
            for (const roleId of permission)
                retarr.push(parseInt(roleId, 10));

        return retarr;
    },

    async getUsersCreateMap(user) {
        const roles = mg.services.role.findUsersCreateRolesIds(user);
        const promiseArray = [];
        const retobj = {};

        for (const roleId of roles) {
            promiseArray.push(
                mg.services.class.findRolesClasses(roleId, user)
                    .then(classes => {
                        retobj[roleId] = classes;
                    })
            );
        }

        await Promise.all(promiseArray);

        return retobj;
    }
};