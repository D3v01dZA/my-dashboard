use crate::db::connection::DbConnection;
use crate::db::schema::users::dsl::users;
use crate::db::schema::users::dsl::name;
use crate::user::model::User;
use crate::result::Res;
use crate::result::Error;

use diesel::prelude::*;

pub fn get_user(connection: &DbConnection, user_name: String) -> Res<Option<User>> {
    match users.filter(name.eq(user_name)).first::<User>(connection) {
        Ok(user) => Ok(Some(user)),
        Err(diesel::result::Error::NotFound) => Ok(None),
        Err(err) => Err(Error::Diesel(err))
    }
}