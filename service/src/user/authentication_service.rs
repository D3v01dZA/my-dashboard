use crate::user::model::User;
use crate::result::Res;
use crate::result::Error;
use crate::user::model::Authentication;

use blake2::Blake2b;
use blake2::Digest;
use sha3::Sha3_256;

pub fn authenticate(user: User, password: String) -> Res<Authentication> {
    let mut sha_hasher = Sha3_256::new();
    sha_hasher.input(password.clone());
    sha_hasher.input(user.get_salt_one());
    let sha_hash = sha_hasher.result().to_vec();
    let mut blake_hasher = Blake2b::new();
    blake_hasher.input(password.clone());
    blake_hasher.input(user.get_salt_two());
    let blake_hash = blake_hasher.result().to_vec();
    Ok(Authentication::create(blake_hash, user))
}