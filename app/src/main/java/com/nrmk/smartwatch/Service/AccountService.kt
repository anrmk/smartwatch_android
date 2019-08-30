package com.nrmk.smartwatch.Service

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import com.nrmk.smartwatch.Data.Dto.AccountDto

class AccountService {
    fun getAccount(accountManager: AccountManager): Account? {
        var accounts: Array<Account> = accountManager.getAccountsByType("com.google")
        return if (accounts.count() > 0) accounts[0] else null
    }

    fun getAccountInfo(context: Context): AccountDto? {
        val accountManager = AccountManager.get(context)
        val account = getAccount(accountManager)

        return if(account != null) AccountDto(account.name, "") else null
    }
}