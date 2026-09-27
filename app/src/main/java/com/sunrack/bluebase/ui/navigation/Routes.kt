package com.sunrack.bluebase.ui.navigation

import com.sunrack.bluebase.core.util.StartSection
import com.sunrack.bluebase.core.util.startSection
import com.sunrack.bluebase.data.model.User
import kotlinx.serialization.Serializable

@Serializable object LoginRoute
@Serializable object RegisterRoute
@Serializable object ForgotPasswordRoute

// Each section owns a nested NavHost of its own (see ClientShell/AdminShell/DeveloperShell);
// these are just the top-level entry points the root NavHost redirects to by role.
@Serializable object ClientSectionRoute
@Serializable object AdminSectionRoute
@Serializable object DeveloperSectionRoute

fun User.startRoute(): Any = when (startSection()) {
    StartSection.Developer -> DeveloperSectionRoute
    StartSection.Admin -> AdminSectionRoute
    StartSection.Client -> ClientSectionRoute
}
