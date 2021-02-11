import { sortedHas } from 'arrays';
import getPermission from 'getPermission';

export default {
  getUsersTokens: async announcementId => {
    announcementId = Number(announcementId);

    if (announcementId) {
      const result = new Array();
      const userIDs = await mg.knex
        .select('user.id')
        .from('user')
        .innerJoin('announcement_class_role', function () {
          this
            .on(
              'announcement_class_role.role_id',
              '=',
              'user.role_id'
            )
            .andOn(
              'announcement_class_role.class_id',
              '=',
              'user.class_id'
            )
        })
        .where(
          'announcement_class_role.announcement_id',
          announcementId
        );

      for (let i = 0; i < userIDs.length; i += 1) {
        if (mg.cache.usersTokens.hasOwnProperty(userIDs[i].id)) {
          for (
            let j = 0;
            j < mg.cache.usersTokens[userIDs[i].id].length;
            j += 1
          ) {
            result.push(mg.cache.usersTokens[userIDs[i].id][j]);
          }
        }
      }

      return result;
    }

    return null;
  },

  getAnnouncementAtId: async id => {
    id = Number(id);

    if (!isNaN(id)) {
      return (await mg.knex
        .select('*')
        .from('announcement')
        .where(
          'announcement.id',
          id
        ))[0];
    }

    return null;
  },

  notify: async announcement => {

  },

  isAvaiable: async (announcement, user) => {
    const roles = getPermission(mg.cache.roles[user.roleId], ['announcement', 'read']);
    const classes = getPermission(mg.cache.roles[user.roleId], ['class', 'multiple']);

    if (roles === true && classes === true) {
      return true;
    } else if (roles && classes) {
      const junctions = await mg.knex
        .select('classId', 'roleId')
        .from('announcementClassRole')
        .where('announcementId', announcement.id);

      for (let j = 0; j < junctions.length; j += 1)
        if (
          (roles === true || sortedHas(roles, junctions[j].roleId)) &&
          (classes === true || sortedHas(classes, junctions[j].classId))
        ) {
          return true;
        }
    } else {
      return false;
    }

    return false;
  }
}