use crate::finance::model::*;
use crate::db::schema::accounts::dsl::accounts;

use std::vec::Vec;
use std::env;
use diesel::prelude::*;

pub struct AccountService {

}

impl AccountService {
    pub fn create() -> AccountService {
        AccountService{  }
    }

    pub fn get_accounts(&self) -> Vec<Account> {
        let connection = self.database_connection();
        accounts.load::<Account>(&connection).unwrap()
    }

    pub fn get_account(&self, id: i32) -> Option<Account> {
        let connection = self.database_connection();
        accounts.find(id)
            .first::<Account>(&connection)
            .ok()
    }

    pub fn create_account(&self, unsaved_account: UnsavedAccount) -> Account {
        let connection = self.database_connection();
        diesel::insert_into(accounts)
            .values(&unsaved_account)
            .get_result(&connection)
            .unwrap()
    }

    fn database_connection(&self) -> PgConnection {
        let database_url = env::var("DATABASE_URL").expect("DATABASE_URL must be set");
        PgConnection::establish(&database_url).expect(&format!("Error connecting to database"))
    }
}