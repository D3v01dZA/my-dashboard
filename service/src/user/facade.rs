use crate::db::connection::DbPool;
use crate::user::user_service::UserService;
use crate::user::model::Authentication;
use crate::result::Res;
use crate::result::Error;

pub struct UserFacade {
    db_pool: DbPool,
    user_service: UserService,
}

impl UserFacade {

    pub fn create(db_pool: DbPool) -> UserFacade {
        UserFacade { db_pool, user_service: UserService::create() }
    }

    pub fn authenticate(&self, name: String, password: String) -> Res<Authentication> {
        self.db_pool.in_transaction(|connection| {
            match self.user_service.get_user(connection, name.clone()) {
                Ok(option) => match option {
                    Some(user) => {
                        Ok(Authentication::create(name, "ASD".to_string(), user))
                    },
                    None => Err(Error::Authentication)
                }
                Err(err) => Err(err)
            }
        })
    }
}

