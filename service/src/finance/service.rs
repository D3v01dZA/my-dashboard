use crate::finance::model::{Account, UnsavedAccount};
use crate::db::schema::accounts::dsl::accounts;
use crate::db::connection::DbConnection;
use crate::result::Res;
use crate::result::Error;

use std::vec::Vec;
use diesel::prelude::*;

pub struct AccountService {

}

impl AccountService {
    pub fn create() -> AccountService {
        AccountService{ }
    }

    pub fn get_accounts(&self, connection: &DbConnection) -> Res<Vec<Account>> {
        accounts.load::<Account>(connection)
            .map_err(|err| Error::Diesel(err))
    }

    pub fn get_account(&self, connection: &DbConnection, id: i32) -> Res<Option<Account>> {
        match accounts.find(id).first::<Account>(connection) {
            Ok(account) => Ok(Some(account)),
            Err(diesel::result::Error::NotFound) => Ok(None),
            Err(err) => Err(Error::Diesel(err))
        }
    }

    pub fn create_account(&self, connection: &DbConnection, unsaved_account: UnsavedAccount) -> Res<Account> {
        diesel::insert_into(accounts)
            .values(&unsaved_account)
            .get_result(connection)
            .map_err(|err| Error::Diesel(err))
    }
}