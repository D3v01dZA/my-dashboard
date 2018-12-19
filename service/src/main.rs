#![feature(proc_macro_hygiene, decl_macro)]
#![allow(proc_macro_derive_resolution_fallback)]

#[macro_use] extern crate rocket;
#[macro_use] extern crate rocket_contrib;
#[macro_use] extern crate serde_derive;
#[macro_use] extern crate diesel;

extern crate serde;
extern crate dotenv;

pub mod finance;
pub mod time;
pub mod db;

use dotenv::dotenv;
use rocket_contrib::json::JsonValue;

fn main() {
    dotenv().ok();
    let account_service = finance::service::AccountService::create();
    let db_pool = db::connection::DbPool::create();
    rocket::ignite()
        .manage(db_pool)
        .mount("/finances", routes![
            finance::controller::index,
            finance::controller::accounts,
            finance::controller::accounts_put,
            finance::controller::accounts_get
        ])
        .manage(account_service)
        .mount("/time-sheets", routes![
            time::index
        ])
        .register(catchers![
            not_found,
            internal_error
        ])
        .launch();
}

#[catch(404)]
fn not_found() -> JsonValue {
    json!({
        "message": "resource not found"
    })
}

#[catch(500)]
fn internal_error() -> JsonValue {
    json!({
        "message": "internal server error"
    })
}
