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

    pub fn in_transaction<F, T>(&self, f: F) -> Result<T, String>
        where F: FnOnce(&DbConnection) -> Result<T, String>
    {
        match self.pool.get() {
            Ok(connection) => self.in_transaction_internal(connection, f),
            Err(error) => Err(format!("{}", error.to_string()))
        }
    }

    fn in_transaction_internal<F, T>(&self, connection: DbConnection, f: F) -> Result<T, String>
        where F: FnOnce(&DbConnection) -> Result<T, String>
    {
        let mut result = Err("Result Never Ran".to_string());
        match connection.transaction(|| {
            result = f(&connection);
            match &result {
                Ok(_) => Ok("Success".to_string()),
                Err(_) => Err(diesel::result::Error::RollbackTransaction)
            }
        }) {
            Ok(_) => result,
            Err(error) => Err(format!("{}", error.to_string()))
        }
    }
}