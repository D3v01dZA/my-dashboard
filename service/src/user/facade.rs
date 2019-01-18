use crate::db::connection::DbPool;
use crate::user::user_service;
use crate::user::authentication_service;
use crate::user::model::Authentication;
use crate::result::Res;
use crate::result::Error;

pub struct UserFacade {
    db_pool: DbPool
}

impl UserFacade {

    pub fn create(db_pool: DbPool) -> UserFacade {
        UserFacade { db_pool }
    }

    pub fn authenticate(&self, name: String, password: String) -> Res<Authentication> {
        self.db_pool.in_transaction(|connection| {
            match user_service::get_user(connection, name.clone()) {
                Ok(option) => match option {
                    Some(user) => authentication_service::authenticate(user, password),
                    None => Err(Error::Authentication)
                }
                Err(err) => Err(err)
            }
        })
    }
}

