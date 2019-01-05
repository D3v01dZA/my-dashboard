use crate::db::connection::DbPool;
use crate::finance::account_service::AccountService;
use crate::result::Res;
use crate::finance::model::Account;
use crate::finance::model::UnattachedAccount;
use crate::user::model::Authentication;

pub struct FinanceFacade {

    db_pool: DbPool,
    account_service: AccountService

}

impl FinanceFacade {

    pub fn create(db_pool: DbPool) -> FinanceFacade {
        FinanceFacade {db_pool, account_service: AccountService::create()}
    }

    pub fn get_account(&self, authentication: &Authentication, id: i32) -> Res<Option<Account>> {
        self.db_pool.in_transaction(|connection| {
            self.account_service.get_account(connection, id)
        })
    }

    pub fn create_account(&self, authentication: &Authentication, account: UnattachedAccount) -> Res<Account> {
        self.db_pool.in_transaction(|connection| {
            self.account_service.create_account(connection, authentication, account)
        })
    }

    pub fn get_accounts(&self, authentication: &Authentication) -> Res<Vec<Account>> {
        self.db_pool.in_transaction(|connection| {
            self.account_service.get_accounts(connection)
        })
    }

}