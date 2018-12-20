use std::fmt::Display;
use std::fmt::Formatter;

pub type Res<T> = Result<T, Error>;

pub enum Error {
    Diesel(diesel::result::Error),
    Sundry(String)
}

impl Display for Error {
    fn fmt(&self, f: &mut Formatter) -> Result<(), std::fmt::Error> {
        match self {
            Error::Diesel(error) => write!(f, "Diesel: {}", error),
            Error::Sundry(string) => write!(f, "Sundry: {}", string)
        }
    }
}