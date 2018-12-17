use crate::finance::account::*;
use crate::finance::service::*;

use rocket::State;
use rocket_contrib::json::Json;

#[get("/")]
pub fn index() -> &'static str {
    "Account Index"
}

#[get("/accounts")]
pub fn accounts(account_service: State<AccountService>) -> Json<Vec<Account>> {
    Json(account_service.get_accounts())
}

#[post("/accounts", data = "<account>")]
pub fn accounts_put(account_service: State<AccountService>, account: Json<UnsavedAccount>) -> Json<Account> {
    Json(account_service.create_account(account.into_inner()))
}

#[get("/accounts/<id>")]
pub fn accounts_get(account_service: State<AccountService>, id: u64) -> Json<Option<Account>> {
    Json(account_service.get_account(id))
}