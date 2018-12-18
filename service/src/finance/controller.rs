use crate::finance::model::*;
use crate::finance::service::*;
use crate::db::connection::*;

use rocket::State;
use rocket_contrib::json::Json;

#[get("/")]
pub fn index() -> &'static str {
    "Account Index"
}

#[get("/accounts")]
pub fn accounts(db_pool: State<DbPool>, account_service: State<AccountService>) -> Json<Vec<Account>> {
    Json(account_service.get_accounts(db_pool.get().unwrap()))
}

#[post("/accounts", data = "<account>")]
pub fn accounts_put(db_pool: State<DbPool>, account_service: State<AccountService>, account: Json<UnsavedAccount>) -> Json<Account> {
    Json(account_service.create_account(db_pool.get().unwrap(), account.into_inner()))
}

#[get("/accounts/<id>")]
pub fn accounts_get(db_pool: State<DbPool>, account_service: State<AccountService>, id: i32) -> Option<Json<Account>> {
    account_service.get_account(db_pool.get().unwrap(), id).map(|account| Json(account))
}