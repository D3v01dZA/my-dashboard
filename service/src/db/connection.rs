use crate::result::Res;
use crate::result::Error;

use diesel::r2d2::{ConnectionManager, Pool, PooledConnection};
use diesel::prelude::*;
use std::env;

pub type DbConnection = PooledConnection<ConnectionManager<PgConnection>>;

pub struct DbPool {
    pool: Pool<ConnectionManager<PgConnection>>
}

impl DbPool {
    pub fn create() -> DbPool {
        let database_url = env::var("DATABASE_URL").expect("DATABASE_URL must be set");
        let manager = ConnectionManager::<PgConnection>::new(database_url);
        DbPool { pool: Pool::builder().build(manager).expect("Failed to create pool.") }
    }

    pub fn in_transaction<F, T>(&self, f: F) -> Res<T>
        where F: FnOnce(&DbConnection) -> Res<T>
    {
        match self.pool.get() {
            Ok(connection) => in_transaction_internal(connection, f),
            Err(err) => Err(Error::Sundry(format!("{}", err)))
        }
    }
}

fn in_transaction_internal<F, T>(connection: DbConnection, f: F) -> Res<T>
    where F: FnOnce(&DbConnection) -> Res<T>
{
    let mut result = Err(Error::Sundry("Result Never Ran".to_string()));
    match connection.transaction(|| {
        result = f(&connection);
        match &result {
            Ok(_) => Ok("Success".to_string()),
            Err(_) => Err(diesel::result::Error::RollbackTransaction)
        }
    }) {
        Ok(_) => result,
        Err(diesel::result::Error::RollbackTransaction) => result,
        Err(error) => Err(Error::Diesel(error))
    }
}