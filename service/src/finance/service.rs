use crate::finance::model::{Account, UnsavedAccount};
use crate::db::schema::accounts::dsl::accounts;
use crate::db::connection::DbConnection;

use std::vec::Vec;
use diesel::prelude::*;
use diesel::result::Error;

pub struct AccountService {

}

impl AccountService {
    pub fn create() -> AccountService {
        AccountService{ }
    }

    pub fn get_accounts(&self, connection: &DbConnection) -> Result<Vec<Account>, String> {
        accounts.load::<Account>(connection)
            .map_err(|err| format!("{}", err))
    }

    pub fn get_account(&self, connection: &DbConnection, id: i32) -> Result<Option<Account>, String> {
        match accounts.find(id).first::<Account>(connection) {
            Ok(account) => Ok(Some(account)),
            Err(Error::NotFound) => Ok(None),
            Err(unknown) => Err(format!("{}", unknown))
        }
    }

    pub fn create_account(&self, connection: &DbConnection, unsaved_account: UnsavedAccount) -> Result<Account, String> {
        diesel::insert_into(accounts)
            .values(&unsaved_account)
            .get_result(connection)
            .map_err(|err| format!("{}", err))
    }
}