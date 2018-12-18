use crate::finance::model::{Account, UnsavedAccount};
use crate::db::schema::accounts::dsl::accounts;
use crate::db::connection::DbConnection;

use std::vec::Vec;
use diesel::prelude::*;

pub struct AccountService {

}

impl AccountService {
    pub fn create() -> AccountService {
        AccountService{ }
    }

    pub fn get_accounts(&self, connection: DbConnection) -> Vec<Account> {
        accounts.load::<Account>(&connection).unwrap()
    }

    pub fn get_account(&self, connection: DbConnection, id: i32) -> Option<Account> {
        accounts.find(id)
            .first::<Account>(&connection)
            .ok()
    }

    pub fn create_account(&self, connection: DbConnection, unsaved_account: UnsavedAccount) -> Account {
        diesel::insert_into(accounts)
            .values(&unsaved_account)
            .get_result(&connection)
            .unwrap()
    }
}