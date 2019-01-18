use crate::result::Res;
use crate::result::Error;
use crate::user::model::Authentication;
use crate::user::facade::UserFacade;

use rocket::request::FromRequest;
use rocket::Request;
use rocket::http::Status;
use rocket::request::Outcome;
use rocket::outcome::Outcome::Success;
use rocket::outcome::Outcome::Failure;
use std::str::from_utf8;
use rocket::State;

pub mod model;
pub mod facade;
mod user_service;
mod authentication_service;

impl<'a, 'r> FromRequest<'a, 'r> for Authentication {
    type Error = ();

    fn from_request(request: &'a Request<'r>) -> Outcome<Authentication, ()> {
        match authorize(request) {
            Ok(user) => Success(user),
            Err(err) => {
                warn!("Authorization: {}", err);
                Failure((Status::Unauthorized, ()))
            }
        }
    }
}

fn authorize(request: &Request) -> Res<Authentication> {
    let (name, password) = read_authorization(request)?;
    let user_facade = match request.guard::<State<UserFacade>>() {
        Success(user_facade) => Ok(user_facade),
        _ => Err(Error::Sundry("Request returned no user facade".to_string()))
    }?;
    user_facade.authenticate(name, password)
}

fn read_authorization(request: &Request) -> Res<(String, String)> {
    let single_header = extract_single_header(request.headers().get("Authorization").collect())?;
    let base_sixty_four = read_base_sixty_four_value(single_header)?;
    let decoded = decode_base_sixty_four(base_sixty_four)?;
    let utf = decoded_to_utf(decoded)?;
    split_auth(utf)
}

fn extract_single_header(headers: Vec<&str>) -> Res<&str> {
    match headers.as_slice() {
        [auth] => Ok(auth),
        [] => Err(Error::Sundry("No auth header".to_string())),
        _ => Err(Error::Sundry("Too many auth headers".to_string()))
    }
}

fn read_base_sixty_four_value(auth: &str) -> Res<&str> {
    let split: Vec<&str> = auth.split(" ").collect();
    match split.as_slice() {
        [basic, base_sixty_four] if basic == &"Basic" => Ok(base_sixty_four),
        [_, _] => Err(Error::Sundry("Strategy not recognized".to_string())),
        _ => Err(Error::Sundry("Authorization not recognized".to_string()))
    }
}

fn decode_base_sixty_four(auth: &str) -> Res<Vec<u8>> {
    base64::decode(auth)
        .map_err(|_| Error::Sundry("Invalid Base64".to_string()))
}

fn decoded_to_utf(decoded: Vec<u8>) -> Res<String> {
    from_utf8(decoded.as_slice())
        .map_err(|_| Error::Sundry("Invalid UTF8".to_string()))
        .map(|result| result.to_string())
}

fn split_auth(utf: String) -> Res<(String, String)> {
    let split: Vec<&str> = utf.split(":").collect();
    match split.as_slice() {
        [name, password] => Ok((name.to_string(), password.to_string())),
        _ => Err(Error::Sundry(format!("Invalid username and password")))
    }
}