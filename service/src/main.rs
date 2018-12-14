#![feature(proc_macro_hygiene, decl_macro)]

#[macro_use] extern crate rocket;

mod finance;
mod time;

fn main() {
    rocket::ignite()
        .mount("/finance", routes![finance::index])
        .mount("/time", routes![time::index])
        .launch();
}