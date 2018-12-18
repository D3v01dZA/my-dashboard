#![feature(proc_macro_hygiene, decl_macro)]
#![allow(proc_macro_derive_resolution_fallback)]

#[macro_use] extern crate rocket;
#[macro_use] extern crate serde_derive;
#[macro_use] extern crate diesel;

extern crate serde;
extern crate dotenv;

pub mod finance;
pub mod time;
pub mod db;

use dotenv::dotenv;

fn main() {
    dotenv().ok();
    rocket::ignite()
        .mount("/finances", routes![
            finance::controller::index,
            finance::controller::accounts,
            finance::controller::accounts_put,
            finance::controller::accounts_get
        ])
        .manage(finance::service::AccountService::create())
        .mount("/time-sheets", routes![
            time::index
        ])
        .launch();
}