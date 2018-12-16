#![feature(proc_macro_hygiene, decl_macro)]

#[macro_use] extern crate rocket;
#[macro_use] extern crate serde_derive;
#[macro_use] extern crate lazy_static;

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
        .mount("/time-sheets", routes![
            time::index
        ])
        .launch();
}