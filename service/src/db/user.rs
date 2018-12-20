use crate::result::Res;
use crate::result::Error;

use rocket::request::FromRequest;
use rocket::Request;
use rocket::http::Status;
use rocket::request::Outcome;
use rocket::outcome::Outcome::Success;
use rocket::outcome::Outcome::Failure;
use std::str::from_utf8;

pub struct User {
    name: String,
    password: String
}

impl<'a, 'r> FromRequest<'a, 'r> for User {
    type Error = ();

    fn from_request(request: &'a Request<'r>) -> Outcome<User, ()> {
        match read_authorization(request) {
            Ok(user) => Success(user),
            Err(_) => Failure((Status::Unauthorized, ()))
        }
    }
}

fn read_authorization(request: &Request) -> Res<User> {
    let single_header = extract_single_header(request.headers().get("Authorization").collect())?;
    let base_sixty_four = read_base_sixty_four_value(single_header)?;
    let decoded = decode_base_sixty_four(base_sixty_four)?;
    let utf = decoded_to_utf(decoded)?;
    let split_auth = split_auth(utf)?;
    Ok(User {name: split_auth.0, password: split_auth.1})
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