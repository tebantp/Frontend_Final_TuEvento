package com.tuevento.tueventofinal.util

import android.util.Patterns

// CORRECCIÓN #15: La validación de password se alinea con el backend.
// El backend solo valida @Size(min=6). Antes el frontend exigía 8 chars +
// mayúscula + número, bloqueando usuarios válidos para el backend.
// Se mantiene la validación institucional como recomendación, no bloqueo.

object ValidationUtils {

    fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isUdecEmail(email: String): Boolean =
        email.endsWith("@ucundinamarca.edu.co", ignoreCase = true)

    // Alineado con el backend: @Size(min = 6)
    fun isValidPassword(password: String): Boolean = password.length >= 6

    // Recomendación visual (no bloquea envío, solo muestra mensaje de ayuda en UI)
    fun isStrongPassword(password: String): Boolean {
        val pattern = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$"
        return password.matches(pattern.toRegex())
    }

    fun isValidPhone(phone: String): Boolean =
        phone.length == 10 && phone.all { it.isDigit() }

    fun isNotBlank(value: String): Boolean = value.isNotBlank()

    fun isValidCupo(cupo: String): Boolean =
        cupo.toIntOrNull()?.let { it >= 1 } ?: false
}
