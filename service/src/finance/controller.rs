use crate::finance::model::{Account, UnsavedAccount};
use crate::finance::service::AccountService;
use crate::db::connection::DbPool;

use rocket::State;
use rocket_contrib::json::Json;

#[get("/")]
pub fn index() -> &'static str {
    "Account Index"
}

#[get("/accounts")]
pub fn accounts(db_pool: State<DbPool>, account_service: State<AccountService>) -> Json<Vec<Account>> {
    match db_pool.in_transaction(|connection| account_service.get_accounts(connection)) {
        Ok(accounts) => Json(accounts),
        Err(err) => panic!(err)
    }
}

#[post("/accounts", data = "<account>")]
pub fn accounts_put(db_pool: State<DbPool>, account_service: State<AccountService>, account: Json<UnsavedAccount>) -> Json<Account> {
    match db_pool.in_transaction(|connection| account_service.create_account(connection, account.into_inner())) {
        Ok(account) => Json(account),
        Err(err) => panic!(err)
    }
}

#[get("/accounts/<id>")]
pub fn accounts_get(db_pool: State<DbPool>, account_service: State<AccountService>, id: i32) -> Option<Json<Account>> {
    match db_pool.in_transaction(|connection| account_service.get_account(connection, id)) {
        Ok(Some(account)) => Some(Json(account)),
        Ok(None) => None,
        Err(err) => panic!(err)
    }
}