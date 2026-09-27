package com.sunrack.bluebase.ui.navigation

import com.sunrack.bluebase.core.util.StartSection
import com.sunrack.bluebase.core.util.startSection
import com.sunrack.bluebase.data.model.User
import kotlinx.serialization.Serializable

@Serializable object LoginRoute
@Serializable object RegisterRoute
@Serializable object ForgotPasswordRoute

// Placeholders until Phase 3 (Navigation shell) replaces these with the real drawers/stacks.
@Serializable object ClientHomeRoute
@Serializable object AdminHomeRoute
@Serializable object DeveloperHomeRoute

fun User.startRoute(): Any = when (startSection()) {
    StartSection.Developer -> DeveloperHomeRoute
    StartSection.Admin -> AdminHomeRoute
    StartSection.Client -> ClientHomeRoute
}
