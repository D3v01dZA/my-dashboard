#![feature(proc_macro_hygiene, decl_macro)]

#[macro_use] extern crate rocket;
#[macro_use] extern crate serde_derive;

extern crate serde;

mod finance;
mod time;

fn main() {
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