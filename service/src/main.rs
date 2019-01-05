#![feature(proc_macro_hygiene, decl_macro)]
#![allow(proc_macro_derive_resolution_fallback)]

#[macro_use] extern crate rocket;
#[macro_use] extern crate rocket_contrib;
#[macro_use] extern crate serde_derive;
#[macro_use] extern crate diesel;
#[macro_use] extern crate log;

extern crate serde;
extern crate dotenv;
extern crate base64;
extern crate simple_logger;

pub mod finance;
pub mod time;
pub mod db;
pub mod result;
pub mod user;

use dotenv::dotenv;
use rocket_contrib::json::JsonValue;
use log::Level;

fn main() {
    dotenv().ok();
    simple_logger::init_with_level(Level::Info).unwrap();
    let db_pool = db::connection::DbPool::create();
    let user_facade = user::facade::UserFacade::create(db_pool.clone());
    let finance_facade = finance::facade::FinanceFacade::create(db_pool.clone());
    rocket::ignite()
        .manage(user_facade)
        .mount("/finances", routes![
            finance::controller::index,
            finance::controller::accounts,
            finance::controller::accounts_put,
            finance::controller::accounts_get
        ])
        .manage(finance_facade)
        .mount("/time-sheets", routes![
            time::index
        ])
        .register(catchers![
            unauthorized,
            not_found,
            internal_error
        ])
        .launch();
}

#[catch(401)]
fn unauthorized() -> JsonValue {
    json!({
        "message": "not authorized"
    })
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
