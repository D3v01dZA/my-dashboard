use crate::finance::model::{Account, UnattachedAccount};
use crate::finance::facade::FinanceFacade;
use crate::user::model::Authentication;

use rocket::State;
use rocket_contrib::json::Json;

#[get("/")]
pub fn index(user: Authentication) -> &'static str {
    "Account Index"
}

#[get("/accounts")]
pub fn accounts(authentication: Authentication, finance_facade: State<FinanceFacade>) -> Json<Vec<Account>> {
    match finance_facade.get_accounts(&authentication) {
        Ok(accounts) => Json(accounts),
        Err(err) => {
            error!("{}", err);
            panic!(err)
        }
    }
}

#[post("/accounts", data = "<account>")]
pub fn accounts_put(authentication: Authentication, finance_facade: State<FinanceFacade>, account: Json<UnattachedAccount>) -> Json<Account> {
    match finance_facade.create_account(&authentication, account.into_inner()) {
        Ok(account) => Json(account),
        Err(err) => {
            error!("{}", err);
            panic!(err)
        }
    }
}

#[get("/accounts/<id>")]
pub fn accounts_get(authentication: Authentication, finance_facade: State<FinanceFacade>, id: i32) -> Option<Json<Account>> {
    match finance_facade.get_account(&authentication, id) {
        Ok(Some(account)) => Some(Json(account)),
        Ok(None) => None,
        Err(err) => {
            error!("{}", err);
            panic!(err)
        }
    }
}