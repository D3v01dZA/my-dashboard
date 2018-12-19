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
        let connection = self.pool.get().unwrap();
        connection.transaction::<Result<T, String>, diesel::result::Error, _>(|| {
            match f(&connection) {
                Ok(result) => Ok(Ok(result)),
                Err(_) => Err(diesel::result::Error::RollbackTransaction)
            }
        }).unwrap()
    }
}