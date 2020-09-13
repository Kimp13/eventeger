module.exports = {
  getUsers: async announcementId => {
    announcementId = Number(announcementId);

    if (announcementId) {
      return await mg.knex('user')
        .innerJoin('announcement_class_role', function() {
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
        )
    }
    
    return null;
  }
}