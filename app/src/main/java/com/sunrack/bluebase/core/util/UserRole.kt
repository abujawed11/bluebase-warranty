package com.sunrack.bluebase.core.util

import com.sunrack.bluebase.data.model.User

/**
 * The developer role is hidden: it's not a distinct `account_type`, it's detected by username/email,
 * exactly like `login.tsx`'s `isDeveloper` check. See also `<RN>/DEVELOPER_README.md`.
 */
fun User.isHiddenDeveloper(): Boolean {
    return username in setOf("developer", "dev_admin", "abujawed11") || email == "developer@company.com"
}

enum class StartSection {
    Developer,
    Admin,
    Client,
}

/** Developer detection takes priority over `account_type`, matching `login.tsx`'s if/else order. */
fun User.startSection(): StartSection = when {
    isHiddenDeveloper() -> StartSection.Developer
    account_type == "admin" -> StartSection.Admin
    else -> StartSection.Client
}
