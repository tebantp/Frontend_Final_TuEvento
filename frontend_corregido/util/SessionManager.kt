package com.tuevento.tueventofinal.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.tuevento.tueventofinal.data.model.UsuarioResponse

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "TuEventoPrefs"
        private const val KEY_USER = "user_session"
        private const val KEY_ROLE = "user_role"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    // El backend no emite JWT — la sesión se persiste con el objeto UsuarioResponse
    // serializado en SharedPreferences. No se guarda token ya que no existe en el backend.
    fun saveSession(user: UsuarioResponse) {
        val userJson = gson.toJson(user)
        prefs.edit().apply {
            putString(KEY_USER, userJson)
            putString(KEY_ROLE, user.rol.name)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun getUser(): UsuarioResponse? {
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, UsuarioResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun logout() {
        prefs.edit().clear().apply()
    }
}
